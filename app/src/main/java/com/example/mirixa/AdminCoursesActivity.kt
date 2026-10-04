package com.example.mirixa

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.mirixa.databinding.ActivityAdminCoursesBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AdminCoursesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminCoursesBinding
    private val database = FirebaseDatabase.getInstance("https://mirixa-b998b-default-rtdb.firebaseio.com")
    private val courseList = mutableListOf<Course>()
    private var filterCatId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminCoursesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        filterCatId = intent.getStringExtra("FILTER_CAT_ID")

        setupClickListeners()
        setupNavigation()
        binding.root.post { fetchCourses() }

        // Live Search
        binding.etSearchCourses.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterCourses(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun openAdminPage(targetClass: Class<*>) {
        if (this::class.java == targetClass) return
        val intent = Intent(this, targetClass).apply {
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        startActivity(intent)
        overridePendingTransition(0, 0)
        finish()
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener { finish() }

        binding.btnProfileTop.setOnClickListener {
            openAdminPage(AdminSettingsActivity::class.java)
        }

        binding.btnTopAddCourse.setOnClickListener {
            binding.nestedScrollCourses.smoothScrollTo(0, binding.cvQuickAddCourseCard.top)
            binding.etNewCourseTitle.requestFocus()
        }

        binding.btnSaveCourse.setOnClickListener {
            saveCourseToFirebase()
        }

        binding.btnCancelCourse.setOnClickListener {
            binding.etNewCourseTitle.text?.clear()
            binding.etNewCourseCategory.text?.clear()
        }
    }

    private fun saveCourseToFirebase() {
        val title = binding.etNewCourseTitle.text.toString().trim()
        val catIdInput = binding.etNewCourseCategory.text.toString().trim().ifEmpty { filterCatId ?: "1" }

        if (title.isEmpty()) {
            Toast.makeText(this, "Please enter course title", Toast.LENGTH_SHORT).show()
            return
        }

        val auth = FirebaseAuth.getInstance()
        if (auth.currentUser != null) {
            performFirebaseSaveCourse(title, catIdInput)
        } else {
            auth.signInAnonymously()
                .addOnSuccessListener { performFirebaseSaveCourse(title, catIdInput) }
                .addOnFailureListener {
                    auth.signInWithEmailAndPassword("mirixalearning@gmail.com", "mirixa0365")
                        .addOnSuccessListener { performFirebaseSaveCourse(title, catIdInput) }
                        .addOnFailureListener {
                            auth.createUserWithEmailAndPassword("mirixalearning@gmail.com", "mirixa0365")
                                .addOnCompleteListener { performFirebaseSaveCourse(title, catIdInput) }
                        }
                }
        }
    }

    private fun performFirebaseSaveCourse(title: String, catId: String) {
        val newId = "c_" + System.currentTimeMillis()
        val newCourse = Course(
            id = newId,
            categoryId = catId,
            title = title,
            description = "Comprehensive masterclass covering $title fundamentals and hands-on projects.",
            imageUrl = "course_python",
            videoLecture = VideoLecture("https://www.youtube.com/watch?v=rfscVS0vtbw", title, "15:00"),
            note = CourseNote("https://www.guru99.com/python-tutorials.html", listOf(CourseTopic("Basics", "Core logic of $title", "bulb"))),
            mindmap = Mindmap("mindmap_python", listOf(MindmapNode("n1", "Core", 150f, 150f))),
            quiz = listOf(Question("What is $title?", listOf("Option A", "Option B", "Option C", "Option D"), 0))
        )

        database.reference.child("courses").child(newId).setValue(newCourse)
            .addOnSuccessListener {
                database.reference.child("categories").child(catId).child("courseCount").get()
                    .addOnSuccessListener { snapshot ->
                        val currentCount = snapshot.getValue(Int::class.java) ?: 0
                        database.reference.child("categories").child(catId).child("courseCount").setValue(currentCount + 1)
                    }

                Toast.makeText(this, "Course '$title' created & Category course count updated!", Toast.LENGTH_SHORT).show()
                binding.etNewCourseTitle.text?.clear()
                binding.etNewCourseCategory.text?.clear()
                binding.nestedScrollCourses.smoothScrollTo(0, 0)
            }
            .addOnFailureListener { err ->
                Toast.makeText(this, "Save error: ${err.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupNavigation() {
        binding.navAdminDashboard.setOnClickListener { openAdminPage(AdminActivity::class.java) }
        binding.navAdminCourses.setOnClickListener {
            Toast.makeText(this, "You are on Course Library", Toast.LENGTH_SHORT).show()
        }
        binding.navAdminCategory.setOnClickListener { openAdminPage(AdminCategoriesActivity::class.java) }
        binding.navAdminUsers.setOnClickListener { openAdminPage(AdminUsersActivity::class.java) }
        binding.navAdminProfile.setOnClickListener { openAdminPage(AdminSettingsActivity::class.java) }
    }

    private fun fetchCourses() {
        database.reference.child("courses").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                courseList.clear()
                if (snapshot.exists() && snapshot.hasChildren()) {
                    for (cSnapshot in snapshot.children) {
                        try {
                            val course = cSnapshot.getValue(Course::class.java)
                            course?.let { courseList.add(it) }
                        } catch (e: Exception) {
                            val id = cSnapshot.child("id").getValue(String::class.java) ?: cSnapshot.key ?: ""
                            val catId = cSnapshot.child("categoryId").getValue(String::class.java) ?: "1"
                            val title = cSnapshot.child("title").getValue(String::class.java) ?: ""
                            val desc = cSnapshot.child("description").getValue(String::class.java) ?: ""
                            val img = cSnapshot.child("imageUrl").getValue(String::class.java) ?: "course_python"
                            if (title.isNotEmpty()) {
                                courseList.add(Course(id, catId, title, desc, img))
                            }
                        }
                    }
                }

                updateCategoryCourseCounts()

                val displayedList = if (filterCatId != null) {
                    courseList.filter { it.categoryId == filterCatId }
                } else {
                    courseList
                }

                val total = courseList.size
                binding.tvTotalCoursesStat.text = total.toString()
                binding.tvVideoStat.text = "$total/$total"
                binding.tvPdfStat.text = "$total/$total"
                binding.tvMindmapStat.text = "$total/$total"

                renderCourses(displayedList)
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun updateCategoryCourseCounts() {
        val catCounts = mutableMapOf<String, Int>()
        for (course in courseList) {
            val cId = course.categoryId
            if (cId.isNotEmpty()) {
                catCounts[cId] = (catCounts[cId] ?: 0) + 1
            }
        }

        for ((cId, count) in catCounts) {
            database.reference.child("categories").child(cId).child("courseCount").setValue(count)
        }
    }

    private fun filterCourses(query: String) {
        val baseList = if (filterCatId != null) courseList.filter { it.categoryId == filterCatId } else courseList
        val filtered = baseList.filter {
            (it.title.contains(query, ignoreCase = true)) ||
            (it.description.contains(query, ignoreCase = true)) ||
            (it.id.contains(query, ignoreCase = true))
        }
        renderCourses(filtered)
    }

    private fun renderCourses(courses: List<Course>) {
        binding.adminCoursesList.removeAllViews()

        if (courses.isEmpty()) {
            val container = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = android.view.Gravity.CENTER
                setPadding(32, 32, 32, 32)
            }

            val emptyTv = TextView(this).apply {
                text = "No courses allocated to this category track yet.\nUse 'Quick Add Course Blueprint' below or tap 'Show All Courses'."
                textSize = 13f
                setTextColor(getColor(R.color.text_medium))
                gravity = android.view.Gravity.CENTER
            }

            val showAllBtn = com.google.android.material.button.MaterialButton(this).apply {
                text = "Show All Master Courses"
                isAllCaps = false
                setBackgroundColor(getColor(R.color.primary_blue))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 16, 0, 0)
                }
                setOnClickListener {
                    filterCatId = null
                    renderCourses(courseList)
                }
            }

            container.addView(emptyTv)
            container.addView(showAllBtn)
            binding.adminCoursesList.addView(container)
            return
        }

        for (course in courses) {
            val view = LayoutInflater.from(this).inflate(R.layout.item_admin_course, binding.adminCoursesList, false)

            view.findViewById<TextView>(R.id.tv_course_title).text = course.title
            view.findViewById<TextView>(R.id.tv_category_badge).text = "Category #${course.categoryId}"
            view.findViewById<TextView>(R.id.tv_slot_badge).text = "ID: ${course.id}"

            val coverImg = view.findViewById<ImageView>(R.id.iv_course_thumb)
            if (!course.imageUrl.isNullOrEmpty()) {
                val resId = resources.getIdentifier(course.imageUrl, "drawable", packageName)
                if (resId != 0) {
                    coverImg.setImageResource(resId)
                } else {
                    coverImg.setImageResource(R.drawable.course_python)
                }
            } else {
                coverImg.setImageResource(R.drawable.course_python)
            }

            // Edit / Configure Content Studio
            val openStudioAction = {
                val intent = Intent(this, AdminCourseStudioActivity::class.java)
                intent.putExtra("COURSE_ID", course.id)
                startActivity(intent)
            }

            view.findViewById<View>(R.id.btn_view_course).setOnClickListener { openStudioAction() }
            view.findViewById<View>(R.id.btn_quick_edit_course).setOnClickListener { openStudioAction() }

            // Delete Course
            view.findViewById<View>(R.id.btn_delete_course).setOnClickListener {
                confirmDeleteCourse(course)
            }

            binding.adminCoursesList.addView(view)
        }
    }

    private fun confirmDeleteCourse(course: Course) {
        AlertDialog.Builder(this)
            .setTitle("Delete Course Blueprint")
            .setMessage("Are you sure you want to delete '${course.title}'?")
            .setPositiveButton("Delete") { dialog, _ ->
                database.reference.child("courses").child(course.id).removeValue()
                    .addOnSuccessListener {
                        database.reference.child("categories").child(course.categoryId).child("courseCount").get()
                            .addOnSuccessListener { snapshot ->
                                val currentCount = snapshot.getValue(Int::class.java) ?: 1
                                val newCount = (currentCount - 1).coerceAtLeast(0)
                                database.reference.child("categories").child(course.categoryId).child("courseCount").setValue(newCount)
                            }

                        Toast.makeText(this, "Course '${course.title}' deleted & category count updated!", Toast.LENGTH_SHORT).show()
                    }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .show()
    }
}
