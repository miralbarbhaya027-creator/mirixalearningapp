package com.example.mirixa

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mirixa.databinding.ActivityAdminSettingsBinding
import com.google.firebase.auth.FirebaseAuth

class AdminSettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminSettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }

        binding.btnEditProfile.setOnClickListener {
            Toast.makeText(this, "Profile Editing restricted to Root Admin.", Toast.LENGTH_SHORT).show()
        }

        binding.btnSwitchLearner.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finishAffinity()
        }

        binding.btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finishAffinity()
        }

        // Navigation
        binding.navAdminOverview.setOnClickListener {
            startActivity(Intent(this, AdminActivity::class.java))
            finish()
        }

        binding.navAdminCategories.setOnClickListener {
            startActivity(Intent(this, AdminCategoriesActivity::class.java))
            finish()
        }

        binding.navAdminUsers.setOnClickListener {
            Toast.makeText(this, "User Management coming soon.", Toast.LENGTH_SHORT).show()
        }

        binding.navAdminCourses.setOnClickListener {
            Toast.makeText(this, "Content Studio coming soon.", Toast.LENGTH_SHORT).show()
        }
        
        binding.navAdminSettings.setOnClickListener {
            // Already here
        }
    }
}
