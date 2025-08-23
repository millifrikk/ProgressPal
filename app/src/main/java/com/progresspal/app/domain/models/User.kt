package com.progresspal.app.domain.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class User(
    val id: Long = 0,
    val name: String? = null,
    val age: Int? = null,
    val height: Float, // in cm
    val gender: String? = null,
    val activityLevel: String? = null,
    val initialWeight: Float, // in kg
    val currentWeight: Float? = null, // in kg
    val targetWeight: Float? = null, // in kg
    val initialWaist: Float? = null, // in cm
    val initialChest: Float? = null, // in cm
    val initialHips: Float? = null, // in cm
    val targetWaist: Float? = null, // in cm
    val targetChest: Float? = null, // in cm
    val targetHips: Float? = null, // in cm
    val trackMeasurements: Boolean = false,
    
    // NEW: Health Settings Fields
    val birthDate: Date? = null, // For age calculations
    val waistCircumference: Float? = null, // Current waist for WHtR and BRI
    val hipCircumference: Float? = null, // Current hips for WHR
    val measurementSystem: String = "METRIC", // METRIC or IMPERIAL
    val medicalGuidelines: String = "US_AHA", // US_AHA or EU_ESC
    val preferredLanguage: String = "en", // For localized health messages
    
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
) : Parcelable

enum class Gender {
    MALE, FEMALE, OTHER
}

