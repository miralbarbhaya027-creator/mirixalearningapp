package com.example.mirixa

data class CourseProgress(
    val courseId: String = "",
    val courseTitle: String = "",
    val videoCompleted: Boolean = false,
    val notesCompleted: Boolean = false,
    val mindMapCompleted: Boolean = false,
    val quizCompleted: Boolean = false,
    val imageUrl: String = ""
) {
    fun getCompletedCount(): Int {
        var count = 0
        if (videoCompleted) count++
        if (notesCompleted) count++
        if (mindMapCompleted) count++
        if (quizCompleted) count++
        return count
    }

    fun getPercentage(): Int {
        return (getCompletedCount() * 100) / 4
    }
}
