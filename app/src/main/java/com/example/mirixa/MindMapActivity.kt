package com.example.mirixa

import android.content.Intent
import android.graphics.Matrix
import android.graphics.PointF
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mirixa.databinding.ActivityMindMapBinding
import com.google.firebase.database.FirebaseDatabase
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
        val courseTitle = intent.getStringExtra("COURSE_TITLE") ?: "Mind Map"
        binding.tvCourseTitle.text = courseTitle
        
        loadTopBarAvatar()

        if (courseId != null) {
            fetchMindMapFromDatabase(courseId, courseTitle)
        } else {
            setupImage(courseTitle, null)
        }

        // Initial setup for the image view
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

        setupBottomNav()
        
        binding.ivMindMap.post { resetMatrix() }
    }

    private fun fetchMindMapFromDatabase(courseId: String, fallbackTitle: String) {
        FirebaseDatabase.getInstance().reference.child("courses").child(courseId).get()
            .addOnSuccessListener { snapshot ->
                val course = snapshot.getValue(Course::class.java)
                currentCourse = course
                setupImage(course?.title ?: fallbackTitle, course?.mindmap?.image)
            }
            .addOnFailureListener {
                setupImage(fallbackTitle, null)
            }
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

    private fun setupImage(courseTitle: String, dbImageName: String?) {
        val imageName = dbImageName ?: run {
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
            "mindmap_$name"
        }

        val resId = resources.getIdentifier(imageName, "drawable", packageName)
        if (resId != 0) {
            binding.ivMindMap.setImageResource(resId)
        } else {
            binding.ivMindMap.setImageResource(R.drawable.ic_logo)
        }
        
        binding.ivMindMap.post { resetMatrix() }
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
        val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: return
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
