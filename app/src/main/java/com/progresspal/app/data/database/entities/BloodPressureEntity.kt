package com.progresspal.app.data.database.entities

import androidx.room.*
import com.progresspal.app.domain.models.MedicalGuidelines
import com.progresspal.app.domain.models.ActivityLevel
import com.progresspal.app.utils.BloodPressureUtils
import java.util.*

/**
 * Room entity representing blood pressure and pulse measurements
 * Follows Material Design guidelines for vital signs tracking
 * 
 * References:
 * - American Heart Association BP categories
 * - AndroidX Room best practices for foreign keys and indices
 */
@Entity(
    tableName = "blood_pressure",
    indices = [
        Index(value = ["userId", "timestamp"], name = "index_bp_user_time"),
        Index(value = ["userId"], name = "index_bp_user")
    ],
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class BloodPressureEntity(
    @PrimaryKey 
    @ColumnInfo(name = "id")
    val id: String = UUID.randomUUID().toString(),
    
    @ColumnInfo(name = "userId")
    val userId: Long,
    
    /**
     * Systolic pressure (top number) in mmHg
     * Normal range: 90-200 mmHg
     */
    @ColumnInfo(name = "systolic")
    val systolic: Int,
    
    /**
     * Diastolic pressure (bottom number) in mmHg  
     * Normal range: 60-130 mmHg
     */
    @ColumnInfo(name = "diastolic")
    val diastolic: Int,
    
    /**
     * Heart rate in beats per minute
     * Normal range: 40-200 bpm
     */
    @ColumnInfo(name = "pulse")
    val pulse: Int,
    
    @ColumnInfo(name = "timestamp")
    val timestamp: Long = System.currentTimeMillis(),
    
    /**
     * Time of day measurement was taken
     * Values: "morning", "afternoon", "evening"
     */
    @ColumnInfo(name = "timeOfDay")
    val timeOfDay: String,
    
    /**
     * Optional tags for context (JSON array)
     * Examples: ["before_meal", "after_exercise", "stressed", "relaxed"]
     */
    @ColumnInfo(name = "tags")
    val tags: String? = null,
    
    /**
     * Optional user notes about the measurement
     */
    @ColumnInfo(name = "notes")
    val notes: String? = null
) {
    companion object {
        // Time of day constants
        const val TIME_MORNING = "morning"
        const val TIME_AFTERNOON = "afternoon" 
        const val TIME_EVENING = "evening"
        
        // Common tags
        const val TAG_BEFORE_MEAL = "before_meal"
        const val TAG_AFTER_MEAL = "after_meal"
        const val TAG_AFTER_EXERCISE = "after_exercise"
        const val TAG_STRESSED = "stressed"
        const val TAG_RELAXED = "relaxed"
        const val TAG_MEDICATION = "medication"
    }
    
    /**
     * Get blood pressure category based on AHA guidelines
     */
    fun getCategory(): BloodPressureCategory {
        return when {
            systolic >= 180 || diastolic >= 120 -> BloodPressureCategory.CRISIS
            systolic >= 140 || diastolic >= 90 -> BloodPressureCategory.STAGE_2
            systolic >= 130 || diastolic >= 80 -> BloodPressureCategory.STAGE_1
            systolic >= 120 && diastolic < 80 -> BloodPressureCategory.ELEVATED
            systolic < 120 && diastolic < 80 -> BloodPressureCategory.OPTIMAL
            else -> BloodPressureCategory.NORMAL
        }
    }
    
    /**
     * Check if pulse is within normal range
     */
    fun isPulseNormal(): Boolean {
        return pulse in 60..100
    }
    
    /**
     * Get formatted display string for blood pressure
     */
    fun getFormattedReading(): String {
        return "$systolic/$diastolic mmHg"
    }
    
    /**
     * Check if this is a high priority reading requiring attention
     */
    fun requiresAttention(): Boolean {
        val category = getCategory()
        return category == BloodPressureCategory.CRISIS || 
               category == BloodPressureCategory.STAGE_2 ||
               pulse < 50 || pulse > 120
    }
    
    /**
     * Get blood pressure category using enhanced multi-standard assessment
     * @param age User's age (null if not available)
     * @param guidelines Medical guidelines to use
     */
    fun getCategoryWithGuidelines(
        age: Int?,
        guidelines: MedicalGuidelines
    ): BloodPressureCategory {
        val assessment = BloodPressureUtils.getAgeAdjustedCategory(
            systolic = systolic,
            diastolic = diastolic,
            age = age,
            guidelines = guidelines
        )
        return assessment.category
    }
    
    /**
     * Get comprehensive blood pressure assessment with age and guidelines
     */
    fun getFullAssessment(
        age: Int?,
        guidelines: MedicalGuidelines
    ): com.progresspal.app.utils.BloodPressureAssessment {
        return BloodPressureUtils.getAgeAdjustedCategory(
            systolic = systolic,
            diastolic = diastolic,
            age = age,
            guidelines = guidelines
        )
    }
    
    /**
     * Get personalized health message
     * @param age User's age
     * @param activityLevel User's activity level
     * @param guidelines Medical guidelines to use
     */
    fun getPersonalizedMessage(
        age: Int?,
        activityLevel: ActivityLevel,
        guidelines: MedicalGuidelines
    ): String {
        return BloodPressureUtils.getPersonalizedBPMessage(
            reading = this,
            age = age,
            activityLevel = activityLevel,
            guidelines = guidelines
        )
    }
    
    /**
     * Check if reading requires immediate attention using enhanced logic
     */
    fun requiresImmediateAttention(
        age: Int?,
        guidelines: MedicalGuidelines
    ): Boolean {
        return BloodPressureUtils.requiresImmediateAttention(
            systolic = systolic,
            diastolic = diastolic,
            pulse = pulse,
            age = age,
            guidelines = guidelines
        )
    }
}

/**
 * Blood pressure categories following American Heart Association guidelines
 */
enum class BloodPressureCategory(
    val displayName: String,
    val description: String,
    val colorHex: String,
    val priority: Int
) {
    OPTIMAL(
        displayName = "Optimal",
        description = "Less than 120/80 mmHg",
        colorHex = "#4CAF50", // Green
        priority = 1
    ),
    NORMAL(
        displayName = "Normal", 
        description = "120-129/80-84 mmHg",
        colorHex = "#8BC34A", // Light Green
        priority = 2
    ),
    ELEVATED(
        displayName = "Elevated",
        description = "120-129 and less than 80 mmHg",
        colorHex = "#FFEB3B", // Yellow
        priority = 3
    ),
    STAGE_1(
        displayName = "Stage 1 Hypertension",
        description = "130-139/80-89 mmHg", 
        colorHex = "#FF9800", // Orange
        priority = 4
    ),
    STAGE_2(
        displayName = "Stage 2 Hypertension",
        description = "140/90 mmHg or higher",
        colorHex = "#FF5722", // Deep Orange
        priority = 5
    ),
    CRISIS(
        displayName = "Hypertensive Crisis",
        description = "Higher than 180/120 mmHg",
        colorHex = "#F44336", // Red
        priority = 6
    );
    
    /**
     * Get recommendation message for this category
     */
    fun getRecommendation(): String {
        return when (this) {
            OPTIMAL -> "Maintain healthy lifestyle to keep optimal blood pressure."
            NORMAL -> "Continue healthy habits. Monitor regularly."
            ELEVATED -> "Adopt healthy lifestyle changes to prevent hypertension."
            STAGE_1 -> "Lifestyle changes and possible medication. Consult healthcare provider."
            STAGE_2 -> "Combination of lifestyle changes and medication typically needed."
            CRISIS -> "Seek immediate medical attention. This is a medical emergency."
        }
    }
    
    /**
     * Check if this category warrants medical consultation
     */
    fun requiresMedicalAttention(): Boolean {
        return this == STAGE_1 || this == STAGE_2 || this == CRISIS
    }
}

/**
 * Data class for blood pressure averages and statistics
 */
data class BloodPressureAverages(
    val avgSystolic: Double,
    val avgDiastolic: Double,
    val avgPulse: Double,
    val readingCount: Int
) {
    fun getFormattedAverage(): String {
        return "${avgSystolic.toInt()}/${avgDiastolic.toInt()} mmHg"
    }
    
    fun getAverageCategory(): BloodPressureCategory {
        return when {
            avgSystolic >= 180 || avgDiastolic >= 120 -> BloodPressureCategory.CRISIS
            avgSystolic >= 140 || avgDiastolic >= 90 -> BloodPressureCategory.STAGE_2
            avgSystolic >= 130 || avgDiastolic >= 80 -> BloodPressureCategory.STAGE_1
            avgSystolic >= 120 && avgDiastolic < 80 -> BloodPressureCategory.ELEVATED
            avgSystolic < 120 && avgDiastolic < 80 -> BloodPressureCategory.OPTIMAL
            else -> BloodPressureCategory.NORMAL
        }
    }
}