package com.example.mirixa

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mirixa.databinding.ActivityAdminSettingsBinding
import com.google.firebase.auth.FirebaseAuth

class AdminSettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminSettingsBinding

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val prefs = getSharedPreferences("mirixa_prefs", MODE_PRIVATE)
        val email = prefs.getString("user_email", null) ?: FirebaseAuth.getInstance().currentUser?.email ?: "mirixalearning@gmail.com"
        val firstName = prefs.getString("user_first_name", "Admin")
        val lastName = prefs.getString("user_last_name", "User")

        binding.tvAdminEmail.text = email
        binding.tvAdminName.text = "$firstName $lastName".trim()

        binding.btnBack.setOnClickListener {
            startActivity(Intent(this, AdminActivity::class.java))
            finish()
        }

        binding.btnEditProfile.setOnClickListener {
            startActivity(Intent(this, PersonalInformationActivity::class.java))
        }

        binding.btnUpdatePassword.setOnClickListener {
            if (email.isEmpty()) {
                Toast.makeText(this, "Admin email not found", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                .addOnSuccessListener {
                    Toast.makeText(this, "✉️ Password reset email sent to $email! Please check your inbox.", Toast.LENGTH_LONG).show()
                    binding.etCurrentPassword.setText("")
                    binding.etNewPassword.setText("")
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to send reset email: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }

        binding.btnSwitchLearner.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finishAffinity()
        }

        binding.btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            getSharedPreferences("mirixa_prefs", MODE_PRIVATE).edit().clear().apply()
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, AdminSelectionActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
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
            startActivity(Intent(this, AdminUsersActivity::class.java))
            finish()
        }

        binding.navAdminCourses.setOnClickListener {
            startActivity(Intent(this, AdminCoursesActivity::class.java))
            finish()
        }

        binding.navAdminSettings.setOnClickListener {
            // Already here
        }
    }
}
