package com.progresspal.app.presentation.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.progresspal.app.R
import com.progresspal.app.databinding.CardBodyCompositionBinding
import com.progresspal.app.domain.models.ActivityLevel
import com.progresspal.app.domain.models.HealthSettings
import com.progresspal.app.domain.models.MeasurementSystem
import com.progresspal.app.utils.BMIUtils
import com.progresspal.app.utils.BodyCompositionUtils
import com.progresspal.app.utils.BodyCompositionAssessment
import com.progresspal.app.utils.BodyFatCalculator
import com.progresspal.app.utils.HealthRisk
import com.progresspal.app.utils.PrimaryMetric
import com.progresspal.app.utils.UnitConverter

/**
 * Enhanced body composition card that replaces simple BMI display
 * Solves the "athletic build incorrectly labeled overweight" problem
 * Shows WHtR as primary metric with comprehensive health assessment
 */
class BodyCompositionCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : CardView(context, attrs, defStyleAttr) {

    private val binding: CardBodyCompositionBinding
    private var onAddWaistClickListener: (() -> Unit)? = null

    init {
        binding = CardBodyCompositionBinding.inflate(LayoutInflater.from(context), this, true)
        setupClickListeners()
    }

    private fun setupClickListeners() {
        // Add waist measurement buttons
        binding.btnAddWaist.setOnClickListener { onAddWaistClickListener?.invoke() }
        binding.btnAddWaistMeasurement.setOnClickListener { onAddWaistClickListener?.invoke() }
        
        // Card click for detailed view
        binding.cardBodyComposition.setOnClickListener {
            // TODO: Navigate to detailed body composition screen
        }
    }

    /**
     * Update the card with current user data and health settings
     * This is the main method that shows our enhanced assessment
     */
    fun updateBodyComposition(
        weightKg: Float,
        heightCm: Float,
        waistCm: Float? = null,
        neckCm: Float? = null,
        hipCm: Float? = null,
        activityLevel: ActivityLevel = ActivityLevel.ACTIVE,
        age: Int? = null,
        gender: String? = null,
        measurementSystem: MeasurementSystem = MeasurementSystem.METRIC
    ) {
        // Get comprehensive body composition assessment
        val assessment = BodyCompositionUtils.getBodyCompositionScore(
            weight = weightKg,
            height = heightCm,
            waist = waistCm,
            neck = neckCm,
            hip = hipCm,
            activityLevel = activityLevel,
            age = age,
            gender = gender
        )

        // Update last updated status with specific measurement availability
        binding.tvLastUpdated.text = when {
            neckCm != null && waistCm != null -> "Based on complete body measurements"
            waistCm != null -> "Add neck measurement for body fat analysis"
            else -> "Add measurements for enhanced assessment"
        }

        // Display overall category (e.g., "Athletic Build" instead of "Overweight")
        binding.tvOverallCategory.text = assessment.category.displayName

        // Health risk indicator with appropriate colors
        updateHealthRiskChip(assessment.healthRisk)

        // Show metrics based on what's available
        updateMetricsDisplay(assessment, measurementSystem, gender)

        // Show personalized recommendation
        binding.tvRecommendation.text = assessment.recommendation

        // Show/hide body measurement prompt based on what's missing
        updateMeasurementPrompt(weightKg, heightCm, waistCm, neckCm, activityLevel)
    }

    private fun updateHealthRiskChip(healthRisk: HealthRisk) {
        binding.chipHealthRisk.apply {
            text = healthRisk.displayName
            
            val (iconColor, textColor) = when (healthRisk) {
                HealthRisk.VERY_LOW -> Pair(R.color.green_600, R.color.green_600)
                HealthRisk.LOW -> Pair(R.color.green_500, R.color.green_500)
                HealthRisk.MODERATE -> Pair(R.color.orange_500, R.color.orange_500)
                HealthRisk.HIGH -> Pair(R.color.red_500, R.color.red_500)
                HealthRisk.UNKNOWN -> Pair(R.color.gray_50, R.color.text_secondary)
            }
            
            chipIconTint = ContextCompat.getColorStateList(context, iconColor)
            setTextColor(ContextCompat.getColor(context, textColor))
        }
    }

    private fun updateMetricsDisplay(
        assessment: BodyCompositionAssessment,
        measurementSystem: MeasurementSystem,
        gender: String?
    ) {
        // Primary metric (Body Fat when available, WHtR when available, BMI otherwise)
        when (assessment.primaryMetric) {
            PrimaryMetric.BODY_FAT -> {
                val bodyFat = assessment.bodyFatPercentage!!
                val category = assessment.bodyFatCategory ?: "Unknown"
                binding.tvPrimaryMetric.text = "🎯 Navy Method Body Fat: ${"%.1f".format(bodyFat)}% ($category)"
                // Use proper healthy ranges based on gender
                val isHealthy = BodyFatCalculator.isHealthyRange(bodyFat, gender)
                updateMetricStatusIcon(binding.ivPrimaryMetricStatus, isHealthy)
                binding.layoutPrimaryMetric.visibility = View.VISIBLE
            }
            PrimaryMetric.WHTR -> {
                val whtr = assessment.whtr!!
                val status = if (whtr < 0.5f) "✓ Healthy" else "⚠ Elevated"
                binding.tvPrimaryMetric.text = "WHtR: ${"%.2f".format(whtr)} $status"
                updateMetricStatusIcon(binding.ivPrimaryMetricStatus, whtr < 0.5f)
                binding.layoutPrimaryMetric.visibility = View.VISIBLE
            }
            PrimaryMetric.BRI -> {
                val bri = assessment.bri!!
                val status = when {
                    bri < 3f -> "✓ Low Risk"
                    bri < 5f -> "⚠ Moderate"
                    else -> "⚠ Elevated"
                }
                binding.tvPrimaryMetric.text = "BRI: ${"%.1f".format(bri)} $status"
                updateMetricStatusIcon(binding.ivPrimaryMetricStatus, bri < 3f)
                binding.layoutPrimaryMetric.visibility = View.VISIBLE
            }
            PrimaryMetric.BMI -> {
                val bmi = assessment.bmi
                val traditionalCategory = BMIUtils.getBMICategory(bmi)
                binding.tvPrimaryMetric.text = "BMI: ${"%.1f".format(bmi)} ($traditionalCategory)"
                updateMetricStatusIcon(binding.ivPrimaryMetricStatus, bmi in 18.5f..24.9f)
                binding.layoutPrimaryMetric.visibility = View.VISIBLE
            }
        }

        // Secondary metric (BRI when available)
        if (assessment.bri != null) {
            val bri = assessment.bri
            val briStatus = when {
                bri < 3f -> "✓ Low Risk"
                bri < 5f -> "⚠ Moderate"
                else -> "⚠ Elevated"
            }
            binding.tvSecondaryMetric.text = "BRI: ${"%.1f".format(bri)} $briStatus"
            updateMetricStatusIcon(binding.ivSecondaryMetricStatus, bri < 4f)
            binding.layoutSecondaryMetric.visibility = View.VISIBLE
        } else {
            binding.layoutSecondaryMetric.visibility = View.GONE
        }

        // BMI reference (when WHtR is primary)
        if (assessment.primaryMetric == PrimaryMetric.WHTR) {
            val bmi = assessment.bmi!!
            val note = if (assessment.category.displayName.contains("Athletic")) {
                " (Note: Athletic)"
            } else ""
            binding.tvBMIReference.text = "BMI: ${"%.1f".format(bmi)}$note"
            
            if (note.isNotEmpty()) {
                binding.tvBMINote.text = "Enhanced"
                binding.tvBMINote.visibility = View.VISIBLE
            } else {
                binding.tvBMINote.visibility = View.GONE
            }
            binding.layoutBMIReference.visibility = View.VISIBLE
        } else {
            binding.layoutBMIReference.visibility = View.GONE
        }
    }

    private fun updateMetricStatusIcon(imageView: android.widget.ImageView, isHealthy: Boolean) {
        val colorRes = if (isHealthy) R.color.green_500 else R.color.orange_500
        imageView.imageTintList = ContextCompat.getColorStateList(context, colorRes)
        imageView.visibility = View.VISIBLE
    }

    private fun updateMeasurementPrompt(
        weightKg: Float,
        heightCm: Float,
        waistCm: Float?,
        neckCm: Float?,
        activityLevel: ActivityLevel
    ) {
        val shouldShowPrompt = (waistCm == null || neckCm == null)
        
        if (shouldShowPrompt) {
            binding.cardWaistPrompt.visibility = View.VISIBLE
            binding.btnAddWaist.visibility = View.VISIBLE
            
            val message = when {
                waistCm == null && neckCm == null -> 
                    "Add neck & waist measurements for Navy Method body fat percentage"
                neckCm == null -> 
                    "Add neck measurement to complete body fat analysis"
                waistCm == null -> 
                    "Add waist measurement to complete body fat analysis"
                else -> 
                    "Add comprehensive body measurements for enhanced assessment"
            }
            binding.tvWaistPromptMessage.text = message
        } else {
            binding.cardWaistPrompt.visibility = View.GONE
            binding.btnAddWaist.visibility = View.GONE
        }
    }

    /**
     * Set listener for add waist measurement actions
     */
    fun setOnAddWaistClickListener(listener: () -> Unit) {
        onAddWaistClickListener = listener
    }

    /**
     * Show loading state while calculating
     */
    fun showLoading() {
        binding.layoutContent.visibility = View.GONE
        // TODO: Add loading indicator if needed
    }

    /**
     * Hide loading state
     */
    fun hideLoading() {
        binding.layoutContent.visibility = View.VISIBLE
    }

    /**
     * Show error state when data unavailable
     */
    fun showError(message: String) {
        binding.tvRecommendation.text = message
        binding.layoutAssessment.visibility = View.GONE
    }
}