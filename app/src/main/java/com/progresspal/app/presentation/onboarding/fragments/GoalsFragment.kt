package com.progresspal.app.presentation.onboarding.fragments

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import com.progresspal.app.databinding.FragmentGoalsBinding
import java.text.SimpleDateFormat
import java.util.*

class GoalsFragment : Fragment() {
    
    private var _binding: FragmentGoalsBinding? = null
    private val binding get() = _binding!!
    
    private var selectedTargetDate: Date? = null
    private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGoalsBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
    }
    
    private fun setupViews() {
        setupMotivationSpinner()
        setupDatePicker()
    }
    
    private fun setupMotivationSpinner() {
        val motivations = arrayOf(
            "What's your main goal?",
            "Lose weight",
            "Gain muscle",
            "Maintain current weight",
            "General health tracking",
            "Other"
        )
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, motivations)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerMotivation.adapter = adapter
    }
    
    private fun setupDatePicker() {
        binding.etTargetDate.setOnClickListener {
            showDatePicker()
        }
        
        binding.etTargetDate.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                showDatePicker()
            }
        }
    }
    
    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MONTH, 3) // Default to 3 months from now
        
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val selectedCalendar = Calendar.getInstance()
                selectedCalendar.set(year, month, dayOfMonth)
                selectedTargetDate = selectedCalendar.time
                binding.etTargetDate.setText(dateFormat.format(selectedTargetDate!!))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        
        // Set minimum date to tomorrow
        val minCalendar = Calendar.getInstance()
        minCalendar.add(Calendar.DAY_OF_MONTH, 1)
        datePickerDialog.datePicker.minDate = minCalendar.timeInMillis
        
        datePickerDialog.show()
    }
    
    fun getGoalData(): GoalData? {
        val targetWeightStr = binding.etTargetWeight.text.toString().trim()
        
        val targetWeight = if (targetWeightStr.isNotEmpty()) {
            try {
                targetWeightStr.toFloat()
            } catch (e: NumberFormatException) {
                binding.tilTargetWeight.error = "Please enter a valid weight"
                return null
            }
        } else {
            null
        }
        
        val motivation = if (binding.spinnerMotivation.selectedItemPosition > 0) {
            binding.spinnerMotivation.selectedItem.toString()
        } else null
        
        return GoalData(
            targetWeight = targetWeight,
            targetDate = selectedTargetDate,
            motivation = motivation
        )
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

data class GoalData(
    val targetWeight: Float?,
    val targetDate: Date?,
    val motivation: String?
)