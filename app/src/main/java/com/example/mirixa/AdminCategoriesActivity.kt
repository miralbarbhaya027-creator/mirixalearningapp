package com.example.mirixa

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.mirixa.databinding.ActivityAdminCategoriesBinding
import com.google.android.material.card.MaterialCardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AdminCategoriesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminCategoriesBinding
    private val database = FirebaseDatabase.getInstance("https://mirixa-b998b-default-rtdb.firebaseio.com")
    private val categoryList = mutableListOf<Category>()
    private var selectedIconType = "code"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminCategoriesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
        setupNavigation()
        binding.root.post { fetchCategoriesAndStats() }

        // Live Search
        binding.etSearchCategories.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterCategories(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun openAdminPage(targetClass: Class<*>) {
        if (this::class.java == targetClass) return
        val intent = Intent(this, targetClass).apply {
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        startActivity(intent)
        overridePendingTransition(0, 0)
    }

    private fun setupClickListeners() {
        binding.btnProfileTop.setOnClickListener {
            openAdminPage(AdminSettingsActivity::class.java)
        }

        binding.btnTopAddCategory.setOnClickListener {
            binding.nestedScrollCategories.smoothScrollTo(0, binding.cvQuickAddCard.top)
            binding.etNewCatName.requestFocus()
        }

        // Icon Selector Chips
        binding.btnIconCode.setOnClickListener { selectIconChip("code") }
        binding.btnIconAndroid.setOnClickListener { selectIconChip("android") }
        binding.btnIconWeb.setOnClickListener { selectIconChip("web") }
        binding.btnIconAi.setOnClickListener { selectIconChip("ai") }
        binding.btnIconDb.setOnClickListener { selectIconChip("database") }

        binding.btnSaveCategory.setOnClickListener {
            saveCategoryToFirebase()
        }

        binding.btnCancelCategory.setOnClickListener {
            binding.etNewCatName.text?.clear()
        }
    }

    private fun selectIconChip(type: String) {
        selectedIconType = type

        val activeBg = android.content.res.ColorStateList.valueOf(Color.parseColor("#2962FF"))
        val inactiveBg = android.content.res.ColorStateList.valueOf(Color.parseColor("#F1F4FF"))
        val activeText = Color.WHITE
        val inactiveText = Color.parseColor("#101820")

        binding.btnIconCode.backgroundTintList = if (type == "code") activeBg else inactiveBg
        binding.btnIconCode.setTextColor(if (type == "code") activeText else inactiveText)

        binding.btnIconAndroid.backgroundTintList = if (type == "android") activeBg else inactiveBg
        binding.btnIconAndroid.setTextColor(if (type == "android") activeText else inactiveText)

        binding.btnIconWeb.backgroundTintList = if (type == "web") activeBg else inactiveBg
        binding.btnIconWeb.setTextColor(if (type == "web") activeText else inactiveText)

        binding.btnIconAi.backgroundTintList = if (type == "ai") activeBg else inactiveBg
        binding.btnIconAi.setTextColor(if (type == "ai") activeText else inactiveText)

        binding.btnIconDb.backgroundTintList = if (type == "database") activeBg else inactiveBg
        binding.btnIconDb.setTextColor(if (type == "database") activeText else inactiveText)

        Toast.makeText(this, "Selected icon: $type", Toast.LENGTH_SHORT).show()
    }

    private fun saveCategoryToFirebase() {
        val name = binding.etNewCatName.text.toString().trim()
        if (name.isEmpty()) {
            Toast.makeText(this, "Please enter a category name", Toast.LENGTH_SHORT).show()
            return
        }

        val auth = FirebaseAuth.getInstance()
        if (auth.currentUser != null) {
            performFirebaseSave(name)
        } else {
            auth.signInAnonymously()
                .addOnSuccessListener { performFirebaseSave(name) }
                .addOnFailureListener {
                    auth.signInWithEmailAndPassword("mirixalearning@gmail.com", "mirixa0365")
                        .addOnSuccessListener { performFirebaseSave(name) }
                        .addOnFailureListener {
                            auth.createUserWithEmailAndPassword("mirixalearning@gmail.com", "mirixa0365")
                                .addOnCompleteListener { performFirebaseSave(name) }
                        }
                }
        }
    }

    private fun performFirebaseSave(name: String) {
        val maxExistingId = categoryList.mapNotNull { it.id.toIntOrNull() }.maxOrNull() ?: categoryList.size
        val nextId = (maxExistingId + 1).toString()
        val newCategory = Category(nextId, name, 0, selectedIconType)

        database.reference.child("categories").child(nextId).setValue(newCategory)
            .addOnSuccessListener {
                Toast.makeText(this, "Category #$nextId '$name' saved to Firebase Database!", Toast.LENGTH_SHORT).show()
                binding.etNewCatName.text?.clear()
            }
    }

    private fun setupNavigation() {
        binding.navAdminDashboard.setOnClickListener { openAdminPage(AdminActivity::class.java) }
        binding.navAdminCourses.setOnClickListener { openAdminPage(AdminCoursesActivity::class.java) }
        binding.navAdminCategory.setOnClickListener {
            Toast.makeText(this, "You are on Category Management", Toast.LENGTH_SHORT).show()
        }
        binding.navAdminUsers.setOnClickListener { openAdminPage(AdminUsersActivity::class.java) }
        binding.navAdminProfile.setOnClickListener { openAdminPage(AdminSettingsActivity::class.java) }
    }

    private fun fetchCategoriesAndStats() {
        database.reference.child("categories").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                categoryList.clear()
                if (snapshot.exists() && snapshot.hasChildren()) {
                    for (catSnapshot in snapshot.children) {
                        try {
                            val cat = catSnapshot.getValue(Category::class.java)
                            if (cat != null && !cat.name.isNullOrEmpty()) {
                                categoryList.add(cat)
                            }
                        } catch (e: Exception) {
                            val id = catSnapshot.child("id").getValue(String::class.java) ?: catSnapshot.key ?: ""
                            val name = catSnapshot.child("name").getValue(String::class.java) ?: ""
                            val count = catSnapshot.child("courseCount").getValue(Int::class.java) ?: 0
                            val icon = catSnapshot.child("iconType").getValue(String::class.java) ?: "code"
                            if (name.isNotEmpty()) {
                                categoryList.add(Category(id, name, count, icon))
                            }
                        }
                    }
                }

                ensureCoreCategoriesExist()

                val count = categoryList.size
                binding.tvTotalCategoriesStat.text = count.toString()
                binding.tvActiveTracksStat.text = count.toString()

                renderCategories(categoryList)
            }

            override fun onCancelled(error: DatabaseError) {
                ensureCoreCategoriesExist()
            }
        })

        // Fetch Total Courses Count
        database.reference.child("courses").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val courseCount = snapshot.childrenCount
                binding.tvLinkedCoursesStat.text = courseCount.toString()
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun ensureCoreCategoriesExist() {
        val coreCategories = mapOf(
            "1" to Category("1", "Programming Languages", 6, "code"),
            "2" to Category("2", "Android Development", 5, "android"),
            "3" to Category("3", "Web Development", 5, "web"),
            "4" to Category("4", "Artificial Intelligence", 5, "ai"),
            "5" to Category("5", "Database Management", 4, "database")
        )

        val missingMap = mutableMapOf<String, Any>()
        for ((id, cat) in coreCategories) {
            if (categoryList.none { it.id == id }) {
                missingMap[id] = cat
                categoryList.add(cat)
            }
        }

        if (missingMap.isNotEmpty()) {
            database.reference.child("categories").updateChildren(missingMap)
        }

        categoryList.sortBy { it.id.toIntOrNull() ?: 99 }

        val count = categoryList.size
        binding.tvTotalCategoriesStat.text = count.toString()
        binding.tvActiveTracksStat.text = count.toString()

        renderCategories(categoryList)
    }

    private fun filterCategories(query: String) {
        val filtered = categoryList.filter {
            it.name.contains(query, ignoreCase = true) ||
            it.id.contains(query, ignoreCase = true) ||
            it.iconType.contains(query, ignoreCase = true)
        }
        renderCategories(filtered)
    }

    private fun renderCategories(categories: List<Category>) {
        binding.adminCategoriesList.removeAllViews()

        for (category in categories) {
            val view = LayoutInflater.from(this).inflate(R.layout.item_admin_category, binding.adminCategoriesList, false)

            view.findViewById<TextView>(R.id.tv_cat_name).text = category.name
            view.findViewById<TextView>(R.id.tv_course_count_badge).text = "${category.courseCount} Courses"

            val iconImg = view.findViewById<ImageView>(R.id.iv_cat_icon)
            val iconCard = view.findViewById<MaterialCardView>(R.id.icon_container)

            when (category.iconType) {
                "code" -> {
                    iconImg.setImageResource(R.drawable.ic_code)
                    iconCard.setCardBackgroundColor(Color.parseColor("#E8EEFF"))
                }
                "android" -> {
                    iconImg.setImageResource(R.drawable.ic_android)
                    iconCard.setCardBackgroundColor(Color.parseColor("#E8EEFF"))
                }
                "web" -> {
                    iconImg.setImageResource(R.drawable.ic_web)
                    iconCard.setCardBackgroundColor(Color.parseColor("#E0F2F1"))
                }
                "ai" -> {
                    iconImg.setImageResource(R.drawable.ic_ai)
                    iconCard.setCardBackgroundColor(Color.parseColor("#FFF3E0"))
                }
                "database" -> {
                    iconImg.setImageResource(R.drawable.ic_database)
                    iconCard.setCardBackgroundColor(Color.parseColor("#FFEBEE"))
                }
                else -> {
                    iconImg.setImageResource(R.drawable.ic_book)
                    iconCard.setCardBackgroundColor(Color.parseColor("#E8EEFF"))
                }
            }

            // View Linked Courses Button
            view.findViewById<View>(R.id.btn_view_courses).setOnClickListener {
                val intent = Intent(this, AdminCoursesActivity::class.java)
                intent.putExtra("FILTER_CAT_ID", category.id)
                startActivity(intent)
            }

            // Edit Category
            view.findViewById<View>(R.id.btn_edit_cat).setOnClickListener {
                showEditCategoryDialog(category)
            }

            // Delete Category
            view.findViewById<View>(R.id.btn_delete_cat).setOnClickListener {
                confirmDeleteCategory(category)
            }

            binding.adminCategoriesList.addView(view)
        }
    }

    private fun showEditCategoryDialog(category: Category) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Edit Category #${category.id}")

        val input = EditText(this)
        input.setText(category.name)
        builder.setView(input)

        builder.setPositiveButton("Update") { dialog, _ ->
            val newName = input.text.toString().trim()
            if (newName.isNotEmpty()) {
                database.reference.child("categories").child(category.id).child("name").setValue(newName)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Category updated to '$newName'!", Toast.LENGTH_SHORT).show()
                    }
            }
            dialog.dismiss()
        }
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
        builder.show()
    }

    private fun confirmDeleteCategory(category: Category) {
        AlertDialog.Builder(this)
            .setTitle("Delete Category")
            .setMessage("Are you sure you want to delete '${category.name}'?")
            .setPositiveButton("Delete") { dialog, _ ->
                database.reference.child("categories").child(category.id).removeValue()
                    .addOnSuccessListener {
                        Toast.makeText(this, "Category '${category.name}' deleted!", Toast.LENGTH_SHORT).show()
                    }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .show()
    }
}
