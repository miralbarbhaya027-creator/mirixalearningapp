package com.example.mirixa

data class Question(
    val question: String = "",
    val options: List<String> = listOf(),
    val correctOptionIndex: Int = 0
)
