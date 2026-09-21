package com.example.mirixa

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mirixa.databinding.ActivityAdminCategoriesBinding
import com.google.android.material.card.MaterialCardView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AdminCategoriesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminCategoriesBinding
    private val database = FirebaseDatabase.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminCategoriesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }

        fetchCategories()
        setupNavigation()

        binding.btnAddCategory.setOnClickListener {
            Toast.makeText(this, "Add Category dialog coming soon", Toast.LENGTH_SHORT).show()
        }

        binding.btnProfileTop.setOnClickListener {
            startActivity(Intent(this, AdminSettingsActivity::class.java))
            finish()
        }
    }

    private fun setupNavigation() {
        binding.navAdminOverview.setOnClickListener {
            startActivity(Intent(this, AdminActivity::class.java))
            finish()
        }
        binding.navAdminSettings.setOnClickListener {
            startActivity(Intent(this, AdminSettingsActivity::class.java))
            finish()
        }
        binding.navAdminUsers.setOnClickListener {
            Toast.makeText(this, "User Management coming soon", Toast.LENGTH_SHORT).show()
        }
        binding.navAdminCourses.setOnClickListener {
            Toast.makeText(this, "Course Studio coming soon", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchCategories() {
        database.reference.child("categories").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                binding.adminCategoriesList.removeAllViews()
                val count = snapshot.childrenCount
                binding.tvActiveCatsCount.text = "$count Active Categories"
                
                var totalCourses = 0
                for (catSnapshot in snapshot.children) {
                    val category = catSnapshot.getValue(Category::class.java)
                    category?.let {
                        addCategoryToUi(it)
                        totalCourses += it.courseCount
                    }
                }
                binding.tvTotalCoursesAlloc.text = "Total Courses: $totalCourses"
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun addCategoryToUi(category: Category) {
        val view = LayoutInflater.from(this).inflate(R.layout.item_admin_category, binding.adminCategoriesList, false)
        
        view.findViewById<TextView>(R.id.tv_cat_name).text = category.name
        view.findViewById<TextView>(R.id.tv_course_count_badge).text = "${category.courseCount} Courses"
        
        val icon = view.findViewById<ImageView>(R.id.iv_cat_icon)
        val iconContainer = view.findViewById<MaterialCardView>(R.id.icon_container)

        when (category.iconType) {
            "code" -> {
                icon.setImageResource(R.drawable.ic_code)
                iconContainer.setCardBackgroundColor(Color.parseColor("#F1F4FF"))
            }
            "android" -> {
                icon.setImageResource(R.drawable.ic_android)
                iconContainer.setCardBackgroundColor(Color.parseColor("#F5F2FF"))
            }
            "web" -> {
                icon.setImageResource(R.drawable.ic_web)
                iconContainer.setCardBackgroundColor(Color.parseColor("#E0F2F1"))
            }
            "ai" -> {
                icon.setImageResource(R.drawable.ic_ai)
                iconContainer.setCardBackgroundColor(Color.parseColor("#FFF3E0"))
            }
            "database" -> {
                icon.setImageResource(R.drawable.ic_database)
                iconContainer.setCardBackgroundColor(Color.parseColor("#FFEBEE"))
            }
        }

        view.findViewById<MaterialCardView>(R.id.btn_edit_cat).setOnClickListener {
            Toast.makeText(this, "Editing ${category.name}", Toast.LENGTH_SHORT).show()
        }

        view.findViewById<MaterialCardView>(R.id.btn_delete_cat).setOnClickListener {
            Toast.makeText(this, "Delete protection active", Toast.LENGTH_SHORT).show()
        }

        binding.adminCategoriesList.addView(view)
    }
}
