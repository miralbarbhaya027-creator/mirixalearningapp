package com.example.mirixa

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
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
            showEditAdminProfileDialog()
        }

        binding.btnSwitchLearner.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finishAffinity()
        }

        binding.btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            getSharedPreferences("mirixa_prefs", MODE_PRIVATE).edit().clear().apply()
            Toast.makeText(this, "Admin Signed Out", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finishAffinity()
        }

        // Navigation
        binding.navAdminDashboard.setOnClickListener { openAdminPage(AdminActivity::class.java) }
        binding.navAdminUsers.setOnClickListener { openAdminPage(AdminUsersActivity::class.java) }
        binding.navAdminCategory.setOnClickListener { openAdminPage(AdminCategoriesActivity::class.java) }
        binding.navAdminCourses.setOnClickListener { openAdminPage(AdminCoursesActivity::class.java) }

        binding.navAdminProfile.setOnClickListener {
            Toast.makeText(this, "You are on Profile Settings", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openAdminPage(targetClass: Class<*>) {
        if (this::class.java == targetClass) return
        val intent = Intent(this, targetClass).apply {
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        startActivity(intent)
        overridePendingTransition(0, 0)
    }

    private fun showEditAdminProfileDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Edit Profile & Password")

        val layout = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(50, 20, 50, 20)
        }

        val nameInput = android.widget.EditText(this).apply {
            hint = "Admin Display Name"
            setText(binding.tvAdminName.text)
        }
        layout.addView(nameInput)

        val emailInput = android.widget.EditText(this).apply {
            hint = "Admin Email Address"
            setText(binding.tvAdminEmail.text)
        }
        layout.addView(emailInput)

        val pwInput = android.widget.EditText(this).apply {
            hint = "New Security Password"
            inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        layout.addView(pwInput)
        builder.setView(layout)

        builder.setPositiveButton("Save Changes") { dialog, _ ->
            val newName = nameInput.text.toString().trim()
            val newEmail = emailInput.text.toString().trim()
            val newPw = pwInput.text.toString().trim()

            if (newName.isNotEmpty()) {
                binding.tvAdminName.text = newName
            }
            if (newEmail.isNotEmpty()) {
                binding.tvAdminEmail.text = newEmail
            }

            if (newPw.isNotEmpty()) {
                val user = FirebaseAuth.getInstance().currentUser
                user?.updatePassword(newPw)?.addOnSuccessListener {
                    Toast.makeText(this, "Password updated successfully!", Toast.LENGTH_SHORT).show()
                }
            }

            Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
        builder.show()
    }
}
