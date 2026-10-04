package com.example.mirixa

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mirixa.databinding.ActivityCourseDetailsBinding
import com.google.firebase.database.FirebaseDatabase

class CourseDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCourseDetailsBinding
    private val database = FirebaseDatabase.getInstance("https://mirixa-b998b-default-rtdb.firebaseio.com")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCourseDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val courseId = intent.getStringExtra("COURSE_ID") ?: "c1"
        val courseTitle = intent.getStringExtra("COURSE_TITLE") ?: "Course Details"
        val categoryName = intent.getStringExtra("CATEGORY_NAME") ?: "Learning Path"

        binding.tvHeaderCategory.text = categoryName
        binding.tvCourseFullTitle.text = courseTitle

        loadTopBarAvatar()
        fetchCourseDetails(courseId, courseTitle)

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

    private fun fetchCourseDetails(id: String, fallbackTitle: String) {
        database.reference.child("courses").child(id).get()
            .addOnSuccessListener { snapshot ->
                val course = snapshot.getValue(Course::class.java)
                if (course != null) {
                    binding.tvCourseFullTitle.text = course.title
                    if (!course.imageUrl.isNullOrEmpty()) {
                        val resId = resources.getIdentifier(course.imageUrl, "drawable", packageName)
                        if (resId != 0) {
                            binding.ivCourseCover.setImageResource(resId)
                        } else {
                            binding.ivCourseCover.setImageResource(R.drawable.course_python)
                        }
                    }
                }
            }
            .addOnFailureListener {
                binding.ivCourseCover.setImageResource(R.drawable.course_python)
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
}
