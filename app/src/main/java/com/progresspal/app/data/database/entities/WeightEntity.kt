package com.progresspal.app.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "weights",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("user_id"),
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class WeightEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "user_id")
    val userId: Long,
    
    val weight: Float, // in kg
    val date: Date,
    val time: String? = null,
    val notes: String? = null,
    
    @ColumnInfo(name = "photo_uri")
    val photoUri: String? = null,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Date = Date()
)