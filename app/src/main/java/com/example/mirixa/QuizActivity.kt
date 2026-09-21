package com.example.mirixa

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.mirixa.databinding.ActivityQuizBinding
import com.google.firebase.database.FirebaseDatabase

class QuizActivity : AppCompatActivity() {

    private lateinit var binding: ActivityQuizBinding
    private var currentQuestionIndex = 0
    private var score = 0
    private var selectedOptionIndex = -1
    private var questions: List<Question> = listOf()
    private var currentCourse: Course? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuizBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val courseId = intent.getStringExtra("COURSE_ID")
        val courseTitle = intent.getStringExtra("COURSE_TITLE") ?: "Quiz"
        
        loadTopBarAvatar()

        if (courseId != null) {
            fetchQuizFromDatabase(courseId)
        } else {
            Toast.makeText(this, "Quiz not found", Toast.LENGTH_SHORT).show()
            finish()
        }

        binding.btnBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        binding.btnProfileTop.setOnClickListener { startActivity(Intent(this, ProfileActivity::class.java)) }

        binding.optionA.setOnClickListener { selectOption(0) }
        binding.optionB.setOnClickListener { selectOption(1) }
        binding.optionC.setOnClickListener { selectOption(2) }
        binding.optionD.setOnClickListener { selectOption(3) }

        binding.btnNext.setOnClickListener {
            if (selectedOptionIndex == -1) {
                Toast.makeText(this, "Please select an option", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            checkAnswer()
            if (currentQuestionIndex < questions.size - 1) {
                currentQuestionIndex++
                selectedOptionIndex = -1
                resetOptionStyles()
                displayQuestion()
            } else {
                showResults()
            }
        }
    }

    private fun fetchQuizFromDatabase(courseId: String) {
        // Show loading state if possible or just clear previous data
        binding.quizProgress.progress = 0
        
        FirebaseDatabase.getInstance().reference.child("courses").child(courseId).get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val course = snapshot.getValue(Course::class.java)
                    if (course != null && course.quiz != null && course.quiz!!.isNotEmpty()) {
                        currentCourse = course
                        questions = course.quiz!!
                        displayQuestion()
                    } else {
                        Toast.makeText(this, "Quiz content is currently being updated", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                } else {
                    Toast.makeText(this, "Course data not found in database", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Database connection error", Toast.LENGTH_SHORT).show()
                finish()
            }
    }

    private fun displayQuestion() {
        if (questions.isEmpty()) return
        
        val q = questions[currentQuestionIndex]
        binding.tvQuestion.text = q.question
        
        // Safely set options
        val optionTextViews = listOf(binding.tvOptionA, binding.tvOptionB, binding.tvOptionC, binding.tvOptionD)
        val optionCards = listOf(binding.optionA, binding.optionB, binding.optionC, binding.optionD)
        
        for (i in 0 until 4) {
            if (i < q.options.size) {
                optionCards[i].visibility = View.VISIBLE
                optionTextViews[i].text = q.options[i]
            } else {
                optionCards[i].visibility = View.GONE
            }
        }

        binding.tvQuestionCount.text = "Question ${currentQuestionIndex + 1} of ${questions.size}"
        binding.quizProgress.progress = ((currentQuestionIndex + 1) * 100) / questions.size
        
        if (currentQuestionIndex == questions.size - 1) {
            binding.btnNext.text = "Finish Quiz"
        } else {
            binding.btnNext.text = "Next Question"
        }
    }

    private fun selectOption(index: Int) {
        selectedOptionIndex = index
        resetOptionStyles()
        val selectedCard = when(index) { 0 -> binding.optionA 1 -> binding.optionB 2 -> binding.optionC else -> binding.optionD }
        val indicator = when(index) { 0 -> binding.indicatorA 1 -> binding.indicatorB 2 -> binding.indicatorC else -> binding.indicatorD }
        selectedCard.setCardBackgroundColor(Color.parseColor("#E6E9FF"))
        selectedCard.strokeColor = ContextCompat.getColor(this, R.color.primary_blue)
        selectedCard.strokeWidth = 4
        indicator.setCardBackgroundColor(ContextCompat.getColor(this, R.color.primary_blue))
        (indicator.getChildAt(0) as TextView).setTextColor(Color.WHITE)
    }

    private fun resetOptionStyles() {
        listOf(binding.optionA, binding.optionB, binding.optionC, binding.optionD).forEach { it.setCardBackgroundColor(Color.WHITE); it.strokeWidth = 0 }
        listOf(binding.indicatorA, binding.indicatorB, binding.indicatorC, binding.indicatorD).forEach {
            it.setCardBackgroundColor(Color.parseColor("#F0F2FF"))
            (it.getChildAt(0) as TextView).setTextColor(Color.parseColor("#888888"))
        }
    }

    private fun checkAnswer() { if (selectedOptionIndex == questions[currentQuestionIndex].correctOptionIndex) score++ }

    private fun showResults() {
        updateProgress()
        val intent = Intent(this, QuizScoreActivity::class.java).apply {
            putExtra("SCORE", score)
            putExtra("TOTAL_QUESTIONS", questions.size)
            putExtra("COURSE_TITLE", intent.getStringExtra("COURSE_TITLE"))
        }
        startActivity(intent)
        finish()
    }

    private fun updateProgress() {
        val course = currentCourse ?: return
        val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: return
        val courseKey = course.title.substringBefore(":").trim().replace(" ", "_").lowercase()
        val ref = FirebaseDatabase.getInstance().reference.child("user_progress").child(uid).child(courseKey)
        
        ref.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                ref.child("quizCompleted").setValue(true)
            } else {
                val newProgress = CourseProgress(
                    courseId = course.id,
                    courseTitle = course.title,
                    quizCompleted = true,
                    imageUrl = course.imageUrl
                )
                ref.setValue(newProgress)
            }
        }
    }

    private fun loadTopBarAvatar() {
        val prefs = getSharedPreferences("mirixa_prefs", MODE_PRIVATE)
        val cachedAvatarIndex = prefs.getInt("user_avatar_index", 0)
        val avatarResources = listOf(R.drawable.ic_person, R.drawable.ic_school, R.drawable.ic_bulb, R.drawable.ic_star, R.drawable.ic_sparkle)
        if (cachedAvatarIndex in avatarResources.indices) binding.ivProfileIconTop.setImageResource(avatarResources[cachedAvatarIndex])
    }
}
