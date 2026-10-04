package com.example.mirixa

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mirixa.databinding.ActivityStudyNotesBinding
import com.google.android.material.card.MaterialCardView
import com.google.firebase.database.FirebaseDatabase

class StudyNotesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStudyNotesBinding
    private val database = FirebaseDatabase.getInstance("https://mirixa-b998b-default-rtdb.firebaseio.com")
    private var currentTopicRow: LinearLayout? = null
    private var topicsInCurrentRow = 0
    private var currentCourse: Course? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStudyNotesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val courseId = intent.getStringExtra("COURSE_ID") ?: "c1"
        val courseTitle = intent.getStringExtra("COURSE_TITLE") ?: "Study Notes"
        
        loadTopBarAvatar()
        fetchCourseDetails(courseId, courseTitle)

        binding.btnBack.setOnClickListener { finish() }
        binding.btnProfileTop.setOnClickListener { startActivity(Intent(this, ProfileActivity::class.java)) }
        binding.btnMarkNotesComplete.setOnClickListener { markNotesAsComplete(courseTitle) }

        setupBottomNav()
    }

    private fun fetchCourseDetails(id: String, fallbackTitle: String) {
        database.reference.child("courses").child(id).get()
            .addOnSuccessListener { snapshot ->
                val course = snapshot.getValue(Course::class.java)
                if (course != null && course.note != null) {
                    currentCourse = course
                    setupNotes(course)
                } else {
                    setupFallbackNotes(fallbackTitle)
                }
            }
            .addOnFailureListener {
                setupFallbackNotes(fallbackTitle)
            }
    }

    private fun setupNotes(course: Course) {
        val noteData = course.note ?: return
        
        binding.tvNotesTitle.text = "${course.title}\nStudy Guide"
        binding.tvNotesDescription.text = course.description
        binding.tvPdfHint.text = "Comprehensive Notes"
        binding.tvPdfAction.text = "View Full PDF Resource"

        if (!course.imageUrl.isNullOrEmpty()) {
            val resId = resources.getIdentifier(course.imageUrl, "drawable", packageName)
            if (resId != 0) {
                binding.ivHeader.setImageResource(resId)
            }
        }

        binding.topicsContainer.removeAllViews()
        noteData.topics.forEach { topic ->
            addTopic(topic.title, topic.description, getIconForType(topic.iconType), "#E8EAF6")
        }

        binding.btnViewPdf.setOnClickListener {
            if (noteData.pdfUrl.isNotEmpty()) {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(noteData.pdfUrl))
                startActivity(Intent.createChooser(intent, "View Notes"))
            }
        }
    }

    private fun setupFallbackNotes(title: String) {
        binding.tvNotesTitle.text = "$title\nStudy Guide"
        binding.tvNotesDescription.text = "Detailed syllabus overview and core reference guide for $title."
        binding.tvPdfHint.text = "Comprehensive Notes"
        binding.tvPdfAction.text = "View Full PDF Resource"

        binding.topicsContainer.removeAllViews()
        addTopic("Fundamentals", "Core concepts and building blocks.", R.drawable.ic_bulb, "#E8EAF6")
        addTopic("Implementation", "Applying principles in real applications.", R.drawable.ic_code, "#E8EAF6")
        addTopic("Best Practices", "Industry standards and design patterns.", R.drawable.ic_star, "#E8EAF6")

        binding.btnViewPdf.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.guru99.com/python-tutorials.html"))
            startActivity(Intent.createChooser(intent, "View Notes"))
        }
    }

    private fun getIconForType(type: String): Int {
        return when (type) {
            "code" -> R.drawable.ic_code
            "edit" -> R.drawable.ic_edit
            "bulb" -> R.drawable.ic_bulb
            "star" -> R.drawable.ic_star
            "categories" -> R.drawable.ic_categories
            "progress" -> R.drawable.ic_progress
            else -> R.drawable.ic_code
        }
    }

    private fun addTopic(title: String, description: String, iconRes: Int, bgColor: String) {
        if (currentTopicRow == null || topicsInCurrentRow == 2) {
            currentTopicRow = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                weightSum = 2.0f
            }
            binding.topicsContainer.addView(currentTopicRow)
            topicsInCurrentRow = 0
        }

        val view = LayoutInflater.from(this).inflate(R.layout.item_notes_topic_grid, currentTopicRow, false)
        view.findViewById<TextView>(R.id.tv_topic_title).text = title
        view.findViewById<TextView>(R.id.tv_topic_description).text = description
        view.findViewById<ImageView>(R.id.iv_topic_icon).setImageResource(iconRes)
        view.findViewById<MaterialCardView>(R.id.icon_container).setCardBackgroundColor(Color.parseColor(bgColor))
        
        currentTopicRow?.addView(view)
        topicsInCurrentRow++
    }

    private fun setupBottomNav() {
        binding.navHome.setOnClickListener { startActivity(Intent(this, MainActivity::class.java)); finish() }
        binding.navCategories.setOnClickListener { startActivity(Intent(this, CategoriesActivity::class.java)); finish() }
        binding.navProgress.setOnClickListener { startActivity(Intent(this, ProgressActivity::class.java)); finish() }
        binding.navProfile.setOnClickListener { startActivity(Intent(this, ProfileActivity::class.java)); finish() }
    }

    private fun markNotesAsComplete(courseTitle: String) {
        val course = currentCourse ?: return
        val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: return
        val courseKey = courseTitle.substringBefore(":").trim().replace(" ", "_").lowercase()
        val ref = database.reference.child("user_progress").child(uid).child(courseKey)
        
        ref.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                ref.child("notesCompleted").setValue(true)
            } else {
                val newProgress = CourseProgress(
                    courseId = course.id,
                    courseTitle = course.title,
                    notesCompleted = true,
                    imageUrl = course.imageUrl
                )
                ref.setValue(newProgress)
            }
            Toast.makeText(this, "Notes marked as complete!", Toast.LENGTH_SHORT).show()
        }
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
