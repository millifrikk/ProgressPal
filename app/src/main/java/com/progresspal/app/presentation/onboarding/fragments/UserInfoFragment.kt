package com.progresspal.app.presentation.onboarding.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import com.progresspal.app.R
import com.progresspal.app.databinding.FragmentUserInfoBinding

class UserInfoFragment : Fragment() {
    
    private var _binding: FragmentUserInfoBinding? = null
    private val binding get() = _binding!!
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserInfoBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
    }
    
    private fun setupViews() {
        setupGenderSpinner()
        setupActivityLevelSpinner()
    }
    
    private fun setupGenderSpinner() {
        val genderOptions = arrayOf("Select Gender (Optional)", "Male", "Female", "Other")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, genderOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerGender.adapter = adapter
    }
    
    private fun setupActivityLevelSpinner() {
        val activityLevels = arrayOf(
            "Select Activity Level (Optional)",
            "Sedentary (little/no exercise)",
            "Lightly active (light exercise 1-3 days/week)",
            "Moderately active (moderate exercise 3-5 days/week)",
            "Very active (hard exercise 6-7 days/week)",
            "Extremely active (very hard exercise/physical job)"
        )
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, activityLevels)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerActivityLevel.adapter = adapter
    }
    
    fun getUserData(): UserInfoData? {
        val name = binding.etName.text.toString().trim()
        val ageStr = binding.etAge.text.toString().trim()
        val heightStr = binding.etHeight.text.toString().trim()
        val weightStr = binding.etCurrentWeight.text.toString().trim()
        
        // Validate required fields
        if (heightStr.isEmpty()) {
            binding.tilHeight.error = "Height is required"
            return null
        }
        
        if (weightStr.isEmpty()) {
            binding.tilCurrentWeight.error = "Current weight is required"
            return null
        }
        
        try {
            val age = if (ageStr.isNotEmpty()) ageStr.toInt() else null
            val height = heightStr.toFloat()
            val currentWeight = weightStr.toFloat()
            
            val gender = if (binding.spinnerGender.selectedItemPosition > 0) {
                binding.spinnerGender.selectedItem.toString()
            } else null
            
            val activityLevel = if (binding.spinnerActivityLevel.selectedItemPosition > 0) {
                binding.spinnerActivityLevel.selectedItem.toString()
            } else null
            
            return UserInfoData(
                name = name.ifEmpty { null },
                age = age,
                height = height,
                currentWeight = currentWeight,
                gender = gender,
                activityLevel = activityLevel
            )
        } catch (e: NumberFormatException) {
            binding.tilHeight.error = "Please enter valid numbers"
            binding.tilCurrentWeight.error = "Please enter valid numbers"
            return null
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

data class UserInfoData(
    val name: String?,
    val age: Int?,
    val height: Float,
    val currentWeight: Float,
    val gender: String?,
    val activityLevel: String?
)