package com.example.mirixa

import android.R
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
        installSplashScreen()
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Handler(Looper.getMainLooper()).postDelayed({
            checkUserStatus()
        }, 2000)
    }

    private fun checkUserStatus() {
        val prefs = getSharedPreferences("mirixa_prefs", MODE_PRIVATE)
        val isLoggedIn = prefs.getBoolean("is_logged_in", false)
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userRole = prefs.getString("user_role", "Student")
        val userEmail = prefs.getString("user_email", "")

        if (isLoggedIn || currentUser != null) {
            if (userRole.equals("Admin", ignoreCase = true) || userEmail == "mirixalearning@gmail.com") {
                startActivity(Intent(this, AdminActivity::class.java))
            } else {
                startActivity(Intent(this, MainActivity::class.java))
            }
        } else {
            startActivity(Intent(this, AdminSelectionActivity::class.java))
        }

        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        finish()
    }
}
