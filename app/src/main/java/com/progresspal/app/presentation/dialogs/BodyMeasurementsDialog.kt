package com.progresspal.app.presentation.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.progresspal.app.databinding.DialogBodyMeasurementsBinding
import com.progresspal.app.domain.models.MeasurementSystem
import com.progresspal.app.utils.UnitConverter

class BodyMeasurementsDialog : DialogFragment() {

    private var _binding: DialogBodyMeasurementsBinding? = null
    private val binding get() = _binding!!
    
    private var measurementSystem: MeasurementSystem = MeasurementSystem.METRIC
    private var userGender: String? = null
    private var currentNeck: Float? = null
    private var currentWaist: Float? = null
    private var currentHip: Float? = null
    
    private var onMeasurementsAddedListener: ((BodyMeasurements) -> Unit)? = null
    
    data class BodyMeasurements(
        val neckCm: Float?,
        val waistCm: Float?,
        val hipCm: Float?
    )
    
    companion object {
        fun newInstance(
            measurementSystem: MeasurementSystem,
            userGender: String? = null,
            currentNeck: Float? = null,
            currentWaist: Float? = null,
            currentHip: Float? = null
        ): BodyMeasurementsDialog {
            val dialog = BodyMeasurementsDialog()
            dialog.measurementSystem = measurementSystem
            dialog.userGender = userGender
            dialog.currentNeck = currentNeck
            dialog.currentWaist = currentWaist
            dialog.currentHip = currentHip
            return dialog
        }
    }
    
    fun setOnMeasurementsAddedListener(listener: (BodyMeasurements) -> Unit) {
        this.onMeasurementsAddedListener = listener
    }
    
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogBodyMeasurementsBinding.inflate(LayoutInflater.from(context))
        
        setupViews()
        setupMeasurementSystem()
        populateCurrentValues()
        
        return AlertDialog.Builder(requireContext())
            .setView(binding.root)
            .create()
    }
    
    private fun setupViews() {
        // Show hip measurement field for females
        if (userGender?.uppercase() == "FEMALE" || userGender?.uppercase() == "F") {
            binding.tilHipValue.visibility = View.VISIBLE
        }
        
        binding.btnCancel.setOnClickListener {
            dismiss()
        }
        
        binding.btnSave.setOnClickListener {
            saveMeasurements()
        }
    }
    
    private fun setupMeasurementSystem() {
        val systemText = when (measurementSystem) {
            MeasurementSystem.METRIC -> "Measurements in Metric (cm)"
            MeasurementSystem.IMPERIAL -> "Measurements in Imperial (inches)"
        }
        binding.tvMeasurementSystemInfo.text = systemText
        
        // Update input hints based on measurement system
        val unit = if (measurementSystem == MeasurementSystem.METRIC) "cm" else "inches"
        binding.etNeckValue.hint = "Neck circumference ($unit)"
        binding.etWaistValue.hint = "Waist circumference ($unit)"
        binding.etHipValue.hint = "Hip circumference ($unit)"
        
        // Update helper texts
        val neckHelper = if (measurementSystem == MeasurementSystem.METRIC) {
            "Measure around neck at narrowest point"
        } else {
            "Measure around neck at narrowest point"
        }
        val waistHelper = if (measurementSystem == MeasurementSystem.METRIC) {
            "Measure at narrowest point of torso"
        } else {
            "Measure at narrowest point of torso"
        }
        val hipHelper = if (measurementSystem == MeasurementSystem.METRIC) {
            "Measure at widest point of hips"
        } else {
            "Measure at widest point of hips"
        }
        
        binding.tilNeckValue.helperText = neckHelper
        binding.tilWaistValue.helperText = waistHelper
        binding.tilHipValue.helperText = hipHelper
    }
    
    private fun populateCurrentValues() {
        // Populate current values if available, converting units as needed
        currentNeck?.let { neckCm ->
            val displayValue = if (measurementSystem == MeasurementSystem.IMPERIAL) {
                UnitConverter.cmToInches(neckCm)
            } else {
                neckCm
            }
            binding.etNeckValue.setText(String.format("%.1f", displayValue))
        }
        
        currentWaist?.let { waistCm ->
            val displayValue = if (measurementSystem == MeasurementSystem.IMPERIAL) {
                UnitConverter.cmToInches(waistCm)
            } else {
                waistCm
            }
            binding.etWaistValue.setText(String.format("%.1f", displayValue))
        }
        
        currentHip?.let { hipCm ->
            val displayValue = if (measurementSystem == MeasurementSystem.IMPERIAL) {
                UnitConverter.cmToInches(hipCm)
            } else {
                hipCm
            }
            binding.etHipValue.setText(String.format("%.1f", displayValue))
        }
    }
    
    private fun saveMeasurements() {
        try {
            val neckInput = binding.etNeckValue.text?.toString()?.toFloatOrNull()
            val waistInput = binding.etWaistValue.text?.toString()?.toFloatOrNull()
            val hipInput = binding.etHipValue.text?.toString()?.toFloatOrNull()
            
            // Validate that at least one measurement is provided
            if (neckInput == null && waistInput == null && hipInput == null) {
                binding.tilNeckValue.error = "Please enter at least one measurement"
                return
            }
            
            // Clear any previous errors
            binding.tilNeckValue.error = null
            binding.tilWaistValue.error = null
            binding.tilHipValue.error = null
            
            // Validate measurement ranges
            neckInput?.let { neck ->
                val minNeck = if (measurementSystem == MeasurementSystem.METRIC) 25f else 10f
                val maxNeck = if (measurementSystem == MeasurementSystem.METRIC) 60f else 24f
                if (neck < minNeck || neck > maxNeck) {
                    val unit = if (measurementSystem == MeasurementSystem.METRIC) "cm" else "inches"
                    binding.tilNeckValue.error = "Neck must be between $minNeck and $maxNeck $unit"
                    return
                }
            }
            
            waistInput?.let { waist ->
                val minWaist = if (measurementSystem == MeasurementSystem.METRIC) 50f else 20f
                val maxWaist = if (measurementSystem == MeasurementSystem.METRIC) 200f else 80f
                if (waist < minWaist || waist > maxWaist) {
                    val unit = if (measurementSystem == MeasurementSystem.METRIC) "cm" else "inches"
                    binding.tilWaistValue.error = "Waist must be between $minWaist and $maxWaist $unit"
                    return
                }
            }
            
            hipInput?.let { hip ->
                val minHip = if (measurementSystem == MeasurementSystem.METRIC) 60f else 24f
                val maxHip = if (measurementSystem == MeasurementSystem.METRIC) 200f else 80f
                if (hip < minHip || hip > maxHip) {
                    val unit = if (measurementSystem == MeasurementSystem.METRIC) "cm" else "inches"
                    binding.tilHipValue.error = "Hip must be between $minHip and $maxHip $unit"
                    return
                }
            }
            
            // Convert all measurements to centimeters for storage
            val neckCm = neckInput?.let { 
                if (measurementSystem == MeasurementSystem.IMPERIAL) {
                    UnitConverter.inchesToCm(it)
                } else {
                    it
                }
            }
            
            val waistCm = waistInput?.let { 
                if (measurementSystem == MeasurementSystem.IMPERIAL) {
                    UnitConverter.inchesToCm(it)
                } else {
                    it
                }
            }
            
            val hipCm = hipInput?.let { 
                if (measurementSystem == MeasurementSystem.IMPERIAL) {
                    UnitConverter.inchesToCm(it)
                } else {
                    it
                }
            }
            
            val measurements = BodyMeasurements(
                neckCm = neckCm,
                waistCm = waistCm,
                hipCm = hipCm
            )
            
            onMeasurementsAddedListener?.invoke(measurements)
            dismiss()
            
        } catch (e: NumberFormatException) {
            binding.tilNeckValue.error = "Please enter valid numbers"
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}