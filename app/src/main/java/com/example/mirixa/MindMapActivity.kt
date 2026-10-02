package com.example.mirixa

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.PointF
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.mirixa.databinding.ActivityMindMapBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.io.ByteArrayOutputStream
import kotlin.math.sqrt

class MindMapActivity : AppCompatActivity(), View.OnTouchListener {

    private lateinit var binding: ActivityMindMapBinding
    private val matrix = Matrix()
    private val savedMatrix = Matrix()
    private val start = PointF()
    private val mid = PointF()
    private var oldDist = 1f
    private var mode = NONE
    private var currentCourse: Course? = null
    private var currentCourseId: String? = null

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            try {
                val inputStream = contentResolver.openInputStream(it)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                if (bitmap != null) {
                    val base64 = bitmapToBase64(bitmap)
                    saveAndApplyMindMapImage(base64)
                }
            } catch (_: Exception) {
                Toast.makeText(this, "Failed to load image from gallery", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap: Bitmap? ->
        bitmap?.let {
            val base64 = bitmapToBase64(it)
            saveAndApplyMindMapImage(base64)
        }
    }

    companion object {
        private const val NONE = 0
        private const val DRAG = 1
        private const val ZOOM = 2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMindMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val courseId = intent.getStringExtra("COURSE_ID")
        currentCourseId = courseId
        val courseTitle = intent.getStringExtra("COURSE_TITLE") ?: "Mind Map"
        binding.tvCourseTitle.text = courseTitle

        loadTopBarAvatar()

        if (courseId != null) {
            fetchMindMapFromDatabase(courseId, courseTitle)
        } else {
            setupImage(courseTitle, null)
        }

        binding.ivMindMap.scaleType = ImageView.ScaleType.MATRIX
        binding.ivMindMap.setOnTouchListener(this)

        binding.btnBack.setOnClickListener { finish() }

        binding.btnProfileTop.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        binding.btnZoomIn.setOnClickListener { zoom(1.2f) }
        binding.btnZoomOut.setOnClickListener { zoom(0.8f) }

        binding.btnFitScreen.setOnClickListener { resetMatrix() }

        binding.btnMarkMindmapComplete.setOnClickListener {
            markMindMapAsComplete(courseTitle)
        }

        binding.btnEditMindmap.setOnClickListener {
            showEditMindMapDialog()
        }

        setupBottomNav()

        binding.ivMindMap.post { resetMatrix() }
    }

    private fun fetchMindMapFromDatabase(courseId: String, fallbackTitle: String) {
        FirebaseDatabase.getInstance().reference.child("courses").child(courseId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val course = snapshot.getValue(Course::class.java)
                    currentCourse = course
                    setupImage(course?.title ?: fallbackTitle, course?.mindmap?.image)
                }

                override fun onCancelled(error: DatabaseError) {
                    setupImage(fallbackTitle, null)
                }
            })
    }

    private fun saveAndApplyMindMapImage(imageStr: String) {
        val courseId = currentCourseId
        if (courseId != null) {
            FirebaseDatabase.getInstance().reference.child("courses").child(courseId)
                .child("mindmap").child("image").setValue(imageStr)
                .addOnSuccessListener {
                    Toast.makeText(this, "Mind Map updated successfully!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    setupImage(binding.tvCourseTitle.text.toString(), imageStr)
                }
        } else {
            setupImage(binding.tvCourseTitle.text.toString(), imageStr)
        }
    }

    private fun showEditMindMapDialog() {
        val options = arrayOf(
            "🖼️ Choose Mind Map from Gallery",
            "📸 Take Photo with Camera",
            "🎨 Select Preset Mind Map",
            "🗑️ Clear Mind Map"
        )
        AlertDialog.Builder(this)
            .setTitle("Edit Mind Map Diagram")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> galleryLauncher.launch("image/*")
                    1 -> cameraLauncher.launch(null)
                    2 -> showPresetMindMapPicker()
                    3 -> saveAndApplyMindMapImage("")
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showPresetMindMapPicker() {
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
                saveAndApplyMindMapImage(presets[which])
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

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

    private fun setupImage(courseTitle: String, dbImageName: String?) {
        if (!dbImageName.isNullOrEmpty()) {
            if (dbImageName.startsWith("data:image") || (dbImageName.length > 100 && !dbImageName.startsWith("http"))) {
                val bitmap = base64ToBitmap(dbImageName)
                if (bitmap != null) {
                    binding.ivMindMap.setImageBitmap(bitmap)
                    binding.ivMindMap.post { resetMatrix() }
                    return
                }
            } else {
                val resId = resources.getIdentifier(dbImageName, "drawable", packageName)
                if (resId != 0) {
                    binding.ivMindMap.setImageResource(resId)
                    binding.ivMindMap.post { resetMatrix() }
                    return
                }
            }
        }

        var name = courseTitle.substringBefore(":").trim().lowercase().replace(" ", "_")
        name = when (name) {
            "javascript" -> "js"
            "c++" -> "cpp"
            "machine_learning" -> "ml"
            "deep_learning" -> "dl"
            "ai_fundamentals" -> "ai_f"
            "neural_networks" -> "neural"
            "prompt_engineering" -> "prompt"
            "room_database" -> "room"
            "firebase_firestore" -> "firestore"
            "sqlite" -> "sqllite"
            "mysql" -> "mysql"
            "sql" -> "sql"
            else -> name
        }
        val imageName = "mindmap_$name"

        val resId = resources.getIdentifier(imageName, "drawable", packageName)
        if (resId != 0) {
            binding.ivMindMap.setImageResource(resId)
        } else {
            binding.ivMindMap.setImageResource(R.drawable.ic_logo)
        }

        binding.ivMindMap.post { resetMatrix() }
    }

    override fun onResume() {
        super.onResume()
        loadTopBarAvatar()
    }

    private fun loadTopBarAvatar() {
        val prefs = getSharedPreferences("mirixa_prefs", MODE_PRIVATE)
        val cachedAvatarIndex = prefs.getInt("user_avatar_index", 0)
        val avatarResources = listOf(
            R.drawable.ic_person,
            R.drawable.ic_school,
            R.drawable.ic_bulb,
            R.drawable.ic_star,
            R.drawable.ic_sparkle
        )
        if (cachedAvatarIndex in avatarResources.indices) {
            binding.ivProfileIconTop.setImageResource(avatarResources[cachedAvatarIndex])
        }
    }

    private fun resetMatrix() {
        val drawable = binding.ivMindMap.drawable ?: return
        val viewWidth = binding.ivMindMap.width.toFloat()
        val viewHeight = binding.ivMindMap.height.toFloat()
        val drawableWidth = drawable.intrinsicWidth.toFloat()
        val drawableHeight = drawable.intrinsicHeight.toFloat()

        if (viewWidth <= 0 || viewHeight <= 0) return

        matrix.reset()
        val scale = (viewWidth / drawableWidth).coerceAtMost(viewHeight / drawableHeight)
        matrix.postScale(scale, scale)
        val dx = (viewWidth - drawableWidth * scale) / 2f
        val dy = (viewHeight - drawableHeight * scale) / 2f
        matrix.postTranslate(dx, dy)
        binding.ivMindMap.imageMatrix = matrix
    }

    private fun zoom(scale: Float) {
        val view = binding.ivMindMap
        val centerX = view.width / 2f
        val centerY = view.height / 2f
        matrix.postScale(scale, scale, centerX, centerY)
        view.imageMatrix = matrix
    }

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        val view = v as ImageView
        v.parent.requestDisallowInterceptTouchEvent(true)

        when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                savedMatrix.set(matrix)
                start.set(event.x, event.y)
                mode = DRAG
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                oldDist = spacing(event)
                if (oldDist > 10f) {
                    savedMatrix.set(matrix)
                    midPoint(mid, event)
                    mode = ZOOM
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                mode = NONE
                v.parent.requestDisallowInterceptTouchEvent(false)
            }
            MotionEvent.ACTION_MOVE -> {
                if (mode == DRAG) {
                    matrix.set(savedMatrix)
                    matrix.postTranslate(event.x - start.x, event.y - start.y)
                } else if (mode == ZOOM) {
                    val newDist = spacing(event)
                    if (newDist > 10f) {
                        matrix.set(savedMatrix)
                        val scale = newDist / oldDist
                        matrix.postScale(scale, scale, mid.x, mid.y)
                    }
                }
            }
        }
        view.imageMatrix = matrix
        return true
    }

    private fun spacing(event: MotionEvent): Float {
        val x = event.getX(0) - event.getX(1)
        val y = event.getY(0) - event.getY(1)
        return sqrt((x * x + y * y).toDouble()).toFloat()
    }

    private fun midPoint(point: PointF, event: MotionEvent) {
        point.set((event.getX(0) + event.getX(1)) / 2, (event.getY(0) + event.getY(1)) / 2)
    }

    private fun setupBottomNav() {
        binding.navHome.setOnClickListener { startActivity(Intent(this, MainActivity::class.java)); finish() }
        binding.navCategories.setOnClickListener { startActivity(Intent(this, CategoriesActivity::class.java)); finish() }
        binding.navProgress.setOnClickListener { startActivity(Intent(this, ProgressActivity::class.java)); finish() }
        binding.navProfile.setOnClickListener { startActivity(Intent(this, ProfileActivity::class.java)); finish() }
    }

    private fun markMindMapAsComplete(courseTitle: String) {
        val course = currentCourse ?: return
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val courseKey = courseTitle.substringBefore(":").trim().replace(" ", "_").lowercase()
        val ref = FirebaseDatabase.getInstance().reference.child("user_progress").child(uid).child(courseKey)

        ref.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                ref.child("mindMapCompleted").setValue(true)
            } else {
                val newProgress = CourseProgress(
                    courseId = course.id,
                    courseTitle = course.title,
                    mindMapCompleted = true,
                    imageUrl = course.imageUrl
                )
                ref.setValue(newProgress)
            }
            Toast.makeText(this, "Mind Map marked as complete!", Toast.LENGTH_SHORT).show()
        }
    }
}
