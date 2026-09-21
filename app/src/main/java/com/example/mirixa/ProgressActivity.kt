package com.example.mirixa

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.mirixa.databinding.ActivityProgressBinding
import com.google.android.material.card.MaterialCardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ProgressActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProgressBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProgressBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        loadTopBarAvatar()
        setupNavigation()
        fetchEnrolledCourses()

        binding.btnBack.setOnClickListener { finish() }
        binding.btnProfileTop.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        binding.btnExploreCourses.setOnClickListener {
            startActivity(Intent(this, CategoriesActivity::class.java))
            finish()
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

    private fun setupNavigation() {
        binding.navHome.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
        binding.navCategories.setOnClickListener {
            startActivity(Intent(this, CategoriesActivity::class.java))
            finish()
        }
        binding.navProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
            finish()
        }
    }

    private fun fetchEnrolledCourses() {
        val uid = auth.currentUser?.uid ?: return

        database.reference.child("user_progress").child(uid)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    binding.enrolledCoursesContainer.removeAllViews()
                    var totalVideos = 0
                    var totalNotes = 0
                    var totalMindMaps = 0
                    var totalQuizzes = 0
                    var lastUpdatedProgress: CourseProgress? = null

                    if (snapshot.exists() && snapshot.hasChildren()) {
                        binding.cardEmptyState.visibility = View.GONE
                        binding.enrolledCoursesContainer.visibility = View.VISIBLE
                        
                        for (courseSnapshot in snapshot.children) {
                            val progress = courseSnapshot.getValue(CourseProgress::class.java)
                            progress?.let { 
                                addCourseToUi(it) 
                                if (it.videoCompleted) totalVideos++
                                if (it.notesCompleted) totalNotes++
                                if (it.mindMapCompleted) totalMindMaps++
                                if (it.quizCompleted) totalQuizzes++
                                lastUpdatedProgress = it 
                            }
                        }
                        
                        lastUpdatedProgress?.let { showContinueLearning(it) }
                    } else {
                        binding.cardEmptyState.visibility = View.VISIBLE
                        binding.enrolledCoursesContainer.visibility = View.GONE
                        binding.cardContinueLearningProgress.visibility = View.GONE
                    }
                    
                    binding.tvTotalVideos.text = totalVideos.toString()
                    binding.tvTotalNotes.text = totalNotes.toString()
                    binding.tvTotalMindmaps.text = totalMindMaps.toString()
                    binding.tvTotalQuizzes.text = totalQuizzes.toString()
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun showContinueLearning(progress: CourseProgress) {
        binding.cardContinueLearningProgress.visibility = View.VISIBLE
        binding.tvContinueTitleProgress.text = progress.courseTitle
        val percent = progress.getPercentage()
        binding.continuePbProgress.progress = percent
        binding.tvContinuePercentProgress.text = "$percent% Mastery Achieved"
        
        binding.btnResumeTopProgress.setOnClickListener {
            val intent = Intent(this, CourseDetailsActivity::class.java).apply {
                putExtra("COURSE_ID", progress.courseId)
                putExtra("COURSE_TITLE", progress.courseTitle)
            }
            startActivity(intent)
        }
    }

    private fun addCourseToUi(progress: CourseProgress) {
        val view = LayoutInflater.from(this).inflate(R.layout.item_enrolled_course, binding.enrolledCoursesContainer, false)
        
        val title = view.findViewById<TextView>(R.id.tv_course_title)
        val lessonsCount = view.findViewById<TextView>(R.id.tv_lessons_count)
        val progressPercent = view.findViewById<TextView>(R.id.tv_progress_percent)
        val progressBar = view.findViewById<com.google.android.material.progressindicator.LinearProgressIndicator>(R.id.course_progress)
        val image = view.findViewById<ImageView>(R.id.iv_course_bg)
        val btnResume = view.findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_resume_progress)

        title.text = progress.courseTitle
        lessonsCount.text = "${progress.getCompletedCount()}/4 Topics"
        
        val percent = progress.getPercentage()
        progressPercent.text = "$percent%"
        progressBar.progress = percent

        btnResume.setOnClickListener {
            val intent = Intent(this, CourseDetailsActivity::class.java).apply {
                putExtra("COURSE_ID", progress.courseId)
                putExtra("COURSE_TITLE", progress.courseTitle)
            }
            startActivity(intent)
        }

        // Dynamic Image Loading from Progress Data
        if (!progress.imageUrl.isNullOrEmpty()) {
            val resId = resources.getIdentifier(progress.imageUrl, "drawable", packageName)
            if (resId != 0) {
                image.setImageResource(resId)
            } else {
                image.setImageResource(R.drawable.ic_logo)
            }
        } else {
            image.setImageResource(R.drawable.ic_logo)
        }

        binding.enrolledCoursesContainer.addView(view)
    }
}
