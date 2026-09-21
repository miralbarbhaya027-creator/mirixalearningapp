package com.example.mirixa

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.mirixa.databinding.ActivityQuizScoreBinding

class QuizScoreActivity : AppCompatActivity() {

    private lateinit var binding: ActivityQuizScoreBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuizScoreBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val score = intent.getIntOfExtra("SCORE", 0)
        val totalQuestions = intent.getIntOfExtra("TOTAL_QUESTIONS", 10)
        val courseTitle = intent.getStringExtra("COURSE_TITLE") ?: "the course"

        val percent = if (totalQuestions > 0) (score * 100) / totalQuestions else 0
        
        binding.cpScore.progress = percent
        binding.tvScorePercent.text = "$percent%"
        binding.tvCorrectCount.text = score.toString()
        binding.tvIncorrectCount.text = (totalQuestions - score).toString()

        loadTopBarAvatar()

        val prefs = getSharedPreferences("mirixa_prefs", MODE_PRIVATE)
        val userName = prefs.getString("user_first_name", "Explorer")
        
        val congratsMsg = when {
            percent >= 80 -> "Excellent Work, $userName!"
            percent >= 50 -> "Good Job, $userName!"
            else -> "Keep Practicing, $userName!"
        }
        binding.tvCongratsMsg.text = congratsMsg

        binding.btnBack.setOnClickListener { finish() }
        
        binding.btnReturn.setOnClickListener {
            // Clear activity stack and return to Categories
            val intent = Intent(this, CategoriesActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }
        
        binding.btnProfileTop.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        loadTopBarAvatar()
    }

    private fun loadTopBarAvatar() {
        val prefs = getSharedPreferences("mirixa_prefs", MODE_PRIVATE)
        val cachedAvatarIndex = prefs.getInt("user_avatar_index", 0)
        val avatarResources = listOf(
            R.drawable.ic_person,
            R.drawable.ic_school,
            R.drawable.ic_bulb,
            R.drawable.ic_star,
            R.drawable.ic_sparkle
        )
        if (cachedAvatarIndex in avatarResources.indices) {
            binding.ivProfileIconTop.setImageResource(avatarResources[cachedAvatarIndex])
        }
    }

    // Helper extension to handle potential type issues with getIntExtra
    private fun Intent.getIntOfExtra(name: String, defaultValue: Int): Int {
        return if (hasExtra(name)) getIntExtra(name, defaultValue) else defaultValue
    }
}
