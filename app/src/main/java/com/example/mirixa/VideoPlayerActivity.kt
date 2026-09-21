package com.example.mirixa

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mirixa.databinding.ActivityVideoPlayerBinding
import com.google.firebase.database.FirebaseDatabase

class VideoPlayerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVideoPlayerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVideoPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val courseId = intent.getStringExtra("COURSE_ID")
        val courseTitle = intent.getStringExtra("COURSE_TITLE") ?: "Video Lesson"

        loadTopBarAvatar()

        if (courseId != null) {
            fetchVideoData(courseId)
        } else {
            Toast.makeText(this, "Video not found", Toast.LENGTH_SHORT).show()
            finish()
        }

        binding.btnBack.setOnClickListener { finish() }
        binding.btnProfileTop.setOnClickListener { startActivity(Intent(this, ProfileActivity::class.java)) }
        setupBottomNav()
    }

    private fun fetchVideoData(id: String) {
        FirebaseDatabase.getInstance().reference.child("courses").child(id).get()
            .addOnSuccessListener { snapshot ->
                val course = snapshot.getValue(Course::class.java)
                if (course != null && course.videoLecture != null) {
                    setupVideoUI(course)
                } else {
                    Toast.makeText(this, "No video available", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
    }

    private fun setupVideoUI(course: Course) {
        val video = course.videoLecture ?: return
        binding.tvVideoTitle.text = video.title
        binding.tvDuration.text = "${video.duration} mins"
        binding.tvOverview.text = course.description

        val playAction = {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(video.url))
            startActivity(intent)
            markVideoComplete(course)
        }

        binding.btnPlay.setOnClickListener { playAction() }
        binding.btnWatchYoutube.setOnClickListener { playAction() }
        
        // Auto-mark as entered
        markVideoComplete(course)
    }

    private fun markVideoComplete(course: Course) {
        val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: return
        val key = course.title.substringBefore(":").trim().replace(" ", "_").lowercase()
        val ref = FirebaseDatabase.getInstance().reference.child("user_progress").child(uid).child(key)
        
        ref.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                ref.child("videoCompleted").setValue(true)
            } else {
                val newProgress = CourseProgress(
                    courseId = course.id,
                    courseTitle = course.title,
                    videoCompleted = true,
                    imageUrl = course.imageUrl
                )
                ref.setValue(newProgress)
            }
        }
    }

    private fun setupBottomNav() {
        binding.navHome.setOnClickListener { startActivity(Intent(this, MainActivity::class.java)); finish() }
        binding.navCategories.setOnClickListener { startActivity(Intent(this, CategoriesActivity::class.java)); finish() }
        binding.navProgress.setOnClickListener { startActivity(Intent(this, ProgressActivity::class.java)); finish() }
        binding.navProfile.setOnClickListener { startActivity(Intent(this, ProfileActivity::class.java)); finish() }
    }

    private fun loadTopBarAvatar() {
        val prefs = getSharedPreferences("mirixa_prefs", MODE_PRIVATE)
        val cachedAvatarIndex = prefs.getInt("user_avatar_index", 0)
        val avatarResources = listOf(R.drawable.ic_person, R.drawable.ic_school, R.drawable.ic_bulb, R.drawable.ic_star, R.drawable.ic_sparkle)
        if (cachedAvatarIndex in avatarResources.indices) binding.ivProfileIconTop.setImageResource(avatarResources[cachedAvatarIndex])
    }
}
