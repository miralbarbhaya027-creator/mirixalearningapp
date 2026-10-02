package com.example.mirixa

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.mirixa.databinding.ActivityCourseDetailsBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class CourseDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCourseDetailsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCourseDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val courseId = intent.getStringExtra("COURSE_ID") ?: ""
        val courseTitle = intent.getStringExtra("COURSE_TITLE") ?: "Course"
        val categoryName = intent.getStringExtra("CATEGORY_NAME") ?: "Learning Path"

        binding.tvHeaderCategory.text = categoryName
        binding.tvCourseFullTitle.text = courseTitle

        loadTopBarAvatar()
        if (courseId.isNotEmpty()) {
            fetchCourseDetails(courseId)
        }

        binding.btnBack.setOnClickListener { finish() }
        binding.btnProfileTop.setOnClickListener { startActivity(Intent(this, ProfileActivity::class.java)) }

        binding.itemVideoLessons.setOnClickListener {
            startActivity(Intent(this, VideoPlayerActivity::class.java).apply {
                putExtra("COURSE_ID", courseId)
                putExtra("COURSE_TITLE", courseTitle)
            })
        }

        binding.itemStudyNotes.setOnClickListener {
            startActivity(Intent(this, StudyNotesActivity::class.java).apply {
                putExtra("COURSE_ID", courseId)
                putExtra("COURSE_TITLE", courseTitle)
            })
        }

        binding.itemMindMap.setOnClickListener {
            startActivity(Intent(this, MindMapActivity::class.java).apply {
                putExtra("COURSE_ID", courseId)
                putExtra("COURSE_TITLE", courseTitle)
            })
        }

        binding.itemQuiz.setOnClickListener {
            startActivity(Intent(this, QuizActivity::class.java).apply {
                putExtra("COURSE_ID", courseId)
                putExtra("COURSE_TITLE", courseTitle)
            })
        }
    }

    private fun fetchCourseDetails(id: String) {
        FirebaseDatabase.getInstance().reference.child("courses").child(id)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val course = snapshot.getValue(Course::class.java)
                        if (course != null) {
                            binding.tvCourseFullTitle.text = course.title
                            loadCourseImageToView(course.imageUrl, binding.ivCourseCover, R.drawable.course_python)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
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

    private fun loadCourseImageToView(imageUrl: String?, imageView: ImageView, defaultResId: Int) {
        if (imageUrl.isNullOrEmpty()) {
            imageView.setImageResource(defaultResId)
            return
        }
        if (imageUrl.startsWith("data:image") || (imageUrl.length > 100 && !imageUrl.startsWith("http"))) {
            val bitmap = base64ToBitmap(imageUrl)
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap)
            } else {
                imageView.setImageResource(defaultResId)
            }
        } else {
            val resId = resources.getIdentifier(imageUrl, "drawable", packageName)
            if (resId != 0) {
                imageView.setImageResource(resId)
            } else {
                imageView.setImageResource(defaultResId)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadTopBarAvatar()
    }

    private fun loadTopBarAvatar() {
        val prefs = getSharedPreferences("mirixa_prefs", MODE_PRIVATE)
        val cachedAvatarIndex = prefs.getInt("user_avatar_index", 0)
        val avatarResources = listOf(R.drawable.ic_person, R.drawable.ic_school, R.drawable.ic_bulb, R.drawable.ic_star, R.drawable.ic_sparkle)
        if (cachedAvatarIndex in avatarResources.indices) binding.ivProfileIconTop.setImageResource(avatarResources[cachedAvatarIndex])
    }
}
