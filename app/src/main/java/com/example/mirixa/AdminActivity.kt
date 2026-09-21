package com.example.mirixa

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mirixa.databinding.ActivityAdminBinding
import com.google.firebase.database.FirebaseDatabase

class AdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminBinding
    private val database = FirebaseDatabase.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }

        fetchStats()

        binding.btnQuickAction.setOnClickListener {
            Toast.makeText(this, "Triggering Platform Telemetry Sync...", Toast.LENGTH_SHORT).show()
        }

        // Action Buttons
        binding.btnViewUsers.setOnClickListener {
            Toast.makeText(this, "User Access Management locked.", Toast.LENGTH_SHORT).show()
        }

        binding.btnResetDb.setOnClickListener {
            startActivity(Intent(this, AdminCategoriesActivity::class.java))
            finish()
        }

        binding.btnAdminSettings.setOnClickListener {
            startActivity(Intent(this, AdminSettingsActivity::class.java))
            finish()
        }

        // Bottom Navigation
        binding.navAdminOverview.setOnClickListener {
            // Already here
        }

        binding.navAdminCategories.setOnClickListener {
            startActivity(Intent(this, AdminCategoriesActivity::class.java))
            finish()
        }

        binding.navAdminSettings.setOnClickListener {
            startActivity(Intent(this, AdminSettingsActivity::class.java))
            finish()
        }

        binding.navAdminUsers.setOnClickListener {
            Toast.makeText(this, "User Management coming soon.", Toast.LENGTH_SHORT).show()
        }

        binding.navAdminCourses.setOnClickListener {
            Toast.makeText(this, "Content Studio coming soon.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchStats() {
        database.reference.child("users").get().addOnSuccessListener { snapshot ->
            val total = snapshot.childrenCount
            binding.tvTotalUsers.text = total.toString()
            
            var students = 0
            var admins = 0
            for (child in snapshot.children) {
                val role = child.child("role").getValue(String::class.java)
                if (role == "Admin") admins++ else students++
            }
            binding.tvStudentCount.text = "$students Students"
            binding.tvAdminCount.text = "$admins Admins"
        }

        database.reference.child("courses").get().addOnSuccessListener {
            binding.tvActiveCourses.text = "${it.childrenCount} Courses"
        }

        database.reference.child("categories").get().addOnSuccessListener {
            binding.tvTotalCategories.text = "${it.childrenCount} Categories"
        }
    }
}
