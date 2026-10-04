package com.example.mirixa

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.mirixa.databinding.ActivityAdminSelectionBinding

class AdminSelectionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminSelectionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Enter Mirixa App -> Opens Student Dashboard immediately
        binding.btnOpenApp.setOnClickListener {
            getSharedPreferences("mirixa_prefs", MODE_PRIVATE).edit()
                .putBoolean("is_logged_in", true)
                .apply()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        // Open Admin Control -> Opens Admin Dashboard immediately
        binding.btnOpenAdmin.setOnClickListener {
            getSharedPreferences("mirixa_prefs", MODE_PRIVATE).edit()
                .putBoolean("is_logged_in", true)
                .apply()
            startActivity(Intent(this, AdminActivity::class.java))
            finish()
        }
    }
}
