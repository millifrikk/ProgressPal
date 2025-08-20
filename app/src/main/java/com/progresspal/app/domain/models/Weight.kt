package com.progresspal.app.domain.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class Weight(
    val id: Long = 0,
    val userId: Long,
    val weight: Float, // in kg
    val date: Date,
    val time: String? = null,
    val notes: String? = null,
    val photoUri: String? = null,
    val createdAt: Date = Date()
) : Parcelable