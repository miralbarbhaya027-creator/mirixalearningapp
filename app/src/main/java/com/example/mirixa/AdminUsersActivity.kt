package com.example.mirixa

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.mirixa.databinding.ActivityAdminUsersBinding
import com.google.android.material.card.MaterialCardView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AdminUsersActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminUsersBinding
    private val database = FirebaseDatabase.getInstance("https://mirixa-b998b-default-rtdb.firebaseio.com")
    private val userList = mutableListOf<User>()
    private var usersListener: ValueEventListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminUsersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
        setupNavigation()
        fetchUsers()

        // Live Search Filter
        binding.etSearchUsers.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterUsers(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun openAdminPage(targetClass: Class<*>) {
        if (this::class.java == targetClass) return
        val intent = Intent(this, targetClass).apply {
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        startActivity(intent)
        overridePendingTransition(0, 0)
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener { finish() }

        binding.btnProfileTop.setOnClickListener {
            openAdminPage(AdminSettingsActivity::class.java)
        }

        binding.btnAddUser.setOnClickListener {
            showAddUserDialog()
        }
    }

    private fun setupNavigation() {
        binding.navAdminDashboard.setOnClickListener { openAdminPage(AdminActivity::class.java) }
        binding.navAdminUsers.setOnClickListener {
            Toast.makeText(this, "You are on User Management", Toast.LENGTH_SHORT).show()
        }
        binding.navAdminCategory.setOnClickListener { openAdminPage(AdminCategoriesActivity::class.java) }
        binding.navAdminCourses.setOnClickListener { openAdminPage(AdminCoursesActivity::class.java) }
        binding.navAdminProfile.setOnClickListener { openAdminPage(AdminSettingsActivity::class.java) }
    }

    private fun showAddUserDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Add New User")

        val layout = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(50, 20, 50, 20)
        }

        val nameInput = android.widget.EditText(this).apply {
            hint = "User Full Name (e.g. John Doe)"
        }
        layout.addView(nameInput)

        val emailInput = android.widget.EditText(this).apply {
            hint = "User Email Address (e.g. john@example.com)"
        }
        layout.addView(emailInput)

        builder.setView(layout)

        builder.setPositiveButton("Provision User") { dialog, _ ->
            val name = nameInput.text.toString().trim()
            val email = emailInput.text.toString().trim()

            if (name.isNotEmpty() && email.isNotEmpty()) {
                val parts = name.split(" ")
                val fName = parts.firstOrNull() ?: name
                val lName = if (parts.size > 1) parts.subList(1, parts.size).joinToString(" ") else ""
                val newUid = "MRX-" + (1000..9999).random()

                val newUser = User(fName, lName, email, newUid, "Student", 0, 0, System.currentTimeMillis())

                database.reference.child("users").child(newUid).setValue(newUser)
                    .addOnSuccessListener {
                        Toast.makeText(this, "User '$name' added to Firebase Database!", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { err ->
                        Toast.makeText(this, "Error adding user: ${err.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "Please enter both name and email", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }

        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
        builder.show()
    }

    private fun fetchUsers() {
        binding.pbLoadingUsers.visibility = View.VISIBLE

        val ref = database.reference.child("users")
        usersListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                binding.pbLoadingUsers.visibility = View.GONE
                userList.clear()

                if (snapshot.exists() && snapshot.hasChildren()) {
                    for (uSnapshot in snapshot.children) {
                        var user = uSnapshot.getValue(User::class.java)

                        if (user == null || (user.firstName.isNullOrEmpty() && user.email.isNullOrEmpty())) {
                            val fName = uSnapshot.child("firstName").getValue(String::class.java) ?: ""
                            val lName = uSnapshot.child("lastName").getValue(String::class.java) ?: ""
                            val mail = uSnapshot.child("email").getValue(String::class.java) ?: ""
                            val uidStr = uSnapshot.child("uid").getValue(String::class.java) ?: uSnapshot.key ?: ""
                            val rStr = uSnapshot.child("role").getValue(String::class.java) ?: "Student"
                            val avatarIdx = uSnapshot.child("avatarIndex").getValue(Int::class.java) ?: 0
                            val cCount = uSnapshot.child("completedCourses").getValue(Int::class.java) ?: 0
                            val created = uSnapshot.child("createdAt").getValue(Long::class.java) ?: System.currentTimeMillis()

                            user = User(
                                firstName = fName,
                                lastName = lName,
                                email = mail,
                                uid = uidStr,
                                role = rStr,
                                avatarIndex = avatarIdx,
                                completedCourses = cCount,
                                createdAt = created
                            )
                        }

                        if (!user.email.isNullOrEmpty() || !user.firstName.isNullOrEmpty()) {
                            userList.add(user)
                        }
                    }
                }

                updateStatsAndRender()
            }

            override fun onCancelled(error: DatabaseError) {
                binding.pbLoadingUsers.visibility = View.GONE
                Toast.makeText(this@AdminUsersActivity, "Firebase Read Error: ${error.message}", Toast.LENGTH_LONG).show()
                updateStatsAndRender()
            }
        }

        ref.addValueEventListener(usersListener!!)
    }

    private fun updateStatsAndRender() {
        val total = userList.size
        val students = userList.count { it.role != "Admin" }
        val admins = userList.count { it.role == "Admin" }

        binding.tvStatTotal.text = total.toString()
        binding.tvStatStudents.text = students.toString()
        binding.tvStatAdmins.text = admins.toString()

        renderUsers(userList)
    }

    private fun filterUsers(query: String) {
        val filtered = userList.filter {
            (it.firstName?.contains(query, ignoreCase = true) == true) ||
            (it.lastName?.contains(query, ignoreCase = true) == true) ||
            (it.email?.contains(query, ignoreCase = true) == true) ||
            (it.uid?.contains(query, ignoreCase = true) == true) ||
            (it.role?.contains(query, ignoreCase = true) == true)
        }
        renderUsers(filtered)
    }

    private fun renderUsers(users: List<User>) {
        binding.adminUsersList.removeAllViews()

        if (users.isEmpty()) {
            binding.cvEmptyUsers.visibility = View.VISIBLE
            binding.adminUsersList.visibility = View.GONE
        } else {
            binding.cvEmptyUsers.visibility = View.GONE
            binding.adminUsersList.visibility = View.VISIBLE

            val avatarResources = listOf(
                R.drawable.ic_person,
                R.drawable.ic_school,
                R.drawable.ic_bulb,
                R.drawable.ic_star,
                R.drawable.ic_sparkle
            )

            for (user in users) {
                val view = LayoutInflater.from(this).inflate(R.layout.item_admin_user, binding.adminUsersList, false)

                val fullName = "${user.firstName ?: ""} ${user.lastName ?: ""}".trim().ifEmpty {
                    user.email?.substringBefore("@") ?: "Learner"
                }
                view.findViewById<TextView>(R.id.tv_user_name).text = fullName
                view.findViewById<TextView>(R.id.tv_user_email).text = user.email ?: "no.email@mirixa.edu"
                view.findViewById<TextView>(R.id.tv_user_role_badge).text = user.role ?: "Student"

                val uidText = if (!user.uid.isNullOrEmpty()) "ID: ${user.uid}" else ""
                view.findViewById<TextView>(R.id.tv_user_uid).text = uidText

                val avatarIv = view.findViewById<ImageView>(R.id.iv_user_avatar)
                val initialsTv = view.findViewById<TextView>(R.id.tv_user_initials)
                val avatarCard = view.findViewById<MaterialCardView>(R.id.avatar_container)

                if (user.avatarIndex in avatarResources.indices && user.avatarIndex > 0) {
                    avatarIv.setImageResource(avatarResources[user.avatarIndex])
                    avatarIv.visibility = View.VISIBLE
                    initialsTv.visibility = View.GONE
                    avatarCard.setCardBackgroundColor(Color.parseColor("#2962FF"))
                } else {
                    avatarIv.visibility = View.GONE
                    initialsTv.visibility = View.VISIBLE
                    val initials = getInitials(user.firstName, user.lastName)
                    initialsTv.text = initials
                    avatarCard.setCardBackgroundColor(getAvatarColor(initials))
                }

                binding.adminUsersList.addView(view)
            }
        }
    }

    private fun getInitials(firstName: String?, lastName: String?): String {
        val f = firstName?.trim()?.take(1)?.uppercase() ?: ""
        val l = lastName?.trim()?.take(1)?.uppercase() ?: ""
        val combined = (f + l)
        return if (combined.isNotEmpty()) combined else "U"
    }

    private fun getAvatarColor(initials: String): Int {
        val colors = listOf(
            "#0052FF", // Vibrant Blue
            "#7000FF", // Deep Purple
            "#006652", // Teal Green
            "#3E2723", // Deep Brown
            "#D500F9", // Lavender Pink
            "#6200EE", // Accent Violet
            "#00838F", // Dark Cyan
            "#D32F2F"  // Crimson
        )
        val index = Math.abs(initials.hashCode()) % colors.size
        return Color.parseColor(colors[index])
    }

    override fun onDestroy() {
        super.onDestroy()
        usersListener?.let { database.reference.child("users").removeEventListener(it) }
    }
}
