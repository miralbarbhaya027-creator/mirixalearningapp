package com.example.mirixa

data class User(
    val firstName: String? = null,
    val lastName: String? = null,
    val email: String? = null,
    val uid: String? = null,
    val role: String? = "Student",
    val avatarIndex: Int = 0
)