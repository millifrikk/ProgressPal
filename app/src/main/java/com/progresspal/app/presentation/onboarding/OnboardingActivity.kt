package com.progresspal.app.presentation.onboarding

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.progresspal.app.MainActivity
import com.progresspal.app.R
import com.progresspal.app.data.database.ProgressPalDatabase
import com.progresspal.app.data.repository.UserRepository
import com.progresspal.app.databinding.ActivityOnboardingBinding
import com.progresspal.app.domain.contracts.OnboardingContract
import com.progresspal.app.presentation.onboarding.fragments.*
import java.text.SimpleDateFormat
import java.util.*

class OnboardingActivity : AppCompatActivity(), OnboardingContract.View {
    
    private lateinit var binding: ActivityOnboardingBinding
    private lateinit var presenter: OnboardingContract.Presenter
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    
    private val welcomeFragment = WelcomeFragment()
    private val userInfoFragment = UserInfoFragment()
    private val goalsFragment = GoalsFragment()
    private val measurementsFragment = MeasurementsSetupFragment()
    
    private val fragments = listOf(
        welcomeFragment,
        userInfoFragment,
        goalsFragment,
        measurementsFragment
    )
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupPresenter()
        setupViewPager()
        setupButtons()
        setupDots()
    }
    
    private fun setupPresenter() {
        val database = ProgressPalDatabase.getDatabase(this)
        val userRepository = UserRepository(database.userDao())
        presenter = OnboardingPresenter(userRepository)
        presenter.attachView(this)
    }
    
    private fun setupViewPager() {
        val adapter = OnboardingPagerAdapter()
        binding.viewPager.adapter = adapter
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updateUI(position)
            }
        })
    }
    
    private fun setupButtons() {
        binding.btnBack.setOnClickListener {
            val currentItem = binding.viewPager.currentItem
            if (currentItem > 0) {
                binding.viewPager.currentItem = currentItem - 1
            }
        }
        
        binding.btnNext.setOnClickListener {
            val currentItem = binding.viewPager.currentItem
            if (currentItem < fragments.size - 1) {
                binding.viewPager.currentItem = currentItem + 1
            } else {
                collectAllDataAndSave()
            }
        }
        
        // Hide skip button to make registration mandatory
        binding.btnSkip.visibility = android.view.View.GONE
    }
    
    private fun setupDots() {
        updateDots(0)
    }
    
    private fun updateDots(position: Int) {
        val dots = listOf(binding.dot0, binding.dot1, binding.dot2, binding.dot3)
        dots.forEachIndexed { index, dot ->
            dot.isSelected = index == position
        }
    }
    
    private fun updateUI(position: Int) {
        updateDots(position)
        
        binding.btnBack.visibility = if (position == 0) {
            android.view.View.INVISIBLE
        } else {
            android.view.View.VISIBLE
        }
        
        binding.btnNext.text = if (position == fragments.size - 1) {
            getString(R.string.finish)
        } else {
            getString(R.string.next)
        }
        
        // Skip button is always hidden (mandatory registration)
        binding.btnSkip.visibility = android.view.View.GONE
    }
    
    private fun collectAllDataAndSave() {
        // Clear previous validation errors
        clearValidationErrors()
        
        // Collect data from all fragments
        val userInfoData = userInfoFragment.getUserData()
        val goalData = goalsFragment.getGoalData()
        val measurementData = measurementsFragment.getMeasurementData()
        
        if (userInfoData == null) {
            showError("Please fill in required user information")
            binding.viewPager.currentItem = 1 // Go to UserInfoFragment
            return
        }
        
        if (measurementData == null) {
            showError("Please check your measurements data")
            binding.viewPager.currentItem = 3 // Go to MeasurementsFragment
            return
        }
        
        // Convert data to string format expected by presenter
        presenter.saveUserInfo(
            name = userInfoData.name,
            age = userInfoData.age?.toString(),
            height = userInfoData.height.toString(),
            gender = userInfoData.gender,
            activityLevel = userInfoData.activityLevel,
            currentWeight = userInfoData.currentWeight.toString(),
            targetWeight = goalData?.targetWeight?.toString(),
            targetDate = goalData?.targetDate?.let { dateFormat.format(it) },
            motivation = goalData?.motivation,
            trackMeasurements = measurementData.trackMeasurements,
            waist = measurementData.waist?.toString(),
            chest = measurementData.chest?.toString(),
            hips = measurementData.hips?.toString()
        )
    }
    
    private fun finishOnboarding() {
        // Save onboarding completion to preferences  
        val sharedPref = getSharedPreferences("app_prefs", MODE_PRIVATE)
        with(sharedPref.edit()) {
            putBoolean("onboarding_completed", true)
            apply()
        }
        
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
    
    private inner class OnboardingPagerAdapter : FragmentStateAdapter(this) {
        override fun getItemCount(): Int = fragments.size
        override fun createFragment(position: Int): Fragment = fragments[position]
    }
    
    // OnboardingContract.View implementation
    override fun showValidationError(field: String, message: String) {
        // Show validation error based on field
        when (field) {
            "height", "weight", "age" -> {
                // Navigate to UserInfoFragment
                binding.viewPager.currentItem = 1
                showError(message)
            }
            "targetWeight" -> {
                // Navigate to GoalsFragment
                binding.viewPager.currentItem = 2
                showError(message)
            }
            "waist", "chest", "hips" -> {
                // Navigate to MeasurementsFragment
                binding.viewPager.currentItem = 3
                showError(message)
            }
            else -> showError(message)
        }
    }
    
    override fun clearValidationErrors() {
        // Clear any displayed errors
        // This could be extended to clear specific field errors
    }
    
    override fun showUserSavedSuccess() {
        Toast.makeText(this, "Profile saved successfully!", Toast.LENGTH_SHORT).show()
    }
    
    override fun navigateToMainActivity() {
        finishOnboarding()
    }
    
    override fun setCurrentPage(position: Int) {
        binding.viewPager.currentItem = position
    }
    
    override fun goToNextPage() {
        val currentItem = binding.viewPager.currentItem
        if (currentItem < fragments.size - 1) {
            binding.viewPager.currentItem = currentItem + 1
        }
    }
    
    override fun goToPreviousPage() {
        val currentItem = binding.viewPager.currentItem
        if (currentItem > 0) {
            binding.viewPager.currentItem = currentItem - 1
        }
    }
    
    override fun showLoading() {
        // Disable buttons to prevent multiple submissions
        binding.btnNext.isEnabled = false
        binding.btnBack.isEnabled = false
        binding.btnSkip.isEnabled = false
    }
    
    override fun hideLoading() {
        // Re-enable buttons
        binding.btnNext.isEnabled = true
        binding.btnBack.isEnabled = true
        binding.btnSkip.isEnabled = true
    }
    
    override fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }
    
    override fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        presenter.detachView()
    }
}