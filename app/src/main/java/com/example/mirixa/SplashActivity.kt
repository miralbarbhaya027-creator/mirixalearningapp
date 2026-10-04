package com.example.mirixa

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.mirixa.databinding.ActivitySplashBinding
import com.google.firebase.auth.FirebaseAuth

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        
        super.onCreate(savedInstanceState)
        
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Handler(Looper.getMainLooper()).postDelayed({
            checkUserStatus()
        }, 1500)
    }

    private fun checkUserStatus() {
        val prefs = getSharedPreferences("mirixa_prefs", MODE_PRIVATE)
        val isLoggedIn = prefs.getBoolean("is_logged_in", false)
        val role = prefs.getString("user_role", "Student")
        val fName = prefs.getString("user_first_name", "")
        val currentUser = FirebaseAuth.getInstance().currentUser

        if (isLoggedIn || currentUser != null) {
            if (role == "Admin" || fName == "Admin" || fName == "Mirixa") {
                startActivity(Intent(this, AdminSelectionActivity::class.java))
            } else {
                startActivity(Intent(this, MainActivity::class.java))
            }
        } else {
            startActivity(Intent(this, LoginActivity::class.java))
        }
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }
}
