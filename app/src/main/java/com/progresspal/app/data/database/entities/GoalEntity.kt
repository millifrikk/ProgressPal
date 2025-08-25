package com.progresspal.app.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "goals",
    indices = [
        Index(value = ["user_id"]),
        Index(value = ["goal_type"]),
        Index(value = ["achieved"]),
        Index(value = ["user_id", "goal_type"]),
        Index(value = ["user_id", "achieved"])
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
data class GoalEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "user_id")
    val userId: Long,
    
    @ColumnInfo(name = "goal_type")
    val goalType: String, // 'weight', 'waist', 'chest', etc.
    
    @ColumnInfo(name = "current_value")
    val currentValue: Float,
    
    @ColumnInfo(name = "target_value")
    val targetValue: Float,
    
    @ColumnInfo(name = "target_date")
    val targetDate: Date? = null,
    
    val achieved: Boolean = false,
    
    @ColumnInfo(name = "achieved_date")
    val achievedDate: Date? = null,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Date = Date(),
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: Date = Date(),
    
    // AI Enhancement Fields
    @ColumnInfo(name = "ai_suggested")
    val aiSuggested: Boolean = false,
    
    @ColumnInfo(name = "ai_reasoning")
    val aiReasoning: String? = null,
    
    @ColumnInfo(name = "milestones")
    val milestones: String? = null, // JSON string of milestone objects
    
    @ColumnInfo(name = "difficulty_score")
    val difficultyScore: Int? = null, // 1-10 scale, AI-generated difficulty assessment
    
    @ColumnInfo(name = "personalization_factors")
    val personalizationFactors: String? = null, // JSON string of factors used for personalization
    
    @ColumnInfo(name = "ai_confidence_score")
    val aiConfidenceScore: Float? = null // 0.0-1.0 AI confidence in suggestion quality
)