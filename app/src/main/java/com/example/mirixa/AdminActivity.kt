package com.example.mirixa

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mirixa.databinding.ActivityAdminBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminBinding
    private val database = FirebaseDatabase.getInstance("https://mirixa-b998b-default-rtdb.firebaseio.com")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
        binding.root.post { fetchRealStats() }
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
        binding.btnBack.setOnClickListener { finish() }

        binding.btnProfileTop.setOnClickListener {
            openAdminPage(AdminSettingsActivity::class.java)
        }

        // Metrics & Module Navigation
        binding.cardMetricUsers.setOnClickListener { openAdminPage(AdminUsersActivity::class.java) }
        binding.cardModuleUsers.setOnClickListener { openAdminPage(AdminUsersActivity::class.java) }

        binding.cardMetricCourses.setOnClickListener { openAdminPage(AdminCoursesActivity::class.java) }
        binding.cardModuleCourses.setOnClickListener { openAdminPage(AdminCoursesActivity::class.java) }

        binding.cardMetricCategories.setOnClickListener { openAdminPage(AdminCategoriesActivity::class.java) }
        binding.cardModuleProfile.setOnClickListener { openAdminPage(AdminSettingsActivity::class.java) }

        binding.btnQuickAction.setOnClickListener {
            val intent = Intent(this, AdminCourseStudioActivity::class.java)
            startActivity(intent)
        }

        // Bottom Navigation Bar Clicks (Instant Switches)
        binding.navAdminDashboard.setOnClickListener {
            Toast.makeText(this, "You are on Admin Dashboard", Toast.LENGTH_SHORT).show()
        }
        binding.navAdminUsers.setOnClickListener { openAdminPage(AdminUsersActivity::class.java) }
        binding.navAdminCategory.setOnClickListener { openAdminPage(AdminCategoriesActivity::class.java) }
        binding.navAdminCourses.setOnClickListener { openAdminPage(AdminCoursesActivity::class.java) }
        binding.navAdminProfile.setOnClickListener { openAdminPage(AdminSettingsActivity::class.java) }
    }

    private fun fetchRealStats() {
        database.reference.child("users").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val total = snapshot.childrenCount
                binding.tvMetricUsers.text = total.toString()
                
                var studentCount = 0
                for (uSnap in snapshot.children) {
                    val role = uSnap.child("role").getValue(String::class.java)
                    if (role != "Admin") studentCount++
                }
                binding.tvStudentsCountLabel.text = "$studentCount Students"
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        database.reference.child("courses").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val total = snapshot.childrenCount
                binding.tvMetricCourses.text = "$total Courses"
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        database.reference.child("categories").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val total = snapshot.childrenCount
                binding.tvMetricCategories.text = "$total Categories"
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }
}
