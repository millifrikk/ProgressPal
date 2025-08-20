package com.progresspal.app.domain.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class Goal(
    val id: Long = 0,
    val userId: Long,
    val goalType: String, // 'weight', 'waist', 'chest', etc.
    val currentValue: Float,
    val targetValue: Float,
    val targetDate: Date? = null,
    val achieved: Boolean = false,
    val achievedDate: Date? = null,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
) : Parcelable