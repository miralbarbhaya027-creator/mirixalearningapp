package com.example.mirixa

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.mirixa.databinding.ActivityContactSupportBinding

class ContactSupportActivity : AppCompatActivity() {

    private lateinit var binding: ActivityContactSupportBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityContactSupportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.btnEmailSupport.setOnClickListener {
            startActivity(Intent(this, EmailSupportActivity::class.java))
        }
    }
}