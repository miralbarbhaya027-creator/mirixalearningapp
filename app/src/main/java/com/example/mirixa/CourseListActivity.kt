package com.example.mirixa

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.example.mirixa.databinding.ActivityCourseListBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class CourseListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCourseListBinding
    private lateinit var database: FirebaseDatabase
    private var categoryId: String? = null
    private var categoryName: String? = null
    private var userEnrolledCourses = mutableSetOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCourseListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = FirebaseDatabase.getInstance()
        categoryId = intent.getStringExtra("CATEGORY_ID")
        categoryName = intent.getStringExtra("CATEGORY_NAME")

        binding.tvCategoryTitle.text = categoryName ?: "Courses"
        
        binding.tvCategorySubtitle.text = when(categoryId) {
            "1" -> "Master the mother languages of modern software."
            "2" -> "Build high-performance mobile experiences."
            "3" -> "Create the responsive frontend and robust backend of the web."
            "4" -> "Harness the power of neural networks and predictive modeling."
            "5" -> "Design and optimize structured and unstructured data storage."
            else -> "Choose a course to start your learning journey."
        }

        loadTopBarAvatar()
        setupNavigation()
        fetchUserEnrollments()

        binding.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.btnProfileTop.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        loadTopBarAvatar()
        fetchUserEnrollments()
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
        binding.navProgress.setOnClickListener {
            startActivity(Intent(this, ProgressActivity::class.java))
            finish()
        }
        binding.navProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
            finish()
        }
    }

    private fun fetchUserEnrollments() {
        val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: return
        database.reference.child("user_progress").child(uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    userEnrolledCourses.clear()
                    if (snapshot.exists()) {
                        for (courseSnapshot in snapshot.children) {
                            userEnrolledCourses.add(courseSnapshot.key ?: "")
                        }
                    }
                    fetchCourses()
                }
                override fun onCancelled(error: DatabaseError) {
                    fetchCourses()
                }
            })
    }

    private fun fetchCourses() {
        if (categoryId == null) return

        binding.loadingSpinner.visibility = View.VISIBLE
        database.reference.child("courses").orderByChild("categoryId").equalTo(categoryId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    binding.loadingSpinner.visibility = View.GONE
                    binding.coursesContainer.removeAllViews()

                    if (snapshot.exists() && snapshot.hasChildren()) {
                        val coursesList = snapshot.children.mapNotNull { it.getValue(Course::class.java) }
                            .sortedBy { it.id }
                        
                        for (course in coursesList) {
                            addCourseToUi(course)
                        }
                    } else {
                        // Categories handles seeding now for cleaner logic
                        Toast.makeText(this@CourseListActivity, "Fetching courses...", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    binding.loadingSpinner.visibility = View.GONE
                }
            })
    }

    private fun addCourseToUi(course: Course) {
        val view = LayoutInflater.from(this).inflate(R.layout.item_course, binding.coursesContainer, false)
        
        val title = view.findViewById<TextView>(R.id.tv_course_title)
        val description = view.findViewById<TextView>(R.id.tv_course_description)
        val image = view.findViewById<ImageView>(R.id.iv_course_image)
        val btnEnroll = view.findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_enroll)

        title.text = course.title
        description.text = course.description
        
        val courseKey = course.title.substringBefore(":").trim().replace(" ", "_").lowercase()
        if (userEnrolledCourses.contains(courseKey)) {
            btnEnroll.text = "Continue Learning"
            btnEnroll.setBackgroundColor(Color.parseColor("#E8EAF6"))
            btnEnroll.setTextColor(Color.parseColor("#2962FF"))
            btnEnroll.setIconResource(R.drawable.ic_progress)
            btnEnroll.setIconTintResource(R.color.primary_blue)
        } else {
            btnEnroll.text = "Enroll Now"
            btnEnroll.setBackgroundColor(Color.parseColor("#2962FF"))
            btnEnroll.setTextColor(Color.WHITE)
            btnEnroll.setIconResource(R.drawable.ic_chevron_right)
            btnEnroll.setIconTintResource(R.color.white)
        }

        // Dynamic Image Loading from Database
        loadCourseImageToView(course.imageUrl, image)

        btnEnroll.setOnClickListener {
            val intent = Intent(this, CourseDetailsActivity::class.java).apply {
                putExtra("COURSE_ID", course.id)
                putExtra("COURSE_TITLE", course.title)
                putExtra("CATEGORY_NAME", categoryName)
            }
            startActivity(intent)
        }

        binding.coursesContainer.addView(view)
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
            imageView.setImageResource(R.drawable.ic_logo)
            return
        }
        if (imageUrl.startsWith("data:image") || (imageUrl.length > 100 && !imageUrl.startsWith("http"))) {
            val bitmap = base64ToBitmap(imageUrl)
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap)
            } else {
                imageView.setImageResource(R.drawable.ic_logo)
            }
        } else {
            val resId = resources.getIdentifier(imageUrl, "drawable", packageName)
            if (resId != 0) {
                imageView.setImageResource(resId)
            } else {
                imageView.setImageResource(R.drawable.ic_logo)
            }
        }
    }
}
