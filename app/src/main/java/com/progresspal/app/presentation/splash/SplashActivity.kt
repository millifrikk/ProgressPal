package com.progresspal.app.presentation.splash

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.progresspal.app.MainActivity
import com.progresspal.app.databinding.ActivitySplashBinding
import com.progresspal.app.presentation.onboarding.OnboardingActivity
import com.progresspal.app.data.database.ProgressPalDatabase
import com.progresspal.app.data.repository.UserRepository

class SplashActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivitySplashBinding
    private val splashDelayMs = 2000L
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupViews()
        navigateAfterDelay()
    }
    
    private fun setupViews() {
        // Views will be set up in the layout
    }
    
    private fun navigateAfterDelay() {
        Handler(Looper.getMainLooper()).postDelayed({
            navigateToNextScreen()
        }, splashDelayMs)
    }
    
    private fun navigateToNextScreen() {
        // Check if user exists in database
        val database = ProgressPalDatabase.getDatabase(this)
        val userRepository = UserRepository(database.userDao())
        
        lifecycleScope.launch {
            val user = userRepository.getUserSync()
            
            val intent = if (user != null) {
                // User exists, go to main dashboard
                Intent(this@SplashActivity, MainActivity::class.java)
            } else {
                // No user, show onboarding
                Intent(this@SplashActivity, OnboardingActivity::class.java)
            }
            
            startActivity(intent)
            finish()
        }
    }
}