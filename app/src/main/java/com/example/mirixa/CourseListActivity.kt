package com.example.mirixa

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mirixa.databinding.ActivityCourseListBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class CourseListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCourseListBinding
    private lateinit var database: FirebaseDatabase
    private var categoryId: String = "1"
    private var categoryName: String = "Programming Languages"
    private var userEnrolledCourses = mutableSetOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCourseListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = FirebaseDatabase.getInstance("https://mirixa-b998b-default-rtdb.firebaseio.com")
        categoryId = intent.getStringExtra("CATEGORY_ID") ?: "1"
        categoryName = intent.getStringExtra("CATEGORY_NAME") ?: "Programming Languages"

        binding.tvCategoryTitle.text = categoryName
        
        binding.tvCategorySubtitle.text = when(categoryId) {
            "1" -> "Master the mother languages of modern software."
            "2" -> "Build high-performance mobile experiences."
            "3" -> "Create the responsive frontend and robust backend of the web."
            "4" -> "Harness the power of neural networks and predictive modeling."
            "5" -> "Design and optimize structured and unstructured data storage."
            else -> "Choose a course to start your learning journey in $categoryName."
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
        val targetCatId = categoryId

        binding.loadingSpinner.visibility = View.VISIBLE
        database.reference.child("courses")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    binding.loadingSpinner.visibility = View.GONE
                    binding.coursesContainer.removeAllViews()

                    if (snapshot.exists() && snapshot.hasChildren()) {
                        // Strict client-side filtering by categoryId
                        val coursesList = snapshot.children.mapNotNull { it.getValue(Course::class.java) }
                            .filter { it.categoryId == targetCatId }
                            .sortedBy { it.id }

                        if (coursesList.isNotEmpty()) {
                            for (course in coursesList) {
                                addCourseToUi(course)
                            }
                        } else {
                            showEmptyCategoryState()
                        }
                    } else {
                        showEmptyCategoryState()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    binding.loadingSpinner.visibility = View.GONE
                    showEmptyCategoryState()
                }
            })
    }

    private fun showEmptyCategoryState() {
        binding.coursesContainer.removeAllViews()

        val card = com.google.android.material.card.MaterialCardView(this).apply {
            radius = 24f * resources.displayMetrics.density
            cardElevation = 2f * resources.displayMetrics.density
            setCardBackgroundColor(Color.WHITE)
            strokeWidth = 0
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 24, 0, 24)
            }
        }

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(32, 48, 32, 48)
        }

        val iconCard = com.google.android.material.card.MaterialCardView(this).apply {
            radius = 28f * resources.displayMetrics.density
            setCardBackgroundColor(Color.parseColor("#F1F4FF"))
            strokeWidth = 0
            layoutParams = LinearLayout.LayoutParams(56.dpToPx(), 56.dpToPx())
        }

        val iconIv = ImageView(this).apply {
            setImageResource(R.drawable.ic_book)
            setColorFilter(Color.parseColor("#2962FF"))
            setPadding(14.dpToPx(), 14.dpToPx(), 14.dpToPx(), 14.dpToPx())
        }
        iconCard.addView(iconIv)

        val emptyTv = TextView(this).apply {
            text = "No courses added to $categoryName yet."
            textSize = 17f
            setTextColor(Color.parseColor("#101820"))
            setTypeface(null, android.graphics.Typeface.BOLD)
            gravity = Gravity.CENTER
            setPadding(0, 16, 0, 0)
        }

        val subTv = TextView(this).apply {
            text = "Admin can add courses for $categoryName from the Admin Panel."
            textSize = 13f
            setTextColor(Color.parseColor("#8899A6"))
            gravity = Gravity.CENTER
            setPadding(0, 8, 0, 0)
        }

        container.addView(iconCard)
        container.addView(emptyTv)
        container.addView(subTv)
        card.addView(container)

        binding.coursesContainer.addView(card)
    }

    private fun Int.dpToPx(): Int {
        return (this * resources.displayMetrics.density).toInt()
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
        if (!course.imageUrl.isNullOrEmpty()) {
            val resId = resources.getIdentifier(course.imageUrl, "drawable", packageName)
            if (resId != 0) {
                image.setImageResource(resId)
            } else {
                image.setImageResource(R.drawable.app_logo)
            }
        } else {
            image.setImageResource(R.drawable.app_logo)
        }

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
}
