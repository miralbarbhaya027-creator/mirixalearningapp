package com.example.mirixa

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mirixa.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance("https://mirixa-b998b-default-rtdb.firebaseio.com")

        val currentUser = auth.currentUser
        if (currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        loadProfileData()
        
        binding.root.post {
            fetchUserInfo(currentUser.uid)
            fetchCategories()
            setupContinueLearning()
            setupFeaturedCourses()
            setupSearchBar()
        }

        binding.btnProfileTop.setOnClickListener { openLearnerPage(ProfileActivity::class.java) }
        binding.navProfile.setOnClickListener { openLearnerPage(ProfileActivity::class.java) }
        binding.navCategories.setOnClickListener { openLearnerPage(CategoriesActivity::class.java) }
        binding.tvSeeAllCategories.setOnClickListener { openLearnerPage(CategoriesActivity::class.java) }
        binding.tvSeeAllCourses.setOnClickListener { openLearnerPage(CategoriesActivity::class.java) }
        binding.navProgress.setOnClickListener { openLearnerPage(ProgressActivity::class.java) }
    }

    private fun openLearnerPage(targetClass: Class<*>) {
        if (this::class.java == targetClass) return
        val intent = Intent(this, targetClass).apply {
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        startActivity(intent)
        overridePendingTransition(0, 0)
    }

    override fun onResume() {
        super.onResume()
        loadProfileData()
    }

    private fun loadProfileData() {
        val prefs = getSharedPreferences("mirixa_prefs", MODE_PRIVATE)
        val cachedName = prefs.getString("user_first_name", "")
        val cachedAvatarIndex = prefs.getInt("user_avatar_index", 0)
        
        if (!cachedName.isNullOrEmpty()) {
            val greeting = getGreetingPrefix()
            binding.tvGreeting.text = "$greeting, $cachedName"
        } else {
            binding.tvGreeting.text = "Hi, Learner"
        }

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

    private fun getGreetingPrefix(): String {
        return when(java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)) {
            in 0..11 -> "Good Morning"
            in 12..16 -> "Good Afternoon"
            else -> "Good Evening"
        }
    }

    private fun setupSearchBar() {
        val courseData = mapOf(
            "Python Programming" to "c1", "JavaScript" to "c2", "Java Mastery" to "c3", "Kotlin for Android" to "c4", "C++ Systems" to "c5", "C Programming" to "c6",
            "Android Basics" to "a1", "XML Layouts" to "a2", "Jetpack Compose" to "a3", "Room Database" to "a4", "Firebase Cloud" to "a5",
            "HTML5 Mastery" to "w1", "CSS3 Design" to "w2", "React.js" to "w3", "PHP Server Side" to "w4", "Web Security" to "w5",
            "AI Fundamentals" to "ai1", "Machine Learning" to "ai2", "Deep Learning" to "ai3", "Neural Networks" to "ai4", "Prompt Engineering" to "ai5",
            "SQL Queries" to "db1", "MySQL Admin" to "db2", "SQLite Mobile" to "db3", "Cloud Firestore" to "db4"
        )

        val adapter = android.widget.ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, courseData.keys.toList())
        binding.etSearch.setAdapter(adapter)

        binding.etSearch.setOnItemClickListener { parent, _, position, _ ->
            val selection = parent.getItemAtPosition(position) as String
            performSearch(selection)
        }

        binding.etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                val query = binding.etSearch.text.toString().trim()
                if (query.isNotEmpty()) {
                    performSearch(query)
                }
                true
            } else {
                false
            }
        }
    }

    private fun performSearch(query: String) {
        val courseData = mapOf(
            "Python Programming" to "c1", "JavaScript" to "c2", "Java Mastery" to "c3", "Kotlin for Android" to "c4", "C++ Systems" to "c5", "C Programming" to "c6",
            "Android Basics" to "a1", "XML Layouts" to "a2", "Jetpack Compose" to "a3", "Room Database" to "a4", "Firebase Cloud" to "a5",
            "HTML5 Mastery" to "w1", "CSS3 Design" to "w2", "React.js" to "w3", "PHP Server Side" to "w4", "Web Security" to "w5",
            "AI Fundamentals" to "ai1", "Machine Learning" to "ai2", "Deep Learning" to "ai3", "Neural Networks" to "ai4", "Prompt Engineering" to "ai5",
            "SQL Queries" to "db1", "MySQL Admin" to "db2", "SQLite Mobile" to "db3", "Cloud Firestore" to "db4"
        )

        val match = courseData.keys.find { it.contains(query, ignoreCase = true) }

        if (match != null) {
            val intent = Intent(this, CourseDetailsActivity::class.java).apply {
                putExtra("COURSE_ID", courseData[match])
                putExtra("COURSE_TITLE", match)
            }
            startActivity(intent)
            binding.etSearch.setText("")
        } else {
            Toast.makeText(this, "No course found matching '$query'", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupContinueLearning() {
        val uid = auth.currentUser?.uid ?: return
        
        database.reference.child("user_progress").child(uid).get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val progressList = mutableListOf<CourseProgress>()
                    for (courseSnap in snapshot.children) {
                        val progress = courseSnap.getValue(CourseProgress::class.java)
                        if (progress != null && progress.getPercentage() < 100) {
                            progressList.add(progress)
                        }
                    }

                    if (progressList.isNotEmpty()) {
                        val activeProgress = progressList.last()
                        binding.cardContinueLearning.visibility = View.VISIBLE
                        updateContinueLearningUi(activeProgress)
                    } else {
                        binding.cardContinueLearning.visibility = View.GONE
                    }
                } else {
                    binding.cardContinueLearning.visibility = View.GONE
                }
            }
            .addOnFailureListener {
                binding.cardContinueLearning.visibility = View.GONE
            }
    }

    private fun updateContinueLearningUi(progress: CourseProgress) {
        binding.tvContinueCourseTitle.text = progress.courseTitle
        val completed = progress.getCompletedCount()
        binding.tvContinueModule.text = "Next Topic: ${getModuleName(completed)}"
        
        val percent = progress.getPercentage()
        binding.tvContinuePercent.text = "$percent% Complete"
        binding.continueProgressBar.progress = percent
        binding.tvContinueCount.text = "$completed/4 Topics"

        val resumeAction = {
            val intent = Intent(this, CourseDetailsActivity::class.java).apply {
                putExtra("COURSE_ID", progress.courseId)
                putExtra("COURSE_TITLE", progress.courseTitle)
            }
            startActivity(intent)
        }

        binding.btnResumeCourse.setOnClickListener { resumeAction() }
        binding.cardContinueLearning.setOnClickListener { resumeAction() }
    }

    private fun getModuleName(completed: Int): String {
        return when(completed) {
            0 -> "Video Lesson"
            1 -> "Study Notes"
            2 -> "Mind Map"
            3 -> "Knowledge Quiz"
            else -> "Course Mastery Achieved!"
        }
    }

    private fun setupFeaturedCourses() {
        val kotlinAction = {
            val intent = Intent(this, CourseDetailsActivity::class.java).apply {
                putExtra("COURSE_ID", "c4")
                putExtra("COURSE_TITLE", "Kotlin for Android")
                putExtra("CATEGORY_NAME", "Android Development")
            }
            startActivity(intent)
        }
        
        binding.cardFeaturedKotlin.setOnClickListener { kotlinAction() }
        binding.btnEnrollKotlin.setOnClickListener { kotlinAction() }

        val aiAction = {
            val intent = Intent(this, CourseDetailsActivity::class.java).apply {
                putExtra("COURSE_ID", "ai1")
                putExtra("COURSE_TITLE", "AI Fundamentals")
                putExtra("CATEGORY_NAME", "Artificial Intelligence")
            }
            startActivity(intent)
        }
        
        binding.cardFeaturedAi.setOnClickListener { aiAction() }
        binding.btnEnrollAi.setOnClickListener { aiAction() }
    }

    private fun fetchCategories() {
        database.reference.child("categories").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                binding.categoriesGrid.removeAllViews()
                if (!snapshot.exists() || !snapshot.hasChildren()) {
                    DatabaseSeeder.seedDatabase()
                    populateDefaultHomeCategories()
                    return
                }

                val categoryList = snapshot.children.mapNotNull { it.getValue(Category::class.java) }
                    .sortedBy { it.id.toIntOrNull() ?: 99 }

                if (categoryList.isEmpty()) {
                    populateDefaultHomeCategories()
                } else {
                    for (category in categoryList) {
                        addCategoryToHome(category)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                populateDefaultHomeCategories()
            }
        })
    }

    private fun populateDefaultHomeCategories() {
        binding.categoriesGrid.removeAllViews()
        val defaultList = listOf(
            Category("1", "Programming Languages", 6, "code"),
            Category("2", "Android Development", 5, "android"),
            Category("3", "Web Development", 5, "web"),
            Category("4", "Artificial Intelligence", 5, "ai"),
            Category("5", "Database Management", 4, "database")
        )
        for (cat in defaultList) {
            addCategoryToHome(cat)
        }
    }

    private fun addCategoryToHome(category: Category) {
        val view = LayoutInflater.from(this).inflate(R.layout.item_category_small, binding.categoriesGrid, false)
        val title = view.findViewById<TextView>(R.id.tv_category_name)
        val icon = view.findViewById<ImageView>(R.id.iv_category_icon)
        
        title.text = category.name

        when (category.iconType) {
            "code" -> icon.setImageResource(R.drawable.ic_code)
            "android" -> icon.setImageResource(R.drawable.ic_android)
            "web" -> icon.setImageResource(R.drawable.ic_web)
            "ai" -> icon.setImageResource(R.drawable.ic_ai)
            "database" -> icon.setImageResource(R.drawable.ic_database)
            else -> icon.setImageResource(R.drawable.ic_book)
        }
        
        val params = GridLayout.LayoutParams()
        params.width = 0
        params.height = GridLayout.LayoutParams.WRAP_CONTENT
        params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
        view.layoutParams = params

        view.setOnClickListener {
            val intent = Intent(this, CourseListActivity::class.java).apply {
                putExtra("CATEGORY_ID", category.id)
                putExtra("CATEGORY_NAME", category.name)
            }
            startActivity(intent)
        }
        
        binding.categoriesGrid.addView(view)
    }

    private fun fetchUserInfo(uid: String) {
        database.reference.child("users").child(uid).get()
            .addOnSuccessListener { snapshot ->
                val user = snapshot.getValue(User::class.java)
                if (user != null) {
                    val fName = user.firstName ?: "Learner"
                    getSharedPreferences("mirixa_prefs", MODE_PRIVATE).edit()
                        .putString("user_first_name", fName)
                        .putInt("user_avatar_index", user.avatarIndex)
                        .apply()
                    val greeting = getGreetingPrefix()
                    binding.tvGreeting.text = "$greeting, $fName"
                }
            }
    }
}
