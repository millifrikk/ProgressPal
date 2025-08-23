package com.progresspal.app.utils

import com.progresspal.app.domain.models.ActivityLevel

/**
 * BMIUtils - Enhanced with body composition scoring
 * Maintains backward compatibility while providing access to advanced assessments
 * 
 * @deprecated Use BodyCompositionUtils.getBodyCompositionScore() for accurate assessment
 * This class maintains old BMI methods for compatibility but adds enhanced alternatives
 */
object BMIUtils {
    
    /**
     * Traditional BMI calculation
     * @deprecated Use BodyCompositionUtils for more accurate assessment
     */
    fun calculateBMI(weightKg: Float, heightCm: Float): Float {
        return BodyCompositionUtils.calculateBMI(weightKg, heightCm)
    }
    
    /**
     * Traditional BMI category - oversimplified classification
     * @deprecated Use getEnhancedCategory() for activity-adjusted assessment
     */
    fun getBMICategory(bmi: Float): String {
        return when {
            bmi < 18.5f -> "Underweight"
            bmi < 25.0f -> "Normal"
            bmi < 30.0f -> "Overweight"
            else -> "Obese"
        }
    }
    
    /**
     * Enhanced category with activity level adjustment
     * Solves the "athletic build incorrectly labeled overweight" problem
     * 
     * Example: 178cm/80kg athlete shows "Athletic Build" instead of "Overweight"
     */
    fun getEnhancedCategory(
        weightKg: Float,
        heightCm: Float,
        activityLevel: ActivityLevel = ActivityLevel.ACTIVE,
        waistCm: Float? = null,
        age: Int? = null
    ): String {
        val assessment = BodyCompositionUtils.getBodyCompositionScore(
            weight = weightKg,
            height = heightCm,
            waist = waistCm,
            activityLevel = activityLevel,
            age = age
        )
        return assessment.category.displayName
    }
    
    /**
     * Get comprehensive body composition assessment
     * This is the recommended method for accurate health evaluation
     */
    fun getBodyCompositionAssessment(
        weightKg: Float,
        heightCm: Float,
        waistCm: Float? = null,
        hipCm: Float? = null,
        activityLevel: ActivityLevel = ActivityLevel.ACTIVE,
        age: Int? = null,
        gender: String? = null
    ): BodyCompositionAssessment {
        return BodyCompositionUtils.getBodyCompositionScore(
            weight = weightKg,
            height = heightCm,
            waist = waistCm,
            hip = hipCm,
            activityLevel = activityLevel,
            age = age,
            gender = gender
        )
    }
    
    /**
     * Check if user should add waist measurement for accurate assessment
     */
    fun shouldAddWaistMeasurement(
        weightKg: Float,
        heightCm: Float,
        activityLevel: ActivityLevel
    ): Boolean {
        val bmi = calculateBMI(weightKg, heightCm)
        
        // Recommend waist measurement for:
        // 1. Athletes with BMI > 25 (may be muscle, not fat)
        // 2. Anyone with BMI 23-30 (borderline cases benefit from WHtR)
        // 3. Active individuals with BMI > 24
        
        return when {
            activityLevel.ordinal >= ActivityLevel.ATHLETIC.ordinal && bmi > 25f -> true
            activityLevel == ActivityLevel.ACTIVE && bmi > 24f -> true
            bmi in 23f..30f -> true
            else -> false
        }
    }
    
    /**
     * Get recommendation for improving body composition
     */
    fun getImprovedRecommendation(
        weightKg: Float,
        heightCm: Float,
        waistCm: Float? = null,
        activityLevel: ActivityLevel = ActivityLevel.ACTIVE,
        age: Int? = null
    ): String {
        val assessment = getBodyCompositionAssessment(
            weightKg, heightCm, waistCm, null, activityLevel, age
        )
        return assessment.recommendation
    }
}