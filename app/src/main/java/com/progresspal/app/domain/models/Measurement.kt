package com.progresspal.app.domain.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class Measurement(
    val id: Long = 0,
    val userId: Long,
    val measurementType: String, // 'waist', 'chest', 'hips', 'neck', 'biceps_left', etc.
    val value: Float, // in cm
    val date: Date,
    val time: String? = null,
    val notes: String? = null,
    val side: String? = null, // 'left', 'right', or null for central measurements
    val createdAt: Date = Date()
) : Parcelable

enum class MeasurementType(val displayName: String) {
    WAIST("Waist"),
    CHEST("Chest"),
    HIPS("Hips"),
    NECK("Neck"),
    BICEPS_LEFT("Biceps (Left)"),
    BICEPS_RIGHT("Biceps (Right)"),
    THIGH_LEFT("Thigh (Left)"),
    THIGH_RIGHT("Thigh (Right)"),
    FOREARM_LEFT("Forearm (Left)"),
    FOREARM_RIGHT("Forearm (Right)")
}