package com.example.mirixa

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mirixa.databinding.ActivityEmailSupportBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class EmailSupportActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEmailSupportBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEmailSupportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        setupSubjectSpinner()

        binding.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.btnProfileTop.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        binding.btnSendMessage.setOnClickListener {
            sendMessage()
        }
    }

    private fun sendMessage() {
        val message = binding.etMessage.text.toString().trim()
        val subject = binding.spinnerSubject.selectedItem.toString()
        val userId = auth.currentUser?.uid

        if (message.isEmpty()) {
            binding.etMessage.error = "Please enter a message"
            return
        }

        if (userId == null) {
            Toast.makeText(this, "Please log in to send a message", Toast.LENGTH_SHORT).show()
            return
        }

        binding.btnSendMessage.isEnabled = false
        binding.btnSendMessage.text = "Sending..."

        val requestRef = database.reference.child("support_requests").push()
        val requestId = requestRef.key
        
        val supportRequest = SupportRequest(
            id = requestId,
            userId = userId,
            subject = subject,
            message = message
        )

        requestRef.setValue(supportRequest)
            .addOnSuccessListener {
                Toast.makeText(this, "Message sent successfully!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                binding.btnSendMessage.isEnabled = true
                binding.btnSendMessage.text = "Send Message"
                Toast.makeText(this, "Failed to send: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupSubjectSpinner() {
        val subjects = arrayOf(
            "General Inquiry",
            "Technical Issue",
            "Course Content",
            "Payment / Subscription",
            "Other"
        )
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, subjects)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerSubject.adapter = adapter
    }
}
