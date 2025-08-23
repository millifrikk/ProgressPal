package com.progresspal.app.utils

import com.progresspal.app.data.database.entities.BloodPressureEntity
import com.progresspal.app.data.database.entities.BloodPressureCategory
import com.progresspal.app.domain.models.MedicalGuidelines
import com.progresspal.app.domain.models.ActivityLevel
import java.util.Calendar
import java.util.Date

/**
 * Enhanced blood pressure utilities supporting multiple medical standards
 * Implements both US (AHA) and EU (ESC) guidelines with age-adjusted assessments
 */
object BloodPressureUtils {
    
    /**
     * Get age-adjusted blood pressure category based on medical guidelines
     * @param systolic Systolic pressure in mmHg
     * @param diastolic Diastolic pressure in mmHg
     * @param age User's age (null if not available)
     * @param guidelines Medical guidelines to use (US_AHA or EU_ESC)
     * @return Blood pressure assessment with category and recommendations
     */
    fun getAgeAdjustedCategory(
        systolic: Int,
        diastolic: Int,
        age: Int?,
        guidelines: MedicalGuidelines
    ): BloodPressureAssessment {
        return when (guidelines) {
            MedicalGuidelines.US_AHA -> {
                // AHA uses standard thresholds for all ages
                getAHACategory(systolic, diastolic, age)
            }
            MedicalGuidelines.EU_ESC -> {
                // ESC has age-adjusted targets
                getESCCategory(systolic, diastolic, age)
            }
        }
    }
    
    /**
     * US (AHA) Guidelines - Standard thresholds for all ages
     */
    private fun getAHACategory(systolic: Int, diastolic: Int, age: Int?): BloodPressureAssessment {
        val category = when {
            systolic >= 180 || diastolic >= 120 -> BloodPressureCategory.CRISIS
            systolic >= 140 || diastolic >= 90 -> BloodPressureCategory.STAGE_2
            systolic >= 130 || diastolic >= 80 -> BloodPressureCategory.STAGE_1
            systolic >= 120 && diastolic < 80 -> BloodPressureCategory.ELEVATED
            systolic < 120 && diastolic < 80 -> BloodPressureCategory.OPTIMAL
            else -> BloodPressureCategory.NORMAL
        }
        
        return BloodPressureAssessment(
            category = category,
            guidelines = MedicalGuidelines.US_AHA,
            age = age,
            isAgeAdjusted = false,
            recommendation = getAHARecommendation(category, age)
        )
    }
    
    /**
     * EU (ESC) Guidelines - Age-adjusted thresholds
     */
    private fun getESCCategory(systolic: Int, diastolic: Int, age: Int?): BloodPressureAssessment {
        val category = when {
            // Crisis levels (same for all ages)
            systolic >= 180 || diastolic >= 110 -> BloodPressureCategory.CRISIS
            
            // Age-adjusted thresholds
            age != null && age >= 80 -> {
                // Very elderly (≥80): More relaxed targets
                when {
                    systolic >= 160 || diastolic >= 100 -> BloodPressureCategory.STAGE_2
                    systolic >= 150 || diastolic >= 95 -> BloodPressureCategory.STAGE_1
                    systolic >= 140 || diastolic >= 90 -> BloodPressureCategory.ELEVATED
                    systolic < 120 && diastolic < 70 -> BloodPressureCategory.OPTIMAL
                    else -> BloodPressureCategory.NORMAL
                }
            }
            age != null && age >= 65 -> {
                // Elderly (65-79): Moderately relaxed targets
                when {
                    systolic >= 160 || diastolic >= 100 -> BloodPressureCategory.STAGE_2
                    systolic >= 140 || diastolic >= 90 -> BloodPressureCategory.STAGE_1
                    systolic >= 130 || diastolic >= 85 -> BloodPressureCategory.ELEVATED
                    systolic < 120 && diastolic < 70 -> BloodPressureCategory.OPTIMAL
                    else -> BloodPressureCategory.NORMAL
                }
            }
            else -> {
                // Adults (<65): Standard ESC thresholds
                when {
                    systolic >= 160 || diastolic >= 100 -> BloodPressureCategory.STAGE_2
                    systolic >= 140 || diastolic >= 90 -> BloodPressureCategory.STAGE_1
                    systolic >= 130 || diastolic >= 85 -> BloodPressureCategory.ELEVATED
                    systolic < 120 && diastolic < 70 -> BloodPressureCategory.OPTIMAL
                    else -> BloodPressureCategory.NORMAL
                }
            }
        }
        
        return BloodPressureAssessment(
            category = category,
            guidelines = MedicalGuidelines.EU_ESC,
            age = age,
            isAgeAdjusted = age != null && age >= 65,
            recommendation = getESCRecommendation(category, age)
        )
    }
    
    /**
     * Get personalized blood pressure message considering multiple factors
     */
    fun getPersonalizedBPMessage(
        reading: BloodPressureEntity,
        age: Int?,
        activityLevel: ActivityLevel,
        guidelines: MedicalGuidelines
    ): String {
        val assessment = getAgeAdjustedCategory(reading.systolic, reading.diastolic, age, guidelines)
        val category = assessment.category
        
        // Context-aware messaging
        return when {
            category == BloodPressureCategory.CRISIS -> 
                "⚠️ Hypertensive crisis detected. Seek immediate medical attention."
                
            category == BloodPressureCategory.STAGE_2 && age != null && age > 75 && guidelines == MedicalGuidelines.EU_ESC ->
                "High blood pressure for your age group. Discuss management with your healthcare provider."
                
            category == BloodPressureCategory.STAGE_2 ->
                "Stage 2 hypertension detected. Medical consultation strongly recommended."
                
            category == BloodPressureCategory.STAGE_1 && age != null && age > 65 && guidelines == MedicalGuidelines.EU_ESC ->
                "Mild elevation - common for your age group. Monitor regularly and maintain healthy lifestyle."
                
            category == BloodPressureCategory.STAGE_1 && age != null && age < 30 ->
                "Elevated for your age. Consider stress management and lifestyle modifications."
                
            category == BloodPressureCategory.STAGE_1 ->
                "Stage 1 hypertension. Lifestyle changes and possible medication consultation needed."
                
            category == BloodPressureCategory.ELEVATED && activityLevel == ActivityLevel.ATHLETIC ->
                "Slightly elevated - may be normal post-exercise. Monitor when rested."
                
            category == BloodPressureCategory.ELEVATED && reading.timeOfDay == "morning" ->
                "Morning elevation is common. Consider monitoring throughout the day."
                
            category == BloodPressureCategory.ELEVATED ->
                "Elevated blood pressure. Focus on healthy lifestyle to prevent hypertension."
                
            (category == BloodPressureCategory.OPTIMAL || category == BloodPressureCategory.NORMAL) && activityLevel == ActivityLevel.ATHLETIC ->
                "Excellent blood pressure for an active individual. Keep up the great work!"
                
            category == BloodPressureCategory.OPTIMAL ->
                "Optimal blood pressure. Maintain your healthy lifestyle."
                
            else -> category.getRecommendation()
        }
    }
    
    private fun getAHARecommendation(category: BloodPressureCategory, age: Int?): String {
        return when (category) {
            BloodPressureCategory.OPTIMAL -> "Maintain healthy lifestyle to keep optimal blood pressure."
            BloodPressureCategory.NORMAL -> "Continue healthy habits. Monitor regularly."
            BloodPressureCategory.ELEVATED -> "Adopt healthy lifestyle changes to prevent hypertension."
            BloodPressureCategory.STAGE_1 -> "Lifestyle changes and possible medication. Consult healthcare provider."
            BloodPressureCategory.STAGE_2 -> "Combination of lifestyle changes and medication typically needed."
            BloodPressureCategory.CRISIS -> "Seek immediate medical attention. This is a medical emergency."
        }
    }
    
    private fun getESCRecommendation(category: BloodPressureCategory, age: Int?): String {
        val ageContext = when {
            age != null && age >= 80 -> " (age-adjusted target)"
            age != null && age >= 65 -> " (senior-friendly target)"
            else -> ""
        }
        
        return when (category) {
            BloodPressureCategory.OPTIMAL -> "Optimal blood pressure$ageContext. Excellent cardiovascular health."
            BloodPressureCategory.NORMAL -> "Normal blood pressure$ageContext. Continue current lifestyle."
            BloodPressureCategory.ELEVATED -> "High-normal range$ageContext. Monitor and maintain healthy habits."
            BloodPressureCategory.STAGE_1 -> "Grade 1 hypertension$ageContext. Lifestyle modifications recommended."
            BloodPressureCategory.STAGE_2 -> "Grade 2 hypertension$ageContext. Medical evaluation needed."
            BloodPressureCategory.CRISIS -> "Hypertensive emergency. Immediate medical attention required."
        }
    }
    
    /**
     * Calculate age from birth date
     */
    fun calculateAge(birthDate: Date): Int {
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
        
        calendar.time = birthDate
        val birthYear = calendar.get(Calendar.YEAR)
        val birthMonth = calendar.get(Calendar.MONTH)
        val birthDay = calendar.get(Calendar.DAY_OF_MONTH)
        
        var age = currentYear - birthYear
        
        // Adjust if birthday hasn't occurred this year
        if (currentMonth < birthMonth || (currentMonth == birthMonth && currentDay < birthDay)) {
            age--
        }
        
        return age
    }
    
    /**
     * Get guidelines display string with age context
     */
    fun getGuidelinesDisplayString(guidelines: MedicalGuidelines, age: Int?): String {
        return when (guidelines) {
            MedicalGuidelines.US_AHA -> "US (AHA)"
            MedicalGuidelines.EU_ESC -> {
                val ageContext = when {
                    age != null && age >= 80 -> " • 80+ adjusted"
                    age != null && age >= 65 -> " • 65+ adjusted"
                    else -> ""
                }
                "EU (ESC)$ageContext"
            }
        }
    }
    
    /**
     * Check if reading requires immediate attention
     */
    fun requiresImmediateAttention(
        systolic: Int,
        diastolic: Int,
        pulse: Int,
        age: Int?,
        guidelines: MedicalGuidelines
    ): Boolean {
        val assessment = getAgeAdjustedCategory(systolic, diastolic, age, guidelines)
        return assessment.category == BloodPressureCategory.CRISIS || 
               pulse < 50 || pulse > 120
    }
}

/**
 * Data class for comprehensive blood pressure assessment
 */
data class BloodPressureAssessment(
    val category: BloodPressureCategory,
    val guidelines: MedicalGuidelines,
    val age: Int?,
    val isAgeAdjusted: Boolean,
    val recommendation: String
) {
    fun getFormattedCategory(): String {
        val adjustedText = if (isAgeAdjusted) " (age-adjusted)" else ""
        return "${category.displayName}$adjustedText"
    }
    
    fun getGuidelinesText(): String {
        return when (guidelines) {
            MedicalGuidelines.US_AHA -> "AHA Guidelines"
            MedicalGuidelines.EU_ESC -> "ESC Guidelines"
        }
    }
}