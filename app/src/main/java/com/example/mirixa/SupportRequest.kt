package com.example.mirixa

data class SupportRequest(
    val id: String? = null,
    val userId: String? = null,
    val subject: String? = null,
    val message: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "Pending" // Pending, In Progress, Resolved
)
