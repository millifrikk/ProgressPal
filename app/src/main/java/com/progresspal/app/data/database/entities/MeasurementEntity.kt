package com.progresspal.app.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "measurements",
    indices = [
        Index(value = ["user_id"]),
        Index(value = ["measurement_type"]),
        Index(value = ["date"]),
        Index(value = ["user_id", "measurement_type"]),
        Index(value = ["user_id", "date"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("user_id"),
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class MeasurementEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "user_id")
    val userId: Long,
    
    @ColumnInfo(name = "measurement_type")
    val measurementType: String, // 'waist', 'chest', 'hips', 'neck', 'biceps_left', etc.
    
    val value: Float, // in cm
    val date: Date,
    val time: String? = null,
    val notes: String? = null,
    val side: String? = null, // 'left', 'right', or null for central measurements
    
    @ColumnInfo(name = "created_at")
    val createdAt: Date = Date()
)