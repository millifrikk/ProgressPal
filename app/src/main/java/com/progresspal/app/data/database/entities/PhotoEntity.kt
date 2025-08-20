package com.progresspal.app.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "photos",
    indices = [
        Index(value = ["user_id"]),
        Index(value = ["weight_id"]),
        Index(value = ["date"]),
        Index(value = ["user_id", "date"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("user_id"),
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = WeightEntity::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("weight_id"),
            onDelete = ForeignKey.SET_NULL
        )
    ]
)
data class PhotoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "user_id")
    val userId: Long,
    
    @ColumnInfo(name = "weight_id")
    val weightId: Long? = null,
    
    @ColumnInfo(name = "photo_uri")
    val photoUri: String,
    
    @ColumnInfo(name = "photo_type")
    val photoType: String? = null, // 'front', 'side', 'back'
    
    val date: Date,
    val notes: String? = null,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Date = Date()
)