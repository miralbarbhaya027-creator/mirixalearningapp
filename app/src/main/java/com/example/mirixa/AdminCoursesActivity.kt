package com.example.mirixa

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Base64
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.mirixa.databinding.ActivityAdminCoursesBinding
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.io.ByteArrayOutputStream

class AdminCoursesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminCoursesBinding
    private val database = FirebaseDatabase.getInstance()
    private val courseList = mutableListOf<Course>()
    private val categoryMap = mutableMapOf<String, String>() // id -> name
    private var searchQuery = ""
    private var filterCategoryId: String? = null

    private var activeImageCallback: ((String) -> Unit)? = null

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            try {
                val inputStream = contentResolver.openInputStream(it)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                if (bitmap != null) {
                    val base64 = bitmapToBase64(bitmap)
                    activeImageCallback?.invoke(base64)
                }
            } catch (_: Exception) {
                Toast.makeText(this, "Failed to load image from gallery", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap: Bitmap? ->
        bitmap?.let {
            val base64 = bitmapToBase64(it)
            activeImageCallback?.invoke(base64)
        }
    }

    private var activeVideoCallback: ((String) -> Unit)? = null
    private var activePdfCallback: ((String) -> Unit)? = null

    private val videoPickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            activeVideoCallback?.invoke(it.toString())
        }
    }

    private val pdfPickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            activePdfCallback?.invoke(it.toString())
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminCoursesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        filterCategoryId = intent.getStringExtra("category_id")
        val categoryName = intent.getStringExtra("category_name")
        if (!categoryName.isNullOrEmpty()) {
            binding.etSearchCourses.setText(categoryName)
            searchQuery = categoryName
        }

        binding.btnBack.setOnClickListener {
            startActivity(Intent(this, AdminActivity::class.java))
            finish()
        }

        binding.btnProfileTop.setOnClickListener {
            startActivity(Intent(this, AdminSettingsActivity::class.java))
            finish()
        }

        binding.btnFilterCourses.setOnClickListener {
            val categoriesList = listOf("All Categories") + categoryMap.values.toList()
            val categoryIdsList = listOf<String?>(null) + categoryMap.keys.toList()

            AlertDialog.Builder(this)
                .setTitle("Filter by Category")
                .setItems(categoriesList.toTypedArray()) { _, which ->
                    filterCategoryId = categoryIdsList[which]
                    val selectedName = categoriesList[which]
                    Toast.makeText(this, "Filtered by: $selectedName", Toast.LENGTH_SHORT).show()
                    renderCourses()
                }
                .show()
        }

        setupNavigation()
        setupSearch()
        fetchCategoriesAndCourses()

        binding.btnAddCourse.setOnClickListener {
            showCourseDialog(null)
        }
    }

    private fun setupNavigation() {
        binding.navAdminOverview.setOnClickListener {
            startActivity(Intent(this, AdminActivity::class.java))
            finish()
        }
        binding.navAdminUsers.setOnClickListener {
            startActivity(Intent(this, AdminUsersActivity::class.java))
            finish()
        }
        binding.navAdminCategories.setOnClickListener {
            startActivity(Intent(this, AdminCategoriesActivity::class.java))
            finish()
        }
        binding.navAdminSettings.setOnClickListener {
            startActivity(Intent(this, AdminSettingsActivity::class.java))
            finish()
        }
    }

    private fun setupSearch() {
        binding.etSearchCourses.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchQuery = s.toString().trim()
                renderCourses()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun fetchCategoriesAndCourses() {
        database.reference.child("categories").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(catSnapshot: DataSnapshot) {
                categoryMap.clear()
                for (cat in catSnapshot.children) {
                    val id = cat.key ?: ""
                    val name = cat.child("name").getValue(String::class.java) ?: "General"
                    categoryMap[id] = name
                }
                fetchCourses()
            }

            override fun onCancelled(error: DatabaseError) {
                fetchCourses()
            }
        })
    }

    private fun fetchCourses() {
        database.reference.child("courses").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                courseList.clear()
                for (child in snapshot.children) {
                    val key = child.key ?: ""
                    val course = child.getValue(Course::class.java)
                    if (course != null) {
                        val validCourse = if (course.id.isEmpty()) course.copy(id = key) else course
                        courseList.add(validCourse)
                    }
                }
                binding.tvActiveCoursesCount.text = "${courseList.size} Courses Published"
                renderCategoryChips()
                renderCourses()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@AdminCoursesActivity, "Failed to load courses: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun renderCategoryChips() {
        val container = binding.categoryChipsContainer
        container.removeAllViews()

        val allChip = createCategoryChip("All Categories (${courseList.size})", null, filterCategoryId == null)
        container.addView(allChip)

        for ((id, name) in categoryMap) {
            val count = courseList.count { it.categoryId == id }
            val chip = createCategoryChip("$name ($count)", id, filterCategoryId == id)
            container.addView(chip)
        }
    }

    private fun createCategoryChip(title: String, catId: String?, isSelected: Boolean): TextView {
        val textView = TextView(this).apply {
            text = title
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
            setTypeface(null, Typeface.BOLD)
            setPadding(36, 18, 36, 18)
            setTextColor(if (isSelected) Color.WHITE else Color.parseColor("#4F5B66"))
            setBackgroundResource(R.drawable.card_background)
            backgroundTintList = ColorStateList.valueOf(
                if (isSelected) Color.parseColor("#2962FF") else Color.parseColor("#F1F4FF")
            )
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                marginEnd = 10.dpToPx()
            }
            layoutParams = params
            setOnClickListener {
                filterCategoryId = catId
                renderCategoryChips()
                renderCourses()
            }
        }
        return textView
    }

    private fun Int.dpToPx(): Int = (this * resources.displayMetrics.density).toInt()

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return "data:image/jpeg;base64," + Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    private fun base64ToBitmap(base64Str: String): Bitmap? {
        return try {
            val pureBase64 = if (base64Str.contains(",")) base64Str.substringAfter(",") else base64Str
            val decodedBytes = Base64.decode(pureBase64, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        } catch (_: Exception) {
            null
        }
    }

    private fun loadCourseImageToView(imageUrl: String?, imageView: ImageView) {
        if (imageUrl.isNullOrEmpty()) {
            imageView.setImageResource(R.drawable.ic_book)
            return
        }
        if (imageUrl.startsWith("data:image") || (imageUrl.length > 100 && !imageUrl.startsWith("http"))) {
            val bitmap = base64ToBitmap(imageUrl)
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap)
            } else {
                imageView.setImageResource(R.drawable.ic_book)
            }
        } else {
            val resId = resources.getIdentifier(imageUrl, "drawable", packageName)
            if (resId != 0) {
                imageView.setImageResource(resId)
            } else {
                imageView.setImageResource(R.drawable.ic_book)
            }
        }
    }

    private fun renderCourses() {
        binding.adminCoursesList.removeAllViews()

        val filtered = courseList.filter { course ->
            val matchesCategory = filterCategoryId == null || course.categoryId == filterCategoryId
            val matchesSearch = searchQuery.isEmpty() ||
                    course.title.contains(searchQuery, ignoreCase = true) ||
                    course.description.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesSearch
        }

        if (filtered.isEmpty()) {
            val emptyView = TextView(this).apply {
                text = if (searchQuery.isEmpty()) "No courses found." else "No courses match '$searchQuery'"
                textSize = 14f
                setTextColor(getColor(R.color.text_light))
                setPadding(0, 32, 0, 32)
                gravity = Gravity.CENTER
            }
            binding.adminCoursesList.addView(emptyView)
            return
        }

        for (course in filtered) {
            addCourseCardToUi(course)
        }
    }

    private fun addCourseCardToUi(course: Course) {
        val view = LayoutInflater.from(this).inflate(R.layout.item_admin_course, binding.adminCoursesList, false)

        val ivIcon = view.findViewById<ImageView>(R.id.iv_course_icon)
        loadCourseImageToView(course.imageUrl, ivIcon)

        val index = courseList.indexOf(course) + 1
        view.findViewById<TextView>(R.id.tv_slot_badge).text = "Slot #$index"
        view.findViewById<TextView>(R.id.tv_course_title).text = course.title

        val catName = categoryMap[course.categoryId] ?: "Category"
        view.findViewById<TextView>(R.id.tv_category_badge).text = catName.uppercase()

        val hasVideo = !course.videoLecture?.url.isNullOrEmpty()
        val hasPdf = !course.note?.pdfUrl.isNullOrEmpty()
        val hasMindmap = !course.mindmap?.image.isNullOrEmpty()
        val hasQuiz = !course.quiz.isNullOrEmpty()

        view.findViewById<TextView>(R.id.tv_video_status).text = if (hasVideo) "🎥 Video Ready" else "⏳ Video Pending"
        view.findViewById<TextView>(R.id.tv_pdf_status).text = if (hasPdf) "📄 PDF Attached" else "⏳ PDF Pending"
        view.findViewById<TextView>(R.id.tv_mindmap_status).text = if (hasMindmap) "🧠 Mind Map Ready" else "⏳ Mind Map Pending"
        view.findViewById<TextView>(R.id.tv_quiz_status).text = if (hasQuiz) "✅ Quiz Ready" else "⏳ Quiz Pending"

        val btnView = view.findViewById<MaterialButton>(R.id.btn_view_course)
        val btnEdit = view.findViewById<MaterialButton>(R.id.btn_edit_course)
        val btnDelete = view.findViewById<MaterialCardView>(R.id.btn_delete_course)

        btnView.setOnClickListener {
            val intent = Intent(this, CourseDetailsActivity::class.java).apply {
                putExtra("course_id", course.id)
            }
            startActivity(intent)
        }

        btnEdit.setOnClickListener {
            showCourseDialog(course)
        }

        btnDelete.setOnClickListener {
            val targetId = course.id.ifEmpty { "c_${course.title.lowercase().replace(" ", "_")}" }
            if (targetId.isEmpty()) {
                Toast.makeText(this, "Cannot delete course: Invalid ID", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            AlertDialog.Builder(this)
                .setTitle("Delete Course")
                .setMessage("Are you sure you want to delete '${course.title}'?")
                .setPositiveButton("Delete") { _, _ ->
                    val oldCatId = course.categoryId
                    database.reference.child("courses").child(targetId).removeValue()
                        .addOnSuccessListener {
                            if (oldCatId.isNotEmpty()) {
                                database.reference.child("categories").child(oldCatId).get().addOnSuccessListener { catSnap ->
                                    val cat = catSnap.getValue(Category::class.java)
                                    if (cat != null && cat.courseCount > 0) {
                                        database.reference.child("categories").child(oldCatId).child("courseCount").setValue(cat.courseCount - 1)
                                    }
                                }
                            }
                            Toast.makeText(this, "'${course.title}' deleted", Toast.LENGTH_SHORT).show()
                        }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        binding.adminCoursesList.addView(view)
    }

    private fun showCourseDialog(existingCourse: Course?) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_course, null)
        val tvTitle = dialogView.findViewById<TextView>(R.id.tv_dialog_title)
        val etTitle = dialogView.findViewById<TextInputEditText>(R.id.et_course_title)
        val etDesc = dialogView.findViewById<TextInputEditText>(R.id.et_course_desc)
        val etImage = dialogView.findViewById<TextInputEditText>(R.id.et_image_url)
        val etMindmap = dialogView.findViewById<TextInputEditText>(R.id.et_mindmap_url)
        val etVideo = dialogView.findViewById<TextInputEditText>(R.id.et_video_url)
        val etPdf = dialogView.findViewById<TextInputEditText>(R.id.et_pdf_url)
        val spinnerCategory = dialogView.findViewById<Spinner>(R.id.spinner_category)

        val btnPickVideoFile = dialogView.findViewById<MaterialButton>(R.id.btn_pick_video_file)
        val btnPickPdfFile = dialogView.findViewById<MaterialButton>(R.id.btn_pick_pdf_file)

        val btnDeleteVideo = dialogView.findViewById<MaterialButton>(R.id.btn_delete_video)
        val btnDeletePdf = dialogView.findViewById<MaterialButton>(R.id.btn_delete_pdf)
        val btnDeleteQuiz = dialogView.findViewById<MaterialButton>(R.id.btn_delete_quiz)

        val etQuizQuestion = dialogView.findViewById<TextInputEditText>(R.id.et_quiz_question)
        val etQuizOptA = dialogView.findViewById<TextInputEditText>(R.id.et_quiz_opt_a)
        val etQuizOptB = dialogView.findViewById<TextInputEditText>(R.id.et_quiz_opt_b)
        val etQuizOptC = dialogView.findViewById<TextInputEditText>(R.id.et_quiz_opt_c)
        val etQuizOptD = dialogView.findViewById<TextInputEditText>(R.id.et_quiz_opt_d)
        val spinnerQuizCorrect = dialogView.findViewById<Spinner>(R.id.spinner_quiz_correct)

        val quizOptions = arrayOf("Option A", "Option B", "Option C", "Option D")
        val quizAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, quizOptions)
        spinnerQuizCorrect.adapter = quizAdapter

        val ivDialogImage = dialogView.findViewById<ImageView>(R.id.iv_dialog_course_image)
        val btnChangeImage = dialogView.findViewById<LinearLayout>(R.id.btn_change_image_overlay)
        val btnDeleteImage = dialogView.findViewById<MaterialCardView>(R.id.btn_delete_dialog_image)

        val ivDialogMindmap = dialogView.findViewById<ImageView>(R.id.iv_dialog_mindmap_image)
        val btnChangeMindmap = dialogView.findViewById<LinearLayout>(R.id.btn_change_mindmap_overlay)
        val btnDeleteMindmap = dialogView.findViewById<MaterialCardView>(R.id.btn_delete_dialog_mindmap)

        var currentImageUrl = existingCourse?.imageUrl ?: ""
        var currentMindmapUrl = existingCourse?.mindmap?.image ?: ""

        fun updateDialogImagePreview(url: String) {
            currentImageUrl = url
            etImage.setText(url)
            loadCourseImageToView(url, ivDialogImage)
        }

        fun updateDialogMindmapPreview(url: String) {
            currentMindmapUrl = url
            etMindmap.setText(url)
            loadCourseImageToView(url, ivDialogMindmap)
        }

        updateDialogImagePreview(currentImageUrl)
        updateDialogMindmapPreview(currentMindmapUrl)

        var activeTargetIsMindmap = false

        activeImageCallback = { newBase64 ->
            if (activeTargetIsMindmap) {
                updateDialogMindmapPreview(newBase64)
            } else {
                updateDialogImagePreview(newBase64)
            }
        }

        val showImageOptionMenu = { isMindmap: Boolean ->
            activeTargetIsMindmap = isMindmap
            val titleStr = if (isMindmap) "Mind Map Diagram Options" else "Course Thumbnail Options"
            val options = arrayOf(
                "🖼️ Choose from Gallery",
                "📸 Take Photo with Camera",
                "🎨 Select Preset Asset",
                "🗑️ Remove / Delete Image"
            )
            AlertDialog.Builder(this)
                .setTitle(titleStr)
                .setItems(options) { _, which ->
                    when (which) {
                        0 -> galleryLauncher.launch("image/*")
                        1 -> cameraLauncher.launch(null)
                        2 -> {
                            if (isMindmap) {
                                showPresetMindMapPicker { selectedPreset ->
                                    updateDialogMindmapPreview(selectedPreset)
                                }
                            } else {
                                showPresetThumbnailPicker { selectedPreset ->
                                    updateDialogImagePreview(selectedPreset)
                                }
                            }
                        }
                        3 -> {
                            if (isMindmap) {
                                updateDialogMindmapPreview("")
                            } else {
                                updateDialogImagePreview("")
                            }
                            Toast.makeText(this, "Image removed", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        btnChangeImage.setOnClickListener { showImageOptionMenu(false) }
        ivDialogImage.setOnClickListener { showImageOptionMenu(false) }
        btnDeleteImage.setOnClickListener { updateDialogImagePreview(""); Toast.makeText(this, "Thumbnail removed", Toast.LENGTH_SHORT).show() }

        btnChangeMindmap.setOnClickListener { showImageOptionMenu(true) }
        ivDialogMindmap.setOnClickListener { showImageOptionMenu(true) }
        btnDeleteMindmap.setOnClickListener { updateDialogMindmapPreview(""); Toast.makeText(this, "Mind Map removed", Toast.LENGTH_SHORT).show() }

        activeVideoCallback = { uriStr ->
            etVideo.setText(uriStr)
            Toast.makeText(this, "Video file selected!", Toast.LENGTH_SHORT).show()
        }

        activePdfCallback = { uriStr ->
            etPdf.setText(uriStr)
            Toast.makeText(this, "PDF Document selected!", Toast.LENGTH_SHORT).show()
        }

        btnPickVideoFile.setOnClickListener {
            videoPickerLauncher.launch("video/*")
        }

        btnPickPdfFile.setOnClickListener {
            pdfPickerLauncher.launch("application/pdf")
        }

        btnDeleteVideo.setOnClickListener {
            etVideo.setText("")
            Toast.makeText(this, "Video cleared", Toast.LENGTH_SHORT).show()
        }

        btnDeletePdf.setOnClickListener {
            etPdf.setText("")
            Toast.makeText(this, "PDF Notes cleared", Toast.LENGTH_SHORT).show()
        }

        btnDeleteQuiz.setOnClickListener {
            etQuizQuestion.setText("")
            etQuizOptA.setText("")
            etQuizOptB.setText("")
            etQuizOptC.setText("")
            etQuizOptD.setText("")
            Toast.makeText(this, "Quiz cleared", Toast.LENGTH_SHORT).show()
        }

        val catList = categoryMap.toList() // Pair<Id, Name>
        val catNames = if (catList.isNotEmpty()) catList.map { it.second } else listOf("Programming Languages", "Android Development", "Web Development", "Artificial Intelligence", "Database Management")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, catNames)
        spinnerCategory.adapter = adapter

        if (existingCourse != null) {
            tvTitle.text = "Edit Course"
            etTitle.setText(existingCourse.title)
            etDesc.setText(existingCourse.description)
            etVideo.setText(existingCourse.videoLecture?.url ?: "")
            etPdf.setText(existingCourse.note?.pdfUrl ?: "")

            if (!existingCourse.quiz.isNullOrEmpty()) {
                val q = existingCourse.quiz[0]
                etQuizQuestion.setText(q.question)
                if (q.options.size >= 4) {
                    etQuizOptA.setText(q.options[0])
                    etQuizOptB.setText(q.options[1])
                    etQuizOptC.setText(q.options[2])
                    etQuizOptD.setText(q.options[3])
                }
                if (q.correctOptionIndex in 0..3) {
                    spinnerQuizCorrect.setSelection(q.correctOptionIndex)
                }
            }

            val currentCatName = categoryMap[existingCourse.categoryId]
            val index = catNames.indexOf(currentCatName)
            if (index >= 0) spinnerCategory.setSelection(index)
        } else {
            tvTitle.text = "Add New Course"
        }

        AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton(if (existingCourse != null) "Update" else "Create") { _, _ ->
                val title = etTitle.text.toString().trim()
                val desc = etDesc.text.toString().trim()
                val imageKey = currentImageUrl.ifEmpty { etImage.text.toString().trim() }
                val mindmapImgKey = currentMindmapUrl.ifEmpty { etMindmap.text.toString().trim() }
                var videoUrl = etVideo.text.toString().trim()
                val pdfUrl = etPdf.text.toString().trim()

                val qPrompt = etQuizQuestion.text.toString().trim()
                val qA = etQuizOptA.text.toString().trim()
                val qB = etQuizOptB.text.toString().trim()
                val qC = etQuizOptC.text.toString().trim()
                val qD = etQuizOptD.text.toString().trim()
                val correctIndex = spinnerQuizCorrect.selectedItemPosition

                if (title.isEmpty()) {
                    Toast.makeText(this, "Course Title is required", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val selectedCatIndex = spinnerCategory.selectedItemPosition
                val selectedCatId = if (catList.isNotEmpty() && selectedCatIndex in catList.indices) {
                    catList[selectedCatIndex].first
                } else {
                    (selectedCatIndex + 1).toString()
                }

                val courseId = existingCourse?.id ?: "c_${System.currentTimeMillis()}"

                if (videoUrl.isNotEmpty() && !videoUrl.startsWith("http")) {
                    videoUrl = "https://www.youtube.com/watch?v=$videoUrl"
                }

                val videoLecture = if (videoUrl.isNotEmpty()) VideoLecture(videoUrl, title, "15:00") else null
                val note = if (pdfUrl.isNotEmpty()) CourseNote(pdfUrl, existingCourse?.note?.topics ?: emptyList()) else null
                val finalImageUrl = imageKey.ifEmpty { existingCourse?.imageUrl ?: "course_${title.lowercase().replace(" ", "_")}" }

                val mindmapObj = if (mindmapImgKey.isNotEmpty()) {
                    Mindmap(image = mindmapImgKey, nodes = existingCourse?.mindmap?.nodes ?: listOf(MindmapNode("n1", "Core", 150f, 150f)))
                } else null

                val updatedQuiz = if (qPrompt.isNotEmpty() && qA.isNotEmpty() && qB.isNotEmpty()) {
                    listOf(Question(qPrompt, listOf(qA, qB, qC.ifEmpty { "Option C" }, qD.ifEmpty { "Option D" }), correctIndex))
                } else null

                val updatedCourse = Course(
                    id = courseId,
                    categoryId = selectedCatId,
                    title = title,
                    description = desc.ifEmpty { "Master $title concepts." },
                    imageUrl = finalImageUrl,
                    videoLecture = videoLecture,
                    note = note,
                    mindmap = mindmapObj,
                    quiz = updatedQuiz
                )

                database.reference.child("courses").child(courseId).setValue(updatedCourse)
                    .addOnSuccessListener {
                        if (existingCourse == null) {
                            database.reference.child("categories").child(selectedCatId).get().addOnSuccessListener { catSnap ->
                                val cat = catSnap.getValue(Category::class.java)
                                if (cat != null) {
                                    database.reference.child("categories").child(selectedCatId).child("courseCount").setValue(cat.courseCount + 1)
                                }
                            }
                        }
                        Toast.makeText(this, "Course '$title' saved successfully!", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showPresetMindMapPicker(onSelected: (String) -> Unit) {
        val presets = arrayOf(
            "mindmap_python", "mindmap_js", "mindmap_java", "mindmap_kotlin",
            "mindmap_cpp", "mindmap_c", "mindmap_android_basics", "mindmap_xml",
            "mindmap_compose", "mindmap_room", "mindmap_firebase", "mindmap_html",
            "mindmap_css", "mindmap_react", "mindmap_php", "mindmap_ai_f",
            "mindmap_ml", "mindmap_prompt"
        )
        AlertDialog.Builder(this)
            .setTitle("Select Preset Mind Map")
            .setItems(presets) { _, which ->
                onSelected(presets[which])
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showPresetThumbnailPicker(onSelected: (String) -> Unit) {
        val presets = arrayOf(
            "course_python", "course_javascript", "course_java", "course_kotlin",
            "course_cpp", "course_c", "course_android_basics", "course_xml",
            "course_compose", "course_room", "course_firebase", "course_html",
            "course_css", "course_react", "course_php", "course_ai_f",
            "course_ml", "course_prompt"
        )
        AlertDialog.Builder(this)
            .setTitle("Select Preset Thumbnail")
            .setItems(presets) { _, which ->
                onSelected(presets[which])
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
