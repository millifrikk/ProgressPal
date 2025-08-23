package com.progresspal.app.domain.models

import java.util.Date

/**
 * User health settings for personalized assessments
 * Controls measurement systems, medical guidelines, and activity levels
 */
data class HealthSettings(
    val measurementSystem: MeasurementSystem = MeasurementSystem.METRIC,
    val medicalGuidelines: MedicalGuidelines = MedicalGuidelines.US_AHA,
    val activityLevel: ActivityLevel = ActivityLevel.ACTIVE,
    val birthDate: Date? = null,
    val preferredLanguage: String = "en"
)

/**
 * Measurement system for weights, heights, and body measurements
 */
enum class MeasurementSystem(val displayName: String) {
    METRIC("Metric (kg, cm)"),
    IMPERIAL("Imperial (lbs, ft/in)")
}

/**
 * Medical guidelines for blood pressure assessment
 */
enum class MedicalGuidelines(
    val displayName: String,
    val description: String
) {
    US_AHA(
        displayName = "US (AHA)",
        description = "American Heart Association Guidelines"
    ),
    EU_ESC(
        displayName = "EU (ESC)",
        description = "European Society of Cardiology Guidelines"
    )
}

/**
 * User activity level for body composition adjustments
 */
enum class ActivityLevel(
    val displayName: String,
    val description: String,
    val bmiBonusThreshold: Float // BMI adjustment for athletic builds
) {
    SEDENTARY(
        displayName = "Sedentary",
        description = "Little to no exercise",
        bmiBonusThreshold = 0f
    ),
    ACTIVE(
        displayName = "Active",
        description = "Regular exercise 2-3x/week",
        bmiBonusThreshold = 1f
    ),
    ATHLETIC(
        displayName = "Athletic",
        description = "Intense training 4-6x/week",
        bmiBonusThreshold = 2f
    ),
    ENDURANCE_ATHLETE(
        displayName = "Endurance Athlete",
        description = "Professional/competitive athlete",
        bmiBonusThreshold = 3f
    )
}