package com.example.mirixa

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.mirixa.databinding.ActivityTermsConditionsBinding

class TermsConditionsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTermsConditionsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTermsConditionsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.btnAgree.setOnClickListener {
            finish()
        }
    }
}