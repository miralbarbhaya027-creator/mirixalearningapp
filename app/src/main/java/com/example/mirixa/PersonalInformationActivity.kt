package com.example.mirixa

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mirixa.databinding.ActivityPersonalInformationBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class PersonalInformationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPersonalInformationBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPersonalInformationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        val currentUser = auth.currentUser
        if (currentUser != null) {
            fetchUserInfo(currentUser.uid)
        }

        binding.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.btnSave.setOnClickListener {
            saveChanges()
        }

        binding.btnChangePass.setOnClickListener {
            changePassword()
        }
    }

    private fun fetchUserInfo(uid: String) {
        database.reference.child("users").child(uid).get()
            .addOnSuccessListener { snapshot ->
                val user = snapshot.getValue(User::class.java)
                if (user != null) {
                    val fullName = "${user.firstName} ${user.lastName}"
                    binding.tvNameDisplay.text = fullName
                    binding.etFullName.setText(fullName)
                    binding.etEmail.setText(user.email)
                    binding.tvRole.text = user.role ?: "Student"
                }
            }
    }

    private fun saveChanges() {
        val fullName = binding.etFullName.text.toString().trim()
        if (fullName.isEmpty()) {
            Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        val nameParts = fullName.split(" ")
        val firstName = nameParts[0]
        val lastName = if (nameParts.size > 1) nameParts.subList(1, nameParts.size).joinToString(" ") else ""

        val uid = auth.currentUser?.uid ?: return
        val updates = mapOf(
            "firstName" to firstName,
            "lastName" to lastName
        )

        database.reference.child("users").child(uid).updateChildren(updates)
            .addOnSuccessListener {
                Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                binding.tvNameDisplay.text = fullName
            }
            .addOnFailureListener {
                Toast.makeText(this, "Update failed: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun changePassword() {
        val newPassword = binding.etNewPassword.text.toString().trim()
        if (newPassword.length < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
            return
        }

        auth.currentUser?.updatePassword(newPassword)
            ?.addOnSuccessListener {
                Toast.makeText(this, "Password changed successfully", Toast.LENGTH_SHORT).show()
                binding.etNewPassword.text?.clear()
            }
            ?.addOnFailureListener {
                Toast.makeText(this, "Failed to change password: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
}