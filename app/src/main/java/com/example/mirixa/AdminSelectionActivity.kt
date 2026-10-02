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

        binding.btnLoginAdmin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java).apply {
                putExtra("TARGET_ROLE", "Admin")
            })
        }

        binding.btnLoginStudent.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java).apply {
                putExtra("TARGET_ROLE", "Student")
            })
        }
    }
}
