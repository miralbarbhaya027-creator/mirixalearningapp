package com.example.mirixa

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mirixa.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth

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

        // --- ADMIN BYPASS LOGIC ---
        // This allows the admin to log in even if the device is temporarily blocked by Firebase security
        if (email == "mirixalearning@gmail.com" && password == "mirixa0365") {
            // Save admin session locally
            getSharedPreferences("mirixa_prefs", MODE_PRIVATE).edit()
                .putString("user_first_name", "Admin")
                .putInt("user_avatar_index", 4)
                .apply()
            
            Toast.makeText(this, "Admin Access Granted", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, AdminSelectionActivity::class.java))
            finish()
            return
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val uid = auth.currentUser?.uid
                    if (uid != null) {
                        // Check if it's the specific admin account
                        if (email == "mirixalearning@gmail.com") {
                            // Ensure admin details exist in DB
                            val db = com.google.firebase.database.FirebaseDatabase.getInstance().reference
                            val adminUser = User("Mirixa", "Admin", email, uid, "Admin", 4)
                            db.child("users").child(uid).setValue(adminUser)
                            
                            getSharedPreferences("mirixa_prefs", MODE_PRIVATE).edit()
                                .putString("user_first_name", "Admin")
                                .putInt("user_avatar_index", 4)
                                .apply()

                            startActivity(Intent(this, AdminSelectionActivity::class.java))
                            finish()
                        } else {
                            // Standard user login
                            com.google.firebase.database.FirebaseDatabase.getInstance().reference.child("users").child(uid).get()
                                .addOnSuccessListener { snapshot ->
                                    val firstName = snapshot.child("firstName").getValue(String::class.java)
                                    getSharedPreferences("mirixa_prefs", MODE_PRIVATE).edit()
                                        .putString("user_first_name", firstName)
                                        .apply()
                                    
                                    Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show()
                                    startActivity(Intent(this, MainActivity::class.java))
                                    finish()
                                }
                                .addOnFailureListener {
                                    startActivity(Intent(this, MainActivity::class.java))
                                    finish()
                                }
                        }
                    }
                } else {
                    Toast.makeText(this, "Login Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}