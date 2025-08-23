package com.progresspal.app.utils

import com.progresspal.app.domain.models.User
import kotlin.math.log10

/**
 * Internal gender type for body fat calculations
 */
private enum class GenderType { MALE, FEMALE, OTHER }

/**
 * Parse string gender to internal enum for calculations
 */
private fun parseGender(gender: String?): GenderType {
    return when (gender?.uppercase()) {
        "MALE", "M" -> GenderType.MALE
        "FEMALE", "F" -> GenderType.FEMALE
        else -> GenderType.MALE // Default to male calculations
    }
}

/**
 * Utility class for calculating body fat percentage using the U.S. Navy Method
 * 
 * The Navy Method is more accurate than BMI as it accounts for body composition
 * rather than just weight-to-height ratio. It distinguishes between muscle and fat mass,
 * making it particularly useful for athletic individuals.
 * 
 * References:
 * - U.S. Navy Body Composition Assessment
 * - NCBI studies on body fat measurement accuracy
 * - American Council on Exercise (ACE) body fat categories
 */
object BodyFatCalculator {
    
    /**
     * Calculate body fat percentage using the U.S. Navy Method
     * 
     * @param gender User's gender as String (affects formula used)
     * @param waistCm Waist circumference in centimeters
     * @param neckCm Neck circumference in centimeters  
     * @param heightCm Height in centimeters
     * @param hipsCm Hip circumference in centimeters (required for females)
     * @return Body fat percentage as a Float (2-50%)
     */
    fun calculateNavyMethod(
        gender: String?,
        waistCm: Float,
        neckCm: Float,
        heightCm: Float,
        hipsCm: Float? = null
    ): Float {
        return when (parseGender(gender)) {
            GenderType.MALE -> {
                // Male formula: 86.010 × log10(waist - neck) - 70.041 × log10(height) + 36.76
                if (waistCm <= neckCm) return 5f // Prevent invalid measurements
                
                val result = 86.010 * log10((waistCm - neckCm).toDouble()) - 
                           70.041 * log10(heightCm.toDouble()) + 36.76
                result.toFloat().coerceIn(2f, 50f)
            }
            GenderType.FEMALE -> {
                // Female formula: 163.205 × log10(waist + hips - neck) - 97.684 × log10(height) - 78.387
                if (hipsCm == null) return 25f // Default if hips not provided
                if (waistCm + hipsCm <= neckCm) return 10f // Prevent invalid measurements
                
                val result = 163.205 * log10((waistCm + hipsCm - neckCm).toDouble()) - 
                           97.684 * log10(heightCm.toDouble()) - 78.387
                result.toFloat().coerceIn(10f, 50f)
            }
            GenderType.OTHER -> {
                // Use male formula as default with adjusted range
                if (waistCm <= neckCm) return 8f
                
                val result = 86.010 * log10((waistCm - neckCm).toDouble()) - 
                           70.041 * log10(heightCm.toDouble()) + 36.76
                result.toFloat().coerceIn(5f, 50f)
            }
        }
    }
    
    /**
     * Get body fat category based on percentage and gender
     * Uses American Council on Exercise (ACE) standards
     * 
     * @param bodyFatPercentage The calculated body fat percentage
     * @param gender User's gender as String
     * @return Category name as String
     */
    fun getCategory(bodyFatPercentage: Float, gender: String?): String {
        val ranges = when (parseGender(gender)) {
            GenderType.MALE -> mapOf(
                "Essential Fat" to 2f..5f,
                "Athletes" to 6f..13f,
                "Fitness" to 14f..17f,
                "Average" to 18f..24f,
                "Above Average" to 25f..29f,
                "Obese" to 30f..Float.MAX_VALUE
            )
            GenderType.FEMALE -> mapOf(
                "Essential Fat" to 10f..13f,
                "Athletes" to 14f..20f,
                "Fitness" to 21f..24f,
                "Average" to 25f..31f,
                "Above Average" to 32f..37f,
                "Obese" to 38f..Float.MAX_VALUE
            )
            GenderType.OTHER -> mapOf(
                "Very Lean" to 0f..12f,
                "Lean" to 12f..18f,
                "Ideal" to 18f..22f,
                "Average" to 22f..28f,
                "Above Average" to 28f..35f,
                "High" to 35f..Float.MAX_VALUE
            )
        }
        
        return ranges.entries.find { bodyFatPercentage in it.value }?.key ?: "Unknown"
    }
    
    /**
     * Get detailed body fat assessment with recommendations
     * 
     * @param bodyFatPercentage The calculated body fat percentage
     * @param gender User's gender as String
     * @return BodyFatAssessment object with category, description, and recommendations
     */
    fun getDetailedAssessment(
        bodyFatPercentage: Float,
        gender: String?
    ): BodyFatAssessment {
        val category = getCategory(bodyFatPercentage, gender)
        val healthRisk = getHealthRisk(bodyFatPercentage, gender)
        val recommendations = getRecommendations(category, gender)
        val isHealthy = isHealthyRange(bodyFatPercentage, gender)
        
        return BodyFatAssessment(
            percentage = bodyFatPercentage,
            category = category,
            healthRisk = healthRisk,
            recommendations = recommendations,
            isHealthy = isHealthy,
            description = getCategoryDescription(category, gender)
        )
    }
    
    /**
     * Check if body fat percentage is in healthy range
     * 
     * @param bodyFatPercentage The body fat percentage
     * @param gender User's gender as String
     * @return True if in healthy range, false otherwise
     */
    fun isHealthyRange(bodyFatPercentage: Float, gender: String?): Boolean {
        return when (parseGender(gender)) {
            GenderType.MALE -> bodyFatPercentage in 10f..20f
            GenderType.FEMALE -> bodyFatPercentage in 18f..28f
            GenderType.OTHER -> bodyFatPercentage in 14f..24f
        }
    }
    
    /**
     * Get health risk level based on body fat percentage
     * 
     * @param bodyFatPercentage The body fat percentage
     * @param gender User's gender as String
     * @return Health risk level as String
     */
    private fun getHealthRisk(bodyFatPercentage: Float, gender: String?): String {
        return when (parseGender(gender)) {
            GenderType.MALE -> when {
                bodyFatPercentage < 5f -> "Health Risk - Too Low"
                bodyFatPercentage in 5f..20f -> "Low Risk"
                bodyFatPercentage in 20f..25f -> "Moderate Risk"
                bodyFatPercentage > 25f -> "High Risk"
                else -> "Unknown"
            }
            GenderType.FEMALE -> when {
                bodyFatPercentage < 12f -> "Health Risk - Too Low"
                bodyFatPercentage in 12f..28f -> "Low Risk"
                bodyFatPercentage in 28f..35f -> "Moderate Risk"
                bodyFatPercentage > 35f -> "High Risk"
                else -> "Unknown"
            }
            GenderType.OTHER -> when {
                bodyFatPercentage < 10f -> "Health Risk - Too Low"
                bodyFatPercentage in 10f..25f -> "Low Risk"
                bodyFatPercentage in 25f..30f -> "Moderate Risk"
                bodyFatPercentage > 30f -> "High Risk"
                else -> "Unknown"
            }
        }
    }
    
    /**
     * Get recommendations based on body fat category
     * 
     * @param category The body fat category
     * @param gender User's gender as String
     * @return List of recommendations as Strings
     */
    private fun getRecommendations(category: String, gender: String?): List<String> {
        return when (category.lowercase()) {
            "essential fat", "very lean" -> listOf(
                "Your body fat is very low - monitor your health",
                "Ensure adequate nutrition and avoid excessive cardio",
                "Consider consulting a healthcare provider",
                "Focus on maintaining current muscle mass"
            )
            "athletes", "lean" -> listOf(
                "Excellent body composition for athletic performance",
                "Continue current training and nutrition plan",
                "Monitor energy levels and recovery",
                "Maintain balance between training and rest"
            )
            "fitness", "ideal" -> listOf(
                "You're in great shape! Maintain current habits",
                "Focus on strength training to preserve muscle",
                "Continue balanced nutrition approach",
                "Regular cardio for cardiovascular health"
            )
            "average" -> listOf(
                "Room for improvement with consistent effort",
                "Focus on creating a moderate caloric deficit",
                "Combine strength training with cardio",
                "Track nutrition and aim for whole foods"
            )
            "above average", "high" -> listOf(
                "Consider implementing a structured fat loss plan",
                "Prioritize strength training to preserve muscle",
                "Create a sustainable caloric deficit",
                "Focus on protein intake and whole foods"
            )
            "obese" -> listOf(
                "Consider consulting with healthcare professionals",
                "Start with gradual lifestyle changes",
                "Focus on sustainable habits over quick fixes",
                "Combine diet modifications with regular activity"
            )
            else -> listOf(
                "Continue tracking your progress",
                "Maintain a balanced approach to health",
                "Focus on overall wellness, not just numbers"
            )
        }
    }
    
    /**
     * Get detailed description of body fat category
     * 
     * @param category The body fat category
     * @param gender User's gender as String
     * @return Description string
     */
    private fun getCategoryDescription(category: String, gender: String?): String {
        return when (category.lowercase()) {
            "essential fat" -> "The minimum fat required for basic physical and physiological health"
            "athletes" -> "Typical of elite athletes in sports requiring low body fat"
            "fitness" -> "Represents a fit, healthy, and athletic appearance"
            "average" -> "Acceptable body fat range for general health"
            "above average" -> "Higher than average but still within acceptable range"
            "obese" -> "Increased health risks - consider lifestyle changes"
            "very lean" -> "Very low body fat - monitor health carefully"
            "lean" -> "Low body fat typical of athletes and fitness enthusiasts"
            "ideal" -> "Optimal body fat range for health and appearance"
            "high" -> "Elevated body fat - focus on healthy weight management"
            else -> "Body fat measurement result"
        }
    }
    
    /**
     * Calculate ideal body fat range for user
     * 
     * @param gender User's gender as String
     * @return Pair of (minimum, maximum) ideal body fat percentages
     */
    fun getIdealRange(gender: String?): Pair<Float, Float> {
        return when (parseGender(gender)) {
            GenderType.MALE -> 12f to 18f
            GenderType.FEMALE -> 18f to 25f
            GenderType.OTHER -> 15f to 22f
        }
    }
    
    /**
     * Validate measurements before calculation
     * 
     * @param waistCm Waist measurement
     * @param neckCm Neck measurement
     * @param heightCm Height measurement
     * @param hipsCm Hip measurement (optional)
     * @return ValidationResult with isValid flag and error message if applicable
     */
    fun validateMeasurements(
        waistCm: Float,
        neckCm: Float,
        heightCm: Float,
        hipsCm: Float? = null
    ): ValidationResult {
        return when {
            waistCm <= 0 || waistCm > 200 -> ValidationResult(false, "Waist measurement must be between 1-200 cm")
            neckCm <= 0 || neckCm > 100 -> ValidationResult(false, "Neck measurement must be between 1-100 cm")
            heightCm <= 0 || heightCm > 300 -> ValidationResult(false, "Height must be between 1-300 cm")
            waistCm <= neckCm -> ValidationResult(false, "Waist measurement must be larger than neck measurement")
            hipsCm != null && (hipsCm <= 0 || hipsCm > 200) -> ValidationResult(false, "Hip measurement must be between 1-200 cm")
            hipsCm != null && hipsCm < waistCm -> ValidationResult(false, "Hip measurement should typically be larger than waist")
            else -> ValidationResult(true, null)
        }
    }
}

/**
 * Data class representing a comprehensive body fat assessment
 */
data class BodyFatAssessment(
    val percentage: Float,
    val category: String,
    val healthRisk: String,
    val recommendations: List<String>,
    val isHealthy: Boolean,
    val description: String
)

/**
 * Data class for measurement validation results
 */
data class ValidationResult(
    val isValid: Boolean,
    val errorMessage: String?
)