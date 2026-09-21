package com.example.mirixa

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.example.mirixa.databinding.ActivityCategoriesBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class CategoriesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCategoriesBinding
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoriesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = FirebaseDatabase.getInstance()

        loadTopBarAvatar()
        setupNavigation()
        fetchCategories()

        binding.btnProfileTop.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
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
        binding.navProgress.setOnClickListener {
            startActivity(Intent(this, ProgressActivity::class.java))
            finish()
        }
        binding.navProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
            finish()
        }
    }

    private fun fetchCategories() {
        binding.loadingSpinner.visibility = View.VISIBLE
        database.reference.child("categories").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                binding.loadingSpinner.visibility = View.GONE
                
                if (!snapshot.exists() || snapshot.childrenCount < 5.toLong()) {
                    DatabaseSeeder.seedDatabase()
                    return 
                }

                // Check if the courses node is modern (has nested quiz/video and imageUrl)
                database.reference.child("courses").child("c1").get().addOnSuccessListener { courseSnapshot ->
                    val course = courseSnapshot.getValue(Course::class.java)
                    if (course == null || course.quiz == null || course.imageUrl.isNullOrEmpty()) {
                        DatabaseSeeder.seedDatabase()
                    }
                }

                binding.categoriesContainer.removeAllViews()
                val sortedCategories = snapshot.children.mapNotNull { it.getValue(Category::class.java) }
                    .sortedBy { it.id }
                
                for (category in sortedCategories) {
                    addCategoryToUi(category)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                binding.loadingSpinner.visibility = View.GONE
            }
        })
    }

    private fun addCategoryToUi(category: Category) {
        val view = LayoutInflater.from(this).inflate(R.layout.item_category, binding.categoriesContainer, false)
        
        val title = view.findViewById<TextView>(R.id.category_title)
        val count = view.findViewById<TextView>(R.id.category_count)
        val icon = view.findViewById<ImageView>(R.id.category_icon)
        val iconContainer = view.findViewById<CardView>(R.id.icon_container)
        val cardBackground = view.findViewById<CardView>(R.id.card_background)

        title.text = category.name
        count.text = "${category.courseCount} Specialized Courses"

        when (category.iconType) {
            "code" -> {
                icon.setImageResource(R.drawable.ic_code)
                iconContainer.setCardBackgroundColor(android.graphics.Color.parseColor("#2962FF"))
                cardBackground.setCardBackgroundColor(android.graphics.Color.parseColor("#F2F5FF"))
            }
            "android" -> {
                icon.setImageResource(R.drawable.ic_android)
                iconContainer.setCardBackgroundColor(android.graphics.Color.parseColor("#A100FF"))
                cardBackground.setCardBackgroundColor(android.graphics.Color.parseColor("#F7F2FF"))
            }
            "web" -> {
                icon.setImageResource(R.drawable.ic_web)
                iconContainer.setCardBackgroundColor(android.graphics.Color.parseColor("#00BFA5"))
                cardBackground.setCardBackgroundColor(android.graphics.Color.parseColor("#E0F2F1"))
            }
            "ai" -> {
                icon.setImageResource(R.drawable.ic_ai)
                iconContainer.setCardBackgroundColor(android.graphics.Color.parseColor("#FF6D00"))
                cardBackground.setCardBackgroundColor(android.graphics.Color.parseColor("#FFF3E0"))
            }
            "database" -> {
                icon.setImageResource(R.drawable.ic_database)
                iconContainer.setCardBackgroundColor(android.graphics.Color.parseColor("#D32F2F"))
                cardBackground.setCardBackgroundColor(android.graphics.Color.parseColor("#FFEBEE"))
            }
            else -> {
                icon.setImageResource(R.drawable.ic_book)
                iconContainer.setCardBackgroundColor(android.graphics.Color.parseColor("#2962FF"))
                cardBackground.setCardBackgroundColor(android.graphics.Color.parseColor("#F2F5FF"))
            }
        }

        view.setOnClickListener {
            val intent = Intent(this, CourseListActivity::class.java).apply {
                putExtra("CATEGORY_ID", category.id)
                putExtra("CATEGORY_NAME", category.name)
            }
            startActivity(intent)
        }

        binding.categoriesContainer.addView(view)
    }

    private fun initializeDefaultCategories() {
        val categories = mapOf(
            "1" to Category("1", "Programming Languages", 6, "code"),
            "2" to Category("2", "Android Development", 5, "android"),
            "3" to Category("3", "Web Development", 4, "web"),
            "4" to Category("4", "Artificial Intelligence", 5, "ai"),
            "5" to Category("5", "Database Management", 4, "database")
        )
        database.reference.child("categories").setValue(categories)
    }
}
