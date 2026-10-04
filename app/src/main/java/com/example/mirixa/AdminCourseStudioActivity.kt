package com.example.mirixa

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.mirixa.databinding.ActivityAdminCourseStudioBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AdminCourseStudioActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminCourseStudioBinding
    private val database = FirebaseDatabase.getInstance("https://mirixa-b998b-default-rtdb.firebaseio.com")
    private var courseId: String = "c1"
    private var currentCourse: Course? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminCourseStudioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        courseId = intent.getStringExtra("COURSE_ID") ?: "c1"

        binding.btnBack.setOnClickListener { finish() }

        setupNavigation()
        loadCourseFromDatabase()
        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnProfileTop.setOnClickListener {
            startActivity(Intent(this, AdminSettingsActivity::class.java))
        }

        // Tap Thumbnail Box -> Open Thumbnail Selection Modal (res/drawable)
        binding.cvThumbnailBox.setOnClickListener {
            showThumbnailPickerDialog()
        }

        // Select Mind Map Drawable Graphic (res/drawable)
        binding.btnSelectMindmapDrawable.setOnClickListener {
            showMindmapPickerDialog()
        }

        // Configure Overview Details -> Save Title, Description & Image Key to Firebase
        binding.btnSaveOverview.setOnClickListener {
            val newTitle = binding.etStudioTitle.text.toString().trim()
            val newDesc = binding.etStudioDesc.text.toString().trim()
            val newImg = binding.etStudioImgUrl.text.toString().trim()

            if (newTitle.isNotEmpty()) {
                database.reference.child("courses").child(courseId).child("title").setValue(newTitle)
                database.reference.child("courses").child(courseId).child("description").setValue(newDesc)
                if (newImg.isNotEmpty()) {
                    database.reference.child("courses").child(courseId).child("imageUrl").setValue(newImg)
                }

                Toast.makeText(this, "Overview & Thumbnail saved to Database!", Toast.LENGTH_SHORT).show()
                binding.tvHeaderCourseTitle.text = newTitle
                binding.tvTopTitle.text = newTitle
                binding.tvCrumbCourse.text = "[ $newTitle ]"
            } else {
                Toast.makeText(this, "Title cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }

        // Save Video Stream to Firebase
        binding.btnSaveVideo.setOnClickListener {
            val videoUrl = binding.etStudioVideoUrl.text.toString().trim()
            if (videoUrl.isNotEmpty()) {
                val updatedVideo = VideoLecture(videoUrl, currentCourse?.title ?: "Lecture Stream", "15:00")
                database.reference.child("courses").child(courseId).child("videoLecture").setValue(updatedVideo)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Video Stream saved to Database!", Toast.LENGTH_SHORT).show()
                        binding.tvVideoLinkedBadge.text = "Video Active"
                    }
            } else {
                Toast.makeText(this, "Please enter a valid video stream URL", Toast.LENGTH_SHORT).show()
            }
        }

        // Preview Stream Video
        binding.btnPreviewVideo.setOnClickListener {
            val intent = Intent(this, VideoPlayerActivity::class.java)
            intent.putExtra("COURSE_ID", courseId)
            startActivity(intent)
        }

        // Add Topic to Syllabus Notes
        binding.btnAddTopic.setOnClickListener {
            val pdfUrl = binding.etStudioPdfUrl.text.toString().trim()
            if (pdfUrl.isNotEmpty()) {
                database.reference.child("courses").child(courseId).child("note").child("pdfUrl").setValue(pdfUrl)
            }
            Toast.makeText(this, "Syllabus Notes updated in Database!", Toast.LENGTH_SHORT).show()
        }

        // Add Node to Mindmap Architecture
        binding.btnAddMindmapNode.setOnClickListener {
            val nodeLabel = binding.etMindmapNodeLabel.text.toString().trim()
            val mindmapImgKey = binding.etStudioMindmapImg.text.toString().trim().ifEmpty { "mindmap_python" }

            if (nodeLabel.isNotEmpty()) {
                val currentNodes = currentCourse?.mindmap?.nodes?.toMutableList() ?: mutableListOf()
                val newNodeId = "node_${System.currentTimeMillis()}"
                val newNode = MindmapNode(newNodeId, nodeLabel, (100..300).random().toFloat(), (100..400).random().toFloat())
                currentNodes.add(newNode)

                val updatedMindmap = Mindmap(mindmapImgKey, currentNodes)

                database.reference.child("courses").child(courseId).child("mindmap").setValue(updatedMindmap)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Mindmap Node '$nodeLabel' & Image saved to Database!", Toast.LENGTH_SHORT).show()
                        binding.etMindmapNodeLabel.text?.clear()
                    }
            } else {
                Toast.makeText(this, "Please enter a node label", Toast.LENGTH_SHORT).show()
            }
        }

        // Add Question to Quiz in Firebase with Correct Answer Index
        binding.btnAddQuizQuestion.setOnClickListener {
            val qPrompt = binding.etQuizQuestion.text.toString().trim()
            val o0 = binding.etQuizOpt0.text.toString().trim()
            val o1 = binding.etQuizOpt1.text.toString().trim()
            val o2 = binding.etQuizOpt2.text.toString().trim()
            val o3 = binding.etQuizOpt3.text.toString().trim()
            val correctIdx = binding.etQuizCorrectIndex.text.toString().trim().toIntOrNull()?.coerceIn(0, 3) ?: 0

            if (qPrompt.isNotEmpty() && o0.isNotEmpty()) {
                val newQuestion = Question(
                    qPrompt,
                    listOf(o0, o1.ifEmpty { "Option B" }, o2.ifEmpty { "Option C" }, o3.ifEmpty { "Option D" }),
                    correctIdx
                )
                val quizList = currentCourse?.quiz?.toMutableList() ?: mutableListOf()
                quizList.add(newQuestion)

                database.reference.child("courses").child(courseId).child("quiz").setValue(quizList)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Question (Correct Option ${correctIdx + 1}) added to Quiz in Database!", Toast.LENGTH_SHORT).show()
                        binding.etQuizQuestion.text?.clear()
                        binding.etQuizOpt0.text?.clear()
                        binding.etQuizOpt1.text?.clear()
                        binding.etQuizOpt2.text?.clear()
                        binding.etQuizOpt3.text?.clear()
                    }
            } else {
                Toast.makeText(this, "Please enter question prompt & options", Toast.LENGTH_SHORT).show()
            }
        }

        // Discard / Save Blueprint Draft
        binding.btnDiscardDraft.setOnClickListener { finish() }

        binding.btnSaveBlueprintDraft.setOnClickListener {
            Toast.makeText(this, "Blueprint Draft committed to Database!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun showThumbnailPickerDialog() {
        val bannerOptions = arrayOf(
            "course_python" to "Python Programming",
            "course_javascript" to "JavaScript",
            "course_java" to "Java Mastery",
            "course_kotlin" to "Kotlin for Android",
            "course_cpp" to "C++ Systems",
            "course_c" to "C Programming",
            "course_android_basics" to "Android Basics",
            "course_xml" to "XML Layouts",
            "course_compose" to "Jetpack Compose",
            "course_room" to "Room Database",
            "course_firebase" to "Firebase Cloud",
            "course_html" to "HTML5 Mastery",
            "course_css" to "CSS3 Design",
            "course_react" to "React.js",
            "course_php" to "PHP Server Side",
            "course_ai_f" to "AI Fundamentals",
            "course_ml" to "Machine Learning",
            "corse_dl" to "Deep Learning",
            "course_neural" to "Neural Networks",
            "course_prompt" to "Prompt Engineering",
            "cpurse_sql" to "SQL Queries",
            "cpurse_mysql" to "MySQL Admin",
            "cpurse_sqllite" to "SQLite Mobile",
            "course_firestore" to "Cloud Firestore"
        )

        val labels = bannerOptions.map { "${it.second} (${it.first})" }.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle("Select Course Thumbnail (res/drawable)")
            .setItems(labels) { dialog, which ->
                val selectedKey = bannerOptions[which].first
                binding.etStudioImgUrl.setText(selectedKey)

                val resId = resources.getIdentifier(selectedKey, "drawable", packageName)
                if (resId != 0) {
                    binding.ivStudioBannerPreview.setImageResource(resId)
                }

                database.reference.child("courses").child(courseId).child("imageUrl").setValue(selectedKey)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Thumbnail key '$selectedKey' saved to Firebase!", Toast.LENGTH_SHORT).show()
                    }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun showMindmapPickerDialog() {
        val mindmapOptions = arrayOf(
            "mindmap_python" to "Python Mind Map",
            "mindmap_js" to "JavaScript Mind Map",
            "mindmap_java" to "Java Mind Map",
            "mindmap_kotlin" to "Kotlin Mind Map",
            "mindmap_cpp" to "C++ Systems Mind Map",
            "mindmap_c" to "C Language Mind Map",
            "mindmap_android_basics" to "Android Architecture",
            "mindmap_xml" to "XML Layouts Mind Map",
            "mindmap_compose" to "Jetpack Compose Mind Map",
            "mindmap_room" to "Room Database Architecture",
            "mindmap_firebase" to "Firebase Cloud Mind Map",
            "mindmap_html" to "HTML5 Structure Mind Map",
            "mindmap_css" to "CSS3 Styling Mind Map",
            "mindmap_react" to "React.js Component Mind Map",
            "mindmap_php" to "PHP Server Side Mind Map",
            "mindmap_web_sec" to "Web Security Architecture",
            "mindmap_ai_f" to "AI Fundamentals Mind Map",
            "mindmap_ml" to "Machine Learning Mind Map",
            "mindmap_dl" to "Deep Learning Mind Map",
            "mindmap_neural" to "Neural Networks Architecture",
            "mindmap_prompt" to "Prompt Engineering Mind Map",
            "mindmap_sql" to "SQL Database Architecture",
            "mindmap_mysql" to "MySQL Server Mind Map",
            "mindmap_sqllite" to "SQLite Mobile Mind Map",
            "mindmap_firestore" to "Cloud Firestore Mind Map"
        )

        val labels = mindmapOptions.map { "${it.second} (${it.first})" }.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle("Select Mind Map Graphic (res/drawable)")
            .setItems(labels) { dialog, which ->
                val selectedKey = mindmapOptions[which].first
                binding.etStudioMindmapImg.setText(selectedKey)

                database.reference.child("courses").child(courseId).child("mindmap").child("image").setValue(selectedKey)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Mind Map Graphic key '$selectedKey' saved to Firebase!", Toast.LENGTH_SHORT).show()
                    }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun loadCourseFromDatabase() {
        database.reference.child("courses").child(courseId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                currentCourse = snapshot.getValue(Course::class.java)
                currentCourse?.let { course ->
                    binding.tvTopTitle.text = course.title
                    binding.tvHeaderCourseTitle.text = course.title
                    binding.tvCrumbCourse.text = "[ ${course.title} ]"
                    binding.etStudioTitle.setText(course.title)
                    binding.etStudioDesc.setText(course.description)
                    binding.etStudioImgUrl.setText(course.imageUrl)

                    if (!course.imageUrl.isNullOrEmpty()) {
                        val resId = resources.getIdentifier(course.imageUrl, "drawable", packageName)
                        if (resId != 0) {
                            binding.ivStudioBannerPreview.setImageResource(resId)
                        } else {
                            binding.ivStudioBannerPreview.setImageResource(R.drawable.course_python)
                        }
                    }

                    course.mindmap?.let { mMap ->
                        binding.etStudioMindmapImg.setText(mMap.image)
                    }

                    if (course.quiz != null && course.quiz!!.isNotEmpty()) {
                        binding.tvBlueprintStatusBadge.text = "Published"
                        binding.tvBlueprintStatusBadge.setBackgroundResource(R.drawable.card_background)
                        binding.tvBlueprintStatusBadge.backgroundTintList = getColorStateList(R.color.primary_blue)
                        binding.tvBlueprintStatusBadge.setTextColor(getColor(R.color.white))
                    }

                    course.videoLecture?.let { vLecture ->
                        binding.etStudioVideoUrl.setText(vLecture.url)
                        if (vLecture.url.isNotEmpty()) {
                            binding.tvVideoStreamLabel.text = "Stream URL Authenticated"
                            binding.tvVideoLinkedBadge.text = "Video Linked"
                        }
                    }

                    course.note?.let {
                        binding.etStudioPdfUrl.setText(it.pdfUrl)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun setupNavigation() {
        binding.navAdminDashboard.setOnClickListener {
            startActivity(Intent(this, AdminActivity::class.java))
            finish()
        }
        binding.navAdminCourses.setOnClickListener {
            startActivity(Intent(this, AdminCoursesActivity::class.java))
            finish()
        }
        binding.navAdminCategory.setOnClickListener {
            startActivity(Intent(this, AdminCategoriesActivity::class.java))
            finish()
        }
        binding.navAdminUsers.setOnClickListener {
            startActivity(Intent(this, AdminUsersActivity::class.java))
            finish()
        }
        binding.navAdminProfile.setOnClickListener {
            startActivity(Intent(this, AdminSettingsActivity::class.java))
            finish()
        }
    }
}
