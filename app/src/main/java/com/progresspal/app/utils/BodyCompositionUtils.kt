package com.progresspal.app.utils

import com.progresspal.app.domain.models.ActivityLevel
import kotlin.math.*

/**
 * Enhanced body composition utilities replacing oversimplified BMI calculations
 * Implements WHtR, BRI, ABSI, and activity-adjusted assessments for accurate health evaluation
 * 
 * Solves the "athletic build incorrectly labeled overweight" problem by using:
 * - Waist-to-Height Ratio (WHtR) as primary metric
 * - Body Roundness Index (BRI) for athletic builds
 * - Activity-level adjustments for muscle mass considerations
 */
object BodyCompositionUtils {
    
    /**
     * Calculate Waist-to-Height Ratio (WHtR) - Most reliable body composition metric
     * More accurate than BMI for predicting health risks
     * @param waistCm Waist circumference in centimeters
     * @param heightCm Height in centimeters
     * @return WHtR value (optimal: <0.5, elevated risk: ≥0.6)
     */
    fun calculateWHtR(waistCm: Float, heightCm: Float): Float {
        return waistCm / heightCm
    }
    
    /**
     * Calculate Body Roundness Index (BRI) - Better for athletic builds
     * Accounts for body shape and muscle distribution
     * @param waistCm Waist circumference in centimeters
     * @param heightCm Height in centimeters
     * @return BRI value (1-15 scale, lower is better)
     */
    fun calculateBRI(waistCm: Float, heightCm: Float): Float {
        val waistToHeightRatio = waistCm / heightCm
        if (waistToHeightRatio >= 1.0f) {
            // Avoid domain error for extreme cases
            return 15.0f
        }
        val eccentricity = sqrt(1 - waistToHeightRatio.pow(2))
        return 364.2f - 365.5f * eccentricity
    }
    
    /**
     * Calculate A Body Shape Index (ABSI) - Mortality predictor
     * Normalized waist circumference adjusted for height and weight
     * @param waistCm Waist circumference in centimeters
     * @param heightCm Height in centimeters
     * @param weightKg Weight in kilograms
     * @return ABSI value (higher values indicate increased health risk)
     */
    fun calculateABSI(waistCm: Float, heightCm: Float, weightKg: Float): Float {
        val heightM = heightCm / 100f
        val bmi = calculateBMI(weightKg, heightCm)
        if (bmi <= 0 || heightM <= 0) return 0f
        
        return waistCm / (bmi.pow(2f/3f) * heightM.pow(1f/2f))
    }
    
    /**
     * Traditional BMI calculation (kept for reference and fallback)
     * @param weightKg Weight in kilograms
     * @param heightCm Height in centimeters
     * @return BMI value
     */
    fun calculateBMI(weightKg: Float, heightCm: Float): Float {
        val heightM = heightCm / 100f
        if (heightM <= 0) return 0f
        return weightKg / (heightM * heightM)
    }
    
    /**
     * Calculate Waist-to-Hip Ratio (WHR) - Additional body shape indicator
     * @param waistCm Waist circumference in centimeters
     * @param hipCm Hip circumference in centimeters
     * @return WHR value (men: <0.9, women: <0.8 for lower risk)
     */
    fun calculateWHR(waistCm: Float, hipCm: Float): Float {
        if (hipCm <= 0) return 0f
        return waistCm / hipCm
    }
    
    /**
     * Get comprehensive body composition assessment
     * This is the main method that replaces simple BMI classification
     */
    fun getBodyCompositionScore(
        weight: Float,
        height: Float,
        waist: Float?,
        hip: Float? = null,
        activityLevel: ActivityLevel,
        age: Int? = null,
        gender: String? = null
    ): BodyCompositionAssessment {
        
        val bmi = calculateBMI(weight, height)
        val whtr = waist?.let { calculateWHtR(it, height) }
        val bri = waist?.let { calculateBRI(it, height) }
        val absi = waist?.let { calculateABSI(it, height, weight) }
        val whr = if (waist != null && hip != null) calculateWHR(waist, hip) else null
        
        return BodyCompositionAssessment(
            bmi = bmi,
            whtr = whtr,
            bri = bri,
            absi = absi,
            whr = whr,
            category = determineCategory(bmi, whtr, bri, activityLevel, age),
            healthRisk = calculateHealthRisk(whtr, bri, absi, whr, gender),
            recommendation = getPersonalizedRecommendation(bmi, whtr, activityLevel, age),
            primaryMetric = determinePrimaryMetric(whtr, bri, activityLevel)
        )
    }
    
    /**
     * Determine body composition category using multiple metrics and activity level
     */
    private fun determineCategory(
        bmi: Float,
        whtr: Float?,
        bri: Float?,
        activityLevel: ActivityLevel,
        age: Int?
    ): BodyCompositionCategory {
        
        // Waist-to-height ratio is the most reliable indicator when available
        if (whtr != null) {
            return when {
                whtr < 0.4f -> BodyCompositionCategory.VERY_LEAN
                whtr < 0.5f -> when (activityLevel) {
                    ActivityLevel.ATHLETIC, ActivityLevel.ENDURANCE_ATHLETE -> BodyCompositionCategory.ATHLETIC_LEAN
                    else -> BodyCompositionCategory.HEALTHY
                }
                whtr < 0.6f -> when (activityLevel) {
                    ActivityLevel.ATHLETIC, ActivityLevel.ENDURANCE_ATHLETE -> BodyCompositionCategory.ATHLETIC_BUILD
                    else -> BodyCompositionCategory.ELEVATED_RISK
                }
                else -> BodyCompositionCategory.HIGH_RISK
            }
        }
        
        // Fall back to BMI with activity-level adjustments
        return getBMICategoryWithActivityAdjustment(bmi, activityLevel, age)
    }
    
    /**
     * BMI category with activity level adjustments for athletes
     */
    private fun getBMICategoryWithActivityAdjustment(
        bmi: Float,
        activityLevel: ActivityLevel,
        age: Int?
    ): BodyCompositionCategory {
        
        // Apply activity level bonus thresholds
        val adjustedBMI = bmi - activityLevel.bmiBonusThreshold
        
        return when (activityLevel) {
            ActivityLevel.ATHLETIC, ActivityLevel.ENDURANCE_ATHLETE -> {
                when {
                    adjustedBMI < 18f -> BodyCompositionCategory.VERY_LEAN
                    adjustedBMI < 25f -> BodyCompositionCategory.ATHLETIC_LEAN
                    adjustedBMI < 28f -> BodyCompositionCategory.ATHLETIC_BUILD  // Higher threshold for athletes
                    adjustedBMI < 32f -> BodyCompositionCategory.HEAVY_ATHLETIC
                    else -> BodyCompositionCategory.NEEDS_ASSESSMENT
                }
            }
            ActivityLevel.ACTIVE -> {
                when {
                    adjustedBMI < 18.5f -> BodyCompositionCategory.UNDERWEIGHT
                    adjustedBMI < 25f -> BodyCompositionCategory.HEALTHY
                    adjustedBMI < 28f -> BodyCompositionCategory.MODERATELY_ELEVATED
                    adjustedBMI < 30f -> BodyCompositionCategory.ELEVATED_RISK
                    else -> BodyCompositionCategory.HIGH_RISK
                }
            }
            else -> {
                // Standard BMI categories for sedentary individuals
                when {
                    bmi < 18.5f -> BodyCompositionCategory.UNDERWEIGHT
                    bmi < 25f -> BodyCompositionCategory.HEALTHY
                    bmi < 30f -> BodyCompositionCategory.ELEVATED_RISK
                    else -> BodyCompositionCategory.HIGH_RISK
                }
            }
        }
    }
    
    /**
     * Calculate overall health risk using multiple metrics
     */
    private fun calculateHealthRisk(
        whtr: Float?,
        bri: Float?,
        absi: Float?,
        whr: Float?,
        gender: String?
    ): HealthRisk {
        var riskScore = 0
        var metricCount = 0
        
        // WHtR risk assessment
        whtr?.let {
            metricCount++
            when {
                it >= 0.6f -> riskScore += 3  // High risk
                it >= 0.5f -> riskScore += 2  // Moderate risk
                it >= 0.4f -> riskScore += 1  // Low risk
                else -> riskScore += 0        // Very low risk
            }
        }
        
        // BRI risk assessment
        bri?.let {
            metricCount++
            when {
                it >= 6f -> riskScore += 3   // High risk
                it >= 4f -> riskScore += 2   // Moderate risk
                it >= 2f -> riskScore += 1   // Low risk
                else -> riskScore += 0       // Very low risk
            }
        }
        
        // WHR risk assessment (gender-specific)
        whr?.let {
            metricCount++
            val threshold = when (gender?.lowercase()) {
                "female" -> 0.8f
                "male" -> 0.9f
                else -> 0.85f  // Average threshold
            }
            if (it >= threshold + 0.1f) riskScore += 3
            else if (it >= threshold) riskScore += 2
            else if (it >= threshold - 0.05f) riskScore += 1
            else riskScore += 0
        }
        
        if (metricCount == 0) return HealthRisk.UNKNOWN
        
        val averageRisk = riskScore.toFloat() / metricCount
        return when {
            averageRisk >= 2.5f -> HealthRisk.HIGH
            averageRisk >= 1.5f -> HealthRisk.MODERATE
            averageRisk >= 0.5f -> HealthRisk.LOW
            else -> HealthRisk.VERY_LOW
        }
    }
    
    /**
     * Get personalized recommendation based on assessment
     */
    private fun getPersonalizedRecommendation(
        bmi: Float,
        whtr: Float?,
        activityLevel: ActivityLevel,
        age: Int?
    ): String {
        
        if (whtr != null) {
            return when {
                whtr < 0.4f -> "Excellent body composition. Maintain current lifestyle."
                whtr < 0.5f && activityLevel.ordinal >= ActivityLevel.ATHLETIC.ordinal -> 
                    "Excellent athletic build. Continue training regimen."
                whtr < 0.5f -> "Healthy body composition. Keep up the good work!"
                whtr < 0.6f && activityLevel.ordinal >= ActivityLevel.ACTIVE.ordinal ->
                    "Good composition for active lifestyle. Consider increasing activity if desired."
                whtr < 0.6f -> "Elevated risk. Focus on waist reduction through diet and exercise."
                else -> "High risk detected. Consult healthcare provider for personalized plan."
            }
        } else {
            // BMI-based recommendations with activity adjustments
            val adjustedBMI = bmi - activityLevel.bmiBonusThreshold
            return when {
                adjustedBMI < 18.5f -> "Consider healthy weight gain strategies."
                adjustedBMI < 25f && activityLevel.ordinal >= ActivityLevel.ATHLETIC.ordinal ->
                    "Healthy athletic weight. Maintain training and nutrition."
                adjustedBMI < 25f -> "Healthy weight range. Maintain current lifestyle."
                adjustedBMI < 30f && activityLevel.ordinal >= ActivityLevel.ATHLETIC.ordinal ->
                    "Athletic build detected. BMI may not reflect true body composition."
                else -> "Consider body composition analysis and healthy lifestyle changes."
            }
        }
    }
    
    /**
     * Determine which metric should be displayed as primary
     */
    private fun determinePrimaryMetric(
        whtr: Float?,
        bri: Float?,
        activityLevel: ActivityLevel
    ): PrimaryMetric {
        return when {
            whtr != null -> PrimaryMetric.WHTR
            bri != null && activityLevel.ordinal >= ActivityLevel.ATHLETIC.ordinal -> PrimaryMetric.BRI
            else -> PrimaryMetric.BMI
        }
    }
}

/**
 * Body composition categories replacing oversimplified BMI categories
 */
enum class BodyCompositionCategory(
    val displayName: String,
    val description: String,
    val colorHex: String
) {
    VERY_LEAN(
        displayName = "Very Lean",
        description = "Low body fat, excellent composition",
        colorHex = "#2196F3"  // Blue
    ),
    ATHLETIC_LEAN(
        displayName = "Athletic Lean",
        description = "Lean athletic build with good muscle definition",
        colorHex = "#4CAF50"  // Green
    ),
    HEALTHY(
        displayName = "Healthy",
        description = "Optimal body composition range",
        colorHex = "#4CAF50"  // Green
    ),
    ATHLETIC_BUILD(
        displayName = "Athletic Build",
        description = "Muscular build with higher weight due to muscle mass",
        colorHex = "#8BC34A"  // Light Green
    ),
    MODERATELY_ELEVATED(
        displayName = "Moderately Elevated",
        description = "Slightly above ideal, manageable with lifestyle changes",
        colorHex = "#FFEB3B"  // Yellow
    ),
    ELEVATED_RISK(
        displayName = "Elevated Risk",
        description = "Increased health risk, lifestyle changes recommended",
        colorHex = "#FF9800"  // Orange
    ),
    HEAVY_ATHLETIC(
        displayName = "Heavy Athletic",
        description = "Heavy but athletic - body composition analysis recommended",
        colorHex = "#FF9800"  // Orange
    ),
    HIGH_RISK(
        displayName = "High Risk",
        description = "Significant health risk, medical consultation advised",
        colorHex = "#F44336"  // Red
    ),
    UNDERWEIGHT(
        displayName = "Underweight",
        description = "Below healthy weight range",
        colorHex = "#9C27B0"  // Purple
    ),
    NEEDS_ASSESSMENT(
        displayName = "Needs Assessment",
        description = "Professional body composition analysis recommended",
        colorHex = "#607D8B"  // Blue Grey
    )
}

/**
 * Health risk levels based on multiple metrics
 */
enum class HealthRisk(val displayName: String, val colorHex: String) {
    VERY_LOW("Very Low Risk", "#4CAF50"),
    LOW("Low Risk", "#8BC34A"),
    MODERATE("Moderate Risk", "#FF9800"),
    HIGH("High Risk", "#F44336"),
    UNKNOWN("Unknown", "#9E9E9E")
}

/**
 * Primary metric to display
 */
enum class PrimaryMetric(val displayName: String, val abbreviation: String) {
    WHTR("Waist-to-Height Ratio", "WHtR"),
    BRI("Body Roundness Index", "BRI"),
    BMI("Body Mass Index", "BMI")
}

/**
 * Comprehensive body composition assessment result
 */
data class BodyCompositionAssessment(
    val bmi: Float,
    val whtr: Float?,
    val bri: Float?,
    val absi: Float?,
    val whr: Float?,
    val category: BodyCompositionCategory,
    val healthRisk: HealthRisk,
    val recommendation: String,
    val primaryMetric: PrimaryMetric
) {
    fun getPrimaryMetricValue(): Float? {
        return when (primaryMetric) {
            PrimaryMetric.WHTR -> whtr
            PrimaryMetric.BRI -> bri
            PrimaryMetric.BMI -> bmi
        }
    }
    
    fun getPrimaryMetricDescription(): String {
        return when (primaryMetric) {
            PrimaryMetric.WHTR -> whtr?.let { "%.2f".format(it) } ?: "N/A"
            PrimaryMetric.BRI -> bri?.let { "%.1f".format(it) } ?: "N/A"
            PrimaryMetric.BMI -> "%.1f".format(bmi)
        }
    }
    
    fun hasWaistMeasurement(): Boolean = whtr != null
    
    fun getMetricSummary(): String {
        val metrics = mutableListOf<String>()
        
        whtr?.let { metrics.add("WHtR: %.2f".format(it)) }
        bri?.let { metrics.add("BRI: %.1f".format(it)) }
        metrics.add("BMI: %.1f".format(bmi))
        
        return metrics.joinToString(" • ")
    }
}