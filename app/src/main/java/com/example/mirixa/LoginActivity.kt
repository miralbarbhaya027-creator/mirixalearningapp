package com.example.mirixa

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mirixa.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        val targetRole = intent.getStringExtra("TARGET_ROLE") ?: "Student"

        if (targetRole.equals("Admin", ignoreCase = true)) {
            binding.tvTitle.text = getString(R.string.admin_sign_in)
            binding.tvSubtitle.text = getString(R.string.admin_sign_in_subtitle)
            binding.layoutFooterStudent.visibility = View.GONE
            binding.layoutFooterAdmin.visibility = View.VISIBLE
        } else {
            binding.tvTitle.text = "Welcome Back"
            binding.tvSubtitle.text = "Sign in to continue your journey."
            binding.layoutFooterStudent.visibility = View.VISIBLE
            binding.layoutFooterAdmin.visibility = View.GONE
        }

        binding.btnBackCard.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.btnLogin.setOnClickListener {
            loginUser()
        }

        binding.tvSignup.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
            finish()
        }

        binding.tvAdminSignup.setOnClickListener {
            startActivity(Intent(this, AdminSignupActivity::class.java))
            finish()
        }

        binding.tvForgotPassword.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            if (email.isEmpty()) {
                Toast.makeText(this, "Enter your email to reset password", Toast.LENGTH_SHORT).show()
            } else {
                auth.sendPasswordResetEmail(email).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Password reset email sent", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun loginUser() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        // --- MASTER ADMIN BYPASS LOGIC ---
        if (email == "mirixalearning@gmail.com" && password == "mirixa0365") {
            auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { _ ->
                        saveAdminPrefsAndProceed()
                    }
                } else {
                    saveAdminPrefsAndProceed()
                }
            }
            return
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val uid = auth.currentUser?.uid
                    if (uid != null) {
                        FirebaseDatabase.getInstance().reference.child("users").child(uid).get()
                            .addOnSuccessListener { snapshot ->
                                val firstName = snapshot.child("firstName").getValue(String::class.java) ?: "User"
                                val lastName = snapshot.child("lastName").getValue(String::class.java) ?: ""
                                val role = snapshot.child("role").getValue(String::class.java) ?: "Student"
                                val avatarIndex = snapshot.child("avatarIndex").getValue(Int::class.java) ?: 0

                                getSharedPreferences("mirixa_prefs", MODE_PRIVATE).edit()
                                    .putBoolean("is_logged_in", true)
                                    .putString("user_uid", uid)
                                    .putString("user_email", email)
                                    .putString("user_first_name", firstName)
                                    .putString("user_last_name", lastName)
                                    .putString("user_role", role)
                                    .putInt("user_avatar_index", avatarIndex)
                                    .apply()

                                Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show()

                                if (role.equals("Admin", ignoreCase = true) || email == "mirixalearning@gmail.com") {
                                    startActivity(Intent(this, AdminActivity::class.java))
                                } else {
                                    startActivity(Intent(this, MainActivity::class.java))
                                }
                                finish()
                            }
                            .addOnFailureListener {
                                getSharedPreferences("mirixa_prefs", MODE_PRIVATE).edit()
                                    .putBoolean("is_logged_in", true)
                                    .putString("user_uid", uid)
                                    .putString("user_email", email)
                                    .putString("user_role", if (email == "mirixalearning@gmail.com") "Admin" else "Student")
                                    .apply()

                                if (email == "mirixalearning@gmail.com") {
                                    startActivity(Intent(this, AdminActivity::class.java))
                                } else {
                                    startActivity(Intent(this, MainActivity::class.java))
                                }
                                finish()
                            }
                    }
                } else {
                    Toast.makeText(this, "Login Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun saveAdminPrefsAndProceed() {
        val uid = auth.currentUser?.uid
        if (uid != null) {
            val adminUser = User(
                firstName = "Admin",
                lastName = "Mirixa",
                email = "mirixalearning@gmail.com",
                uid = uid,
                role = "admin",
                avatarIndex = 4
            )
            FirebaseDatabase.getInstance().reference.child("users").child(uid).setValue(adminUser)
        }

        getSharedPreferences("mirixa_prefs", MODE_PRIVATE).edit()
            .putBoolean("is_logged_in", true)
            .putString("user_uid", uid ?: "")
            .putString("user_email", "mirixalearning@gmail.com")
            .putString("user_first_name", "Admin")
            .putString("user_last_name", "Mirixa")
            .putString("user_role", "Admin")
            .putInt("user_avatar_index", 4)
            .apply()

        Toast.makeText(this, "Admin Access Granted", Toast.LENGTH_SHORT).show()
        startActivity(Intent(this, AdminActivity::class.java))
        finish()
    }
}
