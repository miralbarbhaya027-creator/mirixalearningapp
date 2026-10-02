package com.example.mirixa

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.mirixa.databinding.ActivityAdminCategoriesBinding
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AdminCategoriesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminCategoriesBinding
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()
    private val categoryList = mutableListOf<Category>()

    private val defaultFallbackCategories = listOf(
        Category("1", "Programming Languages", 6, "code"),
        Category("2", "Android Development", 5, "android"),
        Category("3", "Web Development", 5, "web"),
        Category("4", "Artificial Intelligence", 5, "ai"),
        Category("5", "Database Management", 4, "database")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminCategoriesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener {
            startActivity(Intent(this, AdminActivity::class.java))
            finish()
        }

        if (auth.currentUser == null) {
            auth.signInWithEmailAndPassword("mirixalearning@gmail.com", "mirixa0365").addOnCompleteListener {
                fetchCategories()
            }
        } else {
            fetchCategories()
        }

        setupNavigation()

        binding.btnAddCategory.setOnClickListener {
            showCategoryDialog(null)
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
        binding.navAdminUsers.setOnClickListener {
            startActivity(Intent(this, AdminUsersActivity::class.java))
            finish()
        }
        binding.navAdminCourses.setOnClickListener {
            startActivity(Intent(this, AdminCoursesActivity::class.java))
            finish()
        }
        binding.navAdminSettings.setOnClickListener {
            startActivity(Intent(this, AdminSettingsActivity::class.java))
            finish()
        }
    }

    private fun fetchCategories() {
        database.reference.child("courses").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(courseSnapshot: DataSnapshot) {
                val categoryCourseCounts = mutableMapOf<String, Int>()
                val totalPublishedCourses = courseSnapshot.childrenCount.toInt()

                for (child in courseSnapshot.children) {
                    val catId = child.child("categoryId").getValue(String::class.java) ?: ""
                    if (catId.isNotEmpty()) {
                        categoryCourseCounts[catId] = (categoryCourseCounts[catId] ?: 0) + 1
                    }
                }

                database.reference.child("categories").addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        categoryList.clear()
                        if (snapshot.exists() && snapshot.childrenCount > 0L) {
                            for (catSnapshot in snapshot.children) {
                                try {
                                    val category = catSnapshot.getValue(Category::class.java)
                                    if (category != null && category.id.isNotEmpty()) {
                                        val realCount = categoryCourseCounts[category.id] ?: 0
                                        categoryList.add(category.copy(courseCount = realCount))
                                    } else {
                                        val id = catSnapshot.key ?: ""
                                        val name = catSnapshot.child("name").value?.toString() ?: "Category $id"
                                        val realCount = categoryCourseCounts[id] ?: 0
                                        val iconType = catSnapshot.child("iconType").value?.toString() ?: "code"
                                        categoryList.add(Category(id, name, realCount, iconType))
                                    }
                                } catch (_: Exception) {
                                    val id = catSnapshot.key ?: ""
                                    val name = catSnapshot.value?.toString() ?: "Category $id"
                                    val realCount = categoryCourseCounts[id] ?: 0
                                    categoryList.add(Category(id, name, realCount, "code"))
                                }
                            }
                        } else {
                            categoryList.addAll(defaultFallbackCategories.map { cat ->
                                cat.copy(courseCount = categoryCourseCounts[cat.id] ?: 0)
                            })
                        }

                        binding.tvActiveCatsCount.text = "${categoryList.size} Active Categories"
                        binding.tvTotalCoursesAlloc.text = "Total Courses: $totalPublishedCourses"
                        renderCategories()
                    }

                    override fun onCancelled(error: DatabaseError) {
                        if (categoryList.isEmpty()) {
                            categoryList.addAll(defaultFallbackCategories)
                            renderCategories()
                        }
                    }
                })
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun renderCategories() {
        binding.adminCategoriesList.removeAllViews()
        val count = categoryList.size
        binding.tvActiveCatsCount.text = "$count Active Categories"
        
        var totalCourses = 0
        for (category in categoryList) {
            addCategoryToUi(category)
            totalCourses += category.courseCount
        }
        binding.tvTotalCoursesAlloc.text = "Total Courses: $totalCourses"
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
            else -> {
                icon.setImageResource(R.drawable.ic_book)
                iconContainer.setCardBackgroundColor(Color.parseColor("#F1F4FF"))
            }
        }

        view.findViewById<MaterialButton>(R.id.btn_view_courses).setOnClickListener {
            val intent = Intent(this, AdminCoursesActivity::class.java).apply {
                putExtra("category_id", category.id)
                putExtra("category_name", category.name)
            }
            startActivity(intent)
        }

        view.findViewById<MaterialCardView>(R.id.btn_edit_cat).setOnClickListener {
            showCategoryDialog(category)
        }

        view.findViewById<MaterialCardView>(R.id.btn_delete_cat).setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Delete Category")
                .setMessage("Are you sure you want to delete '${category.name}'?")
                .setPositiveButton("Delete") { _, _ ->
                    database.reference.child("categories").child(category.id).removeValue()
                        .addOnSuccessListener {
                            Toast.makeText(this, "'${category.name}' deleted", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener {
                            categoryList.remove(category)
                            renderCategories()
                            Toast.makeText(this, "'${category.name}' deleted (Local)", Toast.LENGTH_SHORT).show()
                        }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        binding.adminCategoriesList.addView(view)
    }

    private fun showCategoryDialog(existingCategory: Category?) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_category, null)
        val tvTitle = dialogView.findViewById<TextView>(R.id.tv_category_dialog_title)
        val etName = dialogView.findViewById<TextInputEditText>(R.id.et_cat_name)
        val spinnerIcon = dialogView.findViewById<Spinner>(R.id.spinner_icon_type)

        val iconTypes = arrayOf("code", "android", "web", "ai", "database")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, iconTypes)
        spinnerIcon.adapter = adapter

        if (existingCategory != null) {
            tvTitle.text = "Edit Category"
            etName.setText(existingCategory.name)
            val iconIndex = iconTypes.indexOf(existingCategory.iconType)
            if (iconIndex >= 0) spinnerIcon.setSelection(iconIndex)
        } else {
            tvTitle.text = "Add Category"
        }

        AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton(if (existingCategory != null) "Update" else "Create") { _, _ ->
                val name = etName.text.toString().trim()
                val iconType = spinnerIcon.selectedItem.toString()

                if (name.isEmpty()) {
                    Toast.makeText(this, "Category Name is required", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val id = existingCategory?.id ?: "cat_${System.currentTimeMillis()}"
                val courseCount = existingCategory?.courseCount ?: 0

                val category = Category(
                    id = id,
                    name = name,
                    courseCount = courseCount,
                    iconType = iconType
                )

                database.reference.child("categories").child(id).setValue(category)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Category '$name' saved!", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        if (existingCategory != null) {
                            val index = categoryList.indexOfFirst { it.id == existingCategory.id }
                            if (index >= 0) categoryList[index] = category
                        } else {
                            categoryList.add(category)
                        }
                        renderCategories()
                        Toast.makeText(this, "Category '$name' saved! (Local)", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
