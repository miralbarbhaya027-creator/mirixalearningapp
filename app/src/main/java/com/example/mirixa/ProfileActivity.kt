package com.example.mirixa

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.mirixa.databinding.ActivityProfileBinding
import com.google.android.material.card.MaterialCardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private var currentAvatarIndex: Int = 0
    private var tempAvatarIndex: Int = 0

    private val avatarResources = listOf(
        R.drawable.ic_person,
        R.drawable.ic_school,
        R.drawable.ic_bulb,
        R.drawable.ic_star,
        R.drawable.ic_sparkle
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        val prefs = getSharedPreferences("mirixa_prefs", MODE_PRIVATE)
        val isLoggedIn = prefs.getBoolean("is_logged_in", false) || auth.currentUser != null
        if (!isLoggedIn) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        val uid = auth.currentUser?.uid ?: prefs.getString("user_uid", "") ?: ""
        if (uid.isNotEmpty()) {
            fetchUserInfo(uid)
        }

        binding.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.btnEditProfilePic.setOnClickListener {
            showAvatarPickerDialog()
        }

        binding.btnCancelEdit.setOnClickListener {
            binding.layoutEditActions.visibility = View.GONE
            tempAvatarIndex = currentAvatarIndex
            updateAvatarUi(currentAvatarIndex)
            Toast.makeText(this, "Changes discarded", Toast.LENGTH_SHORT).show()
        }

        binding.btnSaveEdit.setOnClickListener {
            saveAvatarToFirebase()
        }

        binding.btnPersonalInfo.setOnClickListener {
            startActivity(Intent(this, PersonalInformationActivity::class.java))
        }

        binding.btnPrivacy.setOnClickListener {
            startActivity(Intent(this, PrivacyPolicyActivity::class.java))
        }

        binding.btnTerms.setOnClickListener {
            startActivity(Intent(this, TermsConditionsActivity::class.java))
        }

        binding.btnAbout.setOnClickListener {
            startActivity(Intent(this, AboutActivity::class.java))
        }

        binding.btnLogout.setOnClickListener {
            auth.signOut()
            // Clear local cache if any
            getSharedPreferences("mirixa_prefs", MODE_PRIVATE).edit().clear().apply()
            Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, AdminSelectionActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finishAffinity()
        }

        // Bottom Navigation
        binding.navHome.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            startActivity(intent)
            finish()
        }
        
        binding.navCategories.setOnClickListener {
            startActivity(Intent(this, CategoriesActivity::class.java))
            finish()
        }
        
        binding.navProgress.setOnClickListener {
            startActivity(Intent(this, ProgressActivity::class.java))
            finish()
        }
    }

    private fun fetchUserInfo(uid: String) {
        database.reference.child("users").child(uid).get()
            .addOnSuccessListener { snapshot ->
                val user = snapshot.getValue(User::class.java)
                if (user != null) {
                    val fullName = "${user.firstName} ${user.lastName}"
                    binding.tvProfileName.text = fullName
                    binding.tvProfileEmail.text = user.email
                    currentAvatarIndex = user.avatarIndex
                    tempAvatarIndex = currentAvatarIndex
                    updateAvatarUi(currentAvatarIndex)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load profile info", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateAvatarUi(index: Int) {
        if (index in avatarResources.indices) {
            binding.ivSmallProfile.setImageResource(avatarResources[index])
            binding.ivProfileLarge.setImageResource(avatarResources[index])
        }
    }

    private fun saveAvatarToFirebase() {
        val uid = auth.currentUser?.uid ?: return
        database.reference.child("users").child(uid).child("avatarIndex").setValue(tempAvatarIndex)
            .addOnSuccessListener {
                currentAvatarIndex = tempAvatarIndex
                binding.layoutEditActions.visibility = View.GONE
                updateAvatarUi(currentAvatarIndex)
                
                // Update cache for other screens
                getSharedPreferences("mirixa_prefs", MODE_PRIVATE).edit()
                    .putInt("user_avatar_index", currentAvatarIndex)
                    .apply()

                Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to save profile changes", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showAvatarPickerDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_avatar_picker, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        val avatars = listOf(
            Pair(dialogView.findViewById<MaterialCardView>(R.id.avatar_1), 0),
            Pair(dialogView.findViewById<MaterialCardView>(R.id.avatar_2), 1),
            Pair(dialogView.findViewById<MaterialCardView>(R.id.avatar_3), 2),
            Pair(dialogView.findViewById<MaterialCardView>(R.id.avatar_4), 3),
            Pair(dialogView.findViewById<MaterialCardView>(R.id.avatar_5), 4)
        )

        avatars.forEach { pair ->
            pair.first.setOnClickListener {
                tempAvatarIndex = pair.second
                binding.ivProfileLarge.setImageResource(avatarResources[tempAvatarIndex])
                binding.layoutEditActions.visibility = View.VISIBLE
                dialog.dismiss()
            }
        }

        dialog.show()
    }
}
