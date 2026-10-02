package com.example.mirixa

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.mirixa.databinding.ActivityAdminUsersBinding
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AdminUsersActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminUsersBinding
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()
    private val userList = mutableListOf<User>()
    private var searchQuery = ""

    private val avatarResources = listOf(
        R.drawable.ic_person,
        R.drawable.ic_school,
        R.drawable.ic_bulb,
        R.drawable.ic_star,
        R.drawable.ic_sparkle
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminUsersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener {
            startActivity(Intent(this, AdminActivity::class.java))
            finish()
        }

        binding.btnProfileTop.setOnClickListener {
            startActivity(Intent(this, AdminSettingsActivity::class.java))
            finish()
        }

        setupNavigation()
        setupSearch()

        // Ensure user is authenticated with Firebase Auth before fetching from database
        if (auth.currentUser == null) {
            auth.signInAnonymously().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    fetchUsers()
                } else {
                    Toast.makeText(this, "Firebase Auth failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            fetchUsers()
        }

        binding.btnAddUser.setOnClickListener {
            showAddUserDialog()
        }
    }

    private fun setupNavigation() {
        binding.navAdminOverview.setOnClickListener {
            startActivity(Intent(this, AdminActivity::class.java))
            finish()
        }
        binding.navAdminCategories.setOnClickListener {
            startActivity(Intent(this, AdminCategoriesActivity::class.java))
            finish()
        }
        binding.navAdminCourses.setOnClickListener {
            startActivity(Intent(this, AdminCoursesActivity::class.java))
            finish()
        }
        binding.navAdminSettings.setOnClickListener {
            startActivity(Intent(this, AdminSettingsActivity::class.java))
            finish()
        }
    }

    private fun setupSearch() {
        binding.etSearchUsers.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchQuery = s.toString().trim()
                renderUsers()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun fetchUsers() {
        database.reference.child("users").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                userList.clear()
                var studentCount = 0
                var adminCount = 0

                for (child in snapshot.children) {
                    val uid = child.key ?: child.child("uid").getValue(String::class.java) ?: ""
                    val firstName = child.child("firstName").getValue(String::class.java) ?: ""
                    val lastName = child.child("lastName").getValue(String::class.java) ?: ""
                    val email = child.child("email").getValue(String::class.java) ?: ""
                    val role = child.child("role").getValue(String::class.java) ?: "Student"
                    val avatarIndex = child.child("avatarIndex").getValue(Int::class.java) ?: 0

                    val user = User(
                        firstName = firstName,
                        lastName = lastName,
                        email = email,
                        uid = uid,
                        role = role,
                        avatarIndex = avatarIndex
                    )
                    userList.add(user)

                    if (role.equals("Admin", ignoreCase = true)) {
                        adminCount++
                    } else {
                        studentCount++
                    }
                }

                binding.tvTotalUsersCount.text = "${userList.size} Total Users"
                binding.tvStudentsCountBadge.text = "$studentCount Students"
                binding.tvAdminsCountBadge.text = "$adminCount Admins"

                renderUsers()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@AdminUsersActivity, "Failed to load users: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun renderUsers() {
        binding.adminUsersList.removeAllViews()

        val filtered = if (searchQuery.isEmpty()) {
            userList
        } else {
            userList.filter {
                val fullName = "${it.firstName} ${it.lastName}"
                fullName.contains(searchQuery, ignoreCase = true) ||
                        (it.email ?: "").contains(searchQuery, ignoreCase = true)
            }
        }

        if (filtered.isEmpty()) {
            val emptyView = TextView(this).apply {
                text = if (searchQuery.isEmpty()) "No users registered yet." else "No users match '$searchQuery'"
                textSize = 14f
                setTextColor(getColor(R.color.text_light))
                setPadding(0, 32, 0, 32)
                gravity = Gravity.CENTER
            }
            binding.adminUsersList.addView(emptyView)
            return
        }

        for (user in filtered) {
            addUserCardToUi(user)
        }
    }

    private fun addUserCardToUi(user: User) {
        val view = LayoutInflater.from(this).inflate(R.layout.item_admin_user, binding.adminUsersList, false)

        val fullName = "${user.firstName ?: ""} ${user.lastName ?: ""}".trim().ifEmpty { "Anonymous User" }
        view.findViewById<TextView>(R.id.tv_user_name).text = fullName
        view.findViewById<TextView>(R.id.tv_user_email).text = user.email ?: "No Email"

        val ivAvatar = view.findViewById<ImageView>(R.id.iv_user_avatar)
        val iconContainer = view.findViewById<MaterialCardView>(R.id.icon_user_container)

        if (user.avatarIndex in avatarResources.indices) {
            ivAvatar.setImageResource(avatarResources[user.avatarIndex])
        }

        val roleBadge = view.findViewById<TextView>(R.id.tv_role_badge)
        val btnToggleRole = view.findViewById<MaterialButton>(R.id.btn_toggle_role)
        val btnDeleteUser = view.findViewById<MaterialButton>(R.id.btn_delete_user)

        val isAdmin = user.role.equals("Admin", ignoreCase = true)

        if (isAdmin) {
            roleBadge.text = "ADMIN"
            roleBadge.setTextColor(getColor(R.color.accent_purple))
            roleBadge.setBackgroundResource(R.drawable.card_background)
            roleBadge.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#F5F2FF"))

            iconContainer.setCardBackgroundColor(Color.parseColor("#F5F2FF"))
            ivAvatar.setColorFilter(Color.parseColor("#A100FF"))

            btnToggleRole.text = "Demote Student"
            btnToggleRole.setIconResource(R.drawable.ic_person)
        } else {
            roleBadge.text = "STUDENT"
            roleBadge.setTextColor(getColor(R.color.primary_blue))
            roleBadge.setBackgroundResource(R.drawable.card_background)
            roleBadge.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#F1F4FF"))

            iconContainer.setCardBackgroundColor(Color.parseColor("#F1F4FF"))
            ivAvatar.setColorFilter(Color.parseColor("#2962FF"))

            btnToggleRole.text = "Make Admin"
            btnToggleRole.setIconResource(R.drawable.ic_star)
        }

        btnToggleRole.setOnClickListener {
            val newRole = if (isAdmin) "Student" else "Admin"
            AlertDialog.Builder(this)
                .setTitle("Change User Role")
                .setMessage("Are you sure you want to change $fullName's role to $newRole?")
                .setPositiveButton("Confirm") { _, _ ->
                    val uid = user.uid
                    if (!uid.isNullOrEmpty()) {
                        database.reference.child("users").child(uid).child("role").setValue(newRole)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Updated $fullName to $newRole", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Failed to update role: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        btnDeleteUser.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Delete User")
                .setMessage("Are you sure you want to delete user $fullName?")
                .setPositiveButton("Delete") { _, _ ->
                    val uid = user.uid
                    if (!uid.isNullOrEmpty()) {
                        database.reference.child("users").child(uid).removeValue()
                            .addOnSuccessListener {
                                Toast.makeText(this, "User deleted successfully", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Failed to delete user: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        binding.adminUsersList.addView(view)
    }

    private fun showAddUserDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_user, null)
        val etFirstName = dialogView.findViewById<TextInputEditText>(R.id.et_first_name)
        val etLastName = dialogView.findViewById<TextInputEditText>(R.id.et_last_name)
        val etEmail = dialogView.findViewById<TextInputEditText>(R.id.et_user_email)
        val etPassword = dialogView.findViewById<TextInputEditText>(R.id.et_password)
        val spinnerRole = dialogView.findViewById<Spinner>(R.id.spinner_role)

        val roles = arrayOf("Student", "Admin")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, roles)
        spinnerRole.adapter = adapter

        AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton("Create") { _, _ ->
                val fName = etFirstName.text.toString().trim()
                val lName = etLastName.text.toString().trim()
                val email = etEmail.text.toString().trim()
                val role = spinnerRole.selectedItem.toString()

                if (fName.isEmpty() || email.isEmpty()) {
                    Toast.makeText(this, "Please enter First Name and Email", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                if (role.equals("Admin", ignoreCase = true)) {
                    showAdminKeyVerificationDialog {
                        createUserRecord(fName, lName, email, "admin")
                    }
                } else {
                    createUserRecord(fName, lName, email, "student")
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showAdminKeyVerificationDialog(onSuccess: () -> Unit) {
        val input = TextInputEditText(this).apply {
            hint = "Enter Admin Access Key"
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            textSize = 18f
            setPadding(48, 32, 48, 32)
        }

        val container = FrameLayout(this).apply {
            setPadding(48, 16, 48, 16)
            addView(input)
        }

        AlertDialog.Builder(this)
            .setTitle("🔑 Admin Access Key Required")
            .setMessage("Please enter the secret Admin Access Key to create a new Administrator account.")
            .setView(container)
            .setPositiveButton("Verify") { _, _ ->
                val key = input.text.toString().trim()
                if (key == "mirixa_admin_2026") {
                    onSuccess()
                } else {
                    Toast.makeText(this, "❌ Invalid Admin Access Key", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun createUserRecord(fName: String, lName: String, email: String, role: String) {
        val newRef = database.reference.child("users").push()
        val uid = newRef.key ?: System.currentTimeMillis().toString()
        val newUser = User(
            firstName = fName,
            lastName = lName,
            email = email,
            uid = uid,
            role = role,
            avatarIndex = if (role == "admin") 4 else 0
        )

        newRef.setValue(newUser)
            .addOnSuccessListener {
                Toast.makeText(this, "User $fName created successfully!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to create user: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
