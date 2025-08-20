package com.progresspal.app.presentation.onboarding.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.progresspal.app.databinding.FragmentMeasurementsSetupBinding

class MeasurementsSetupFragment : Fragment() {
    
    private var _binding: FragmentMeasurementsSetupBinding? = null
    private val binding get() = _binding!!
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMeasurementsSetupBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
    }
    
    private fun setupViews() {
        binding.switchTrackMeasurements.setOnCheckedChangeListener { _, isChecked ->
            binding.measurementsContainer.visibility = if (isChecked) View.VISIBLE else View.GONE
        }
    }
    
    fun getMeasurementData(): MeasurementData? {
        if (!binding.switchTrackMeasurements.isChecked) {
            return MeasurementData(
                trackMeasurements = false,
                waist = null,
                chest = null,
                hips = null
            )
        }
        
        val waistStr = binding.etWaist.text.toString().trim()
        val chestStr = binding.etChest.text.toString().trim()
        val hipsStr = binding.etHips.text.toString().trim()
        
        try {
            return MeasurementData(
                trackMeasurements = true,
                waist = if (waistStr.isNotEmpty()) waistStr.toFloat() else null,
                chest = if (chestStr.isNotEmpty()) chestStr.toFloat() else null,
                hips = if (hipsStr.isNotEmpty()) hipsStr.toFloat() else null
            )
        } catch (e: NumberFormatException) {
            // Show error for invalid numbers
            return null
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

data class MeasurementData(
    val trackMeasurements: Boolean,
    val waist: Float?,
    val chest: Float?,
    val hips: Float?
)