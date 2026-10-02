package com.example.mirixa

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.mirixa.databinding.ActivityAdminBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminBinding
    private val database = FirebaseDatabase.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fetchStats()

        binding.btnQuickAction.setOnClickListener {
            showQuickActionMenu()
        }

        // Action Buttons
        binding.btnViewUsers.setOnClickListener {
            startActivity(Intent(this, AdminUsersActivity::class.java))
        }

        binding.btnResetDb.setOnClickListener {
            startActivity(Intent(this, AdminCoursesActivity::class.java))
        }

        binding.btnAdminSettings.setOnClickListener {
            startActivity(Intent(this, AdminSettingsActivity::class.java))
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
            startActivity(Intent(this, AdminUsersActivity::class.java))
            finish()
        }

        binding.navAdminCourses.setOnClickListener {
            startActivity(Intent(this, AdminCoursesActivity::class.java))
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        fetchStats()
    }

    private fun fetchStats() {
        database.reference.child("users").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val total = snapshot.childrenCount
                binding.tvTotalUsers.text = total.toString()

                var students = 0
                var admins = 0
                for (child in snapshot.children) {
                    val role = child.child("role").getValue(String::class.java)
                    if (role.equals("Admin", ignoreCase = true)) admins++ else students++
                }
                binding.tvStudentCount.text = "$students Students"
                binding.tvAdminCount.text = "$admins Admins"
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        database.reference.child("courses").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                binding.tvActiveCourses.text = "${snapshot.childrenCount} Courses"
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        database.reference.child("categories").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                binding.tvTotalCategories.text = "${snapshot.childrenCount} Categories"
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun showQuickActionMenu() {
        val options = arrayOf("Re-Seed / Reset Database", "Open User Management", "Open Course Studio", "Open Category Management")
        AlertDialog.Builder(this)
            .setTitle("Admin Quick Actions")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> confirmReseedDatabase()
                    1 -> startActivity(Intent(this, AdminUsersActivity::class.java))
                    2 -> startActivity(Intent(this, AdminCoursesActivity::class.java))
                    3 -> startActivity(Intent(this, AdminCategoriesActivity::class.java))
                }
            }
            .setNegativeButton("Close", null)
            .show()
    }

    private fun confirmReseedDatabase() {
        AlertDialog.Builder(this)
            .setTitle("Reset / Re-Seed Database")
            .setMessage("This will populate default categories, courses, notes, and quizzes into Firebase. Existing custom data might be overwritten. Proceed?")
            .setPositiveButton("Re-Seed Now") { _, _ ->
                DatabaseSeeder.seedDatabase()
                Toast.makeText(this, "Database re-seeded successfully!", Toast.LENGTH_SHORT).show()
                fetchStats()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
