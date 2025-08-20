package com.progresspal.app.domain.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Statistics(
    val currentWeight: Float,
    val targetWeight: Float?,
    val initialWeight: Float,
    val weightProgress: Float?, // percentage
    val bmi: Float,
    val bmiCategory: String,
    val weeklyAverageChange: Float?,
    val estimatedDaysToGoal: Int?,
    val totalWeightEntries: Int,
    val totalMeasurementEntries: Int
) : Parcelable

@Parcelize
data class QuickStat(
    val label: String,
    val value: String,
    val change: String? = null,
    val isPositive: Boolean = true
) : Parcelable