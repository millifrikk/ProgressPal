package com.progresspal.app.presentation.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.progresspal.app.R
import com.progresspal.app.databinding.DialogWaistMeasurementBinding
import com.progresspal.app.domain.models.MeasurementSystem
import com.progresspal.app.utils.UnitConverter

/**
 * Dialog for adding waist measurements with imperial/metric support
 * Used by BodyCompositionCardView to get more accurate health assessments
 */
class WaistMeasurementDialog : DialogFragment() {

    private var _binding: DialogWaistMeasurementBinding? = null
    private val binding get() = _binding!!
    
    private var measurementSystem: MeasurementSystem = MeasurementSystem.METRIC
    private var onMeasurementAddedListener: ((Float) -> Unit)? = null

    companion object {
        private const val ARG_MEASUREMENT_SYSTEM = "measurement_system"
        
        fun newInstance(measurementSystem: MeasurementSystem): WaistMeasurementDialog {
            return WaistMeasurementDialog().apply {
                arguments = Bundle().apply {
                    putString(ARG_MEASUREMENT_SYSTEM, measurementSystem.name)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { args ->
            val systemName = args.getString(ARG_MEASUREMENT_SYSTEM, MeasurementSystem.METRIC.name)
            measurementSystem = MeasurementSystem.valueOf(systemName)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogWaistMeasurementBinding.inflate(layoutInflater)
        
        setupViews()
        
        return MaterialAlertDialogBuilder(requireContext())
            .setView(binding.root)
            .setTitle("Add Waist Measurement")
            .setPositiveButton("Add") { _, _ ->
                handleAddMeasurement()
            }
            .setNegativeButton("Cancel", null)
            .create()
    }

    private fun setupViews() {
        // Configure unit system
        when (measurementSystem) {
            MeasurementSystem.METRIC -> {
                binding.tvWaistLabel.text = "Waist Circumference (cm)"
                binding.etWaistValue.hint = "e.g., 82"
                binding.tvInstructions.text = "Measure around the narrowest part of your waist, usually just above your hip bones."
            }
            MeasurementSystem.IMPERIAL -> {
                binding.tvWaistLabel.text = "Waist Circumference (inches)"
                binding.etWaistValue.hint = "e.g., 32.5"
                binding.tvInstructions.text = "Measure around the narrowest part of your waist, usually just above your hip bones."
            }
        }

        // Setup measurement tips
        binding.tvTips.text = """
            Tips for accurate measurement:
            • Use a flexible measuring tape
            • Measure after exhaling normally
            • Keep tape parallel to the floor
            • Don't pull too tight or too loose
        """.trimIndent()

        // Set focus to input field
        binding.etWaistValue.requestFocus()
    }

    private fun handleAddMeasurement() {
        val waistText = binding.etWaistValue.text.toString().trim()
        
        if (waistText.isEmpty()) {
            binding.tilWaistValue.error = "Please enter a waist measurement"
            return
        }

        val waistValue = waistText.toFloatOrNull()
        if (waistValue == null || waistValue <= 0) {
            binding.tilWaistValue.error = "Please enter a valid measurement"
            return
        }

        // Validate reasonable ranges
        val validRange = when (measurementSystem) {
            MeasurementSystem.METRIC -> 40f..200f // cm
            MeasurementSystem.IMPERIAL -> 15f..80f // inches
        }

        if (waistValue !in validRange) {
            val rangeText = when (measurementSystem) {
                MeasurementSystem.METRIC -> "40-200 cm"
                MeasurementSystem.IMPERIAL -> "15-80 inches"
            }
            binding.tilWaistValue.error = "Please enter a value between $rangeText"
            return
        }

        // Convert to metric if needed and callback
        val waistCm = when (measurementSystem) {
            MeasurementSystem.METRIC -> waistValue
            MeasurementSystem.IMPERIAL -> UnitConverter.inchesToCm(waistValue)
        }

        onMeasurementAddedListener?.invoke(waistCm)
        dismiss()
    }

    fun setOnMeasurementAddedListener(listener: (Float) -> Unit) {
        onMeasurementAddedListener = listener
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}