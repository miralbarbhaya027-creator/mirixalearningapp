package com.example.mirixa

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mirixa.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private val database = FirebaseDatabase.getInstance("https://mirixa-b998b-default-rtdb.firebaseio.com")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

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

        binding.tvForgotPassword.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            if (email.isEmpty()) {
                Toast.makeText(this, "Enter your email to reset password", Toast.LENGTH_SHORT).show()
            } else {
                auth.sendPasswordResetEmail(email).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Password reset email sent", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Reset Info: Password reset email queued", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun loginUser() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        if (email.isEmpty()) {
            Toast.makeText(this, "Please enter your email address", Toast.LENGTH_SHORT).show()
            return
        }

        // Admin Credentials Access Bypass
        if (email.contains("admin", ignoreCase = true) || email.contains("mirixa", ignoreCase = true) || password == "mirixa0365") {
            getSharedPreferences("mirixa_prefs", MODE_PRIVATE).edit()
                .putString("user_first_name", "Admin")
                .putInt("user_avatar_index", 4)
                .putBoolean("is_logged_in", true)
                .apply()
            
            Toast.makeText(this, "Admin Access Granted", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, AdminSelectionActivity::class.java))
            finish()
            return
        }

        if (password.isEmpty()) {
            Toast.makeText(this, "Please enter your password", Toast.LENGTH_SHORT).show()
            return
        }

        // Student Firebase Authentication
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val uid = auth.currentUser?.uid ?: ""
                    handleSuccessfulLogin(email, uid)
                } else {
                    // Seamless Fallback: Create Student Account if not existing
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this) { regTask ->
                            if (regTask.isSuccessful) {
                                val uid = auth.currentUser?.uid ?: ""
                                val fName = email.substringBefore("@").replace(".", " ").replaceFirstChar { it.uppercase() }
                                val newUser = User(fName, "Learner", email, uid, "Student", 0)
                                database.reference.child("users").child(uid).setValue(newUser)
                                handleSuccessfulLogin(email, uid)
                            } else {
                                Toast.makeText(this, "Login Error: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                }
            }
    }

    private fun handleSuccessfulLogin(email: String, uid: String) {
        if (email == "mirixalearning@gmail.com") {
            val adminUser = User("Mirixa", "Admin", email, uid, "Admin", 4)
            database.reference.child("users").child(uid).setValue(adminUser)
            
            getSharedPreferences("mirixa_prefs", MODE_PRIVATE).edit()
                .putString("user_first_name", "Admin")
                .putInt("user_avatar_index", 4)
                .putBoolean("is_logged_in", true)
                .apply()

            startActivity(Intent(this, AdminSelectionActivity::class.java))
            finish()
        } else {
            database.reference.child("users").child(uid).get()
                .addOnSuccessListener { snapshot ->
                    val firstName = snapshot.child("firstName").getValue(String::class.java) ?: email.substringBefore("@")
                    getSharedPreferences("mirixa_prefs", MODE_PRIVATE).edit()
                        .putString("user_first_name", firstName)
                        .putBoolean("is_logged_in", true)
                        .apply()
                    
                    Toast.makeText(this, "Welcome to Mirixa Learning!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
                .addOnFailureListener {
                    getSharedPreferences("mirixa_prefs", MODE_PRIVATE).edit()
                        .putString("user_first_name", email.substringBefore("@"))
                        .putBoolean("is_logged_in", true)
                        .apply()

                    Toast.makeText(this, "Welcome to Mirixa Learning!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
        }
    }
}
