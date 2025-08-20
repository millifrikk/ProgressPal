package com.progresspal.app.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val name: String? = null,
    val age: Int? = null,
    val height: Float, // in cm
    val gender: String? = null,
    
    @ColumnInfo(name = "activity_level")
    val activityLevel: String? = null,
    
    @ColumnInfo(name = "initial_weight")
    val initialWeight: Float, // in kg
    
    @ColumnInfo(name = "current_weight")
    val currentWeight: Float? = null, // in kg
    
    @ColumnInfo(name = "target_weight")
    val targetWeight: Float? = null, // in kg
    
    @ColumnInfo(name = "initial_waist")
    val initialWaist: Float? = null, // in cm
    
    @ColumnInfo(name = "initial_chest")
    val initialChest: Float? = null, // in cm
    
    @ColumnInfo(name = "initial_hips")
    val initialHips: Float? = null, // in cm
    
    @ColumnInfo(name = "target_waist")
    val targetWaist: Float? = null, // in cm
    
    @ColumnInfo(name = "target_chest")
    val targetChest: Float? = null, // in cm
    
    @ColumnInfo(name = "target_hips")
    val targetHips: Float? = null, // in cm
    
    @ColumnInfo(name = "track_measurements")
    val trackMeasurements: Boolean = false,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Date = Date(),
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: Date = Date()
)