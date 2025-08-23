package com.progresspal.app.presentation.bloodpressure

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.progresspal.app.R
import com.progresspal.app.data.database.entities.BloodPressureEntity
import com.progresspal.app.data.database.entities.BloodPressureCategory
import com.progresspal.app.databinding.CardBloodPressureBinding
import com.progresspal.app.domain.models.MedicalGuidelines
import com.progresspal.app.domain.models.ActivityLevel
import com.progresspal.app.utils.BloodPressureUtils
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Custom view for displaying blood pressure information on the dashboard
 * Implements Material Design 3 patterns with color-coded health categories
 * Handles both empty and populated states with appropriate user guidance
 */
class BloodPressureCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val binding: CardBloodPressureBinding
    private var onAddMeasurementClicked: (() -> Unit)? = null
    private var onViewHistoryClicked: (() -> Unit)? = null
    private var onViewTrendsClicked: (() -> Unit)? = null
    
    private var currentReading: BloodPressureEntity? = null
    private var userAge: Int? = null
    private var userActivityLevel: ActivityLevel = ActivityLevel.ACTIVE
    private var medicalGuidelines: MedicalGuidelines = MedicalGuidelines.US_AHA
    
    init {
        binding = CardBloodPressureBinding.inflate(LayoutInflater.from(context), this, true)
        setupClickListeners()
        showNoDataState()
    }
    
    private fun setupClickListeners() {
        binding.btnAddMeasurement.setOnClickListener {
            onAddMeasurementClicked?.invoke()
        }
        
        binding.btnGetStarted.setOnClickListener {
            onAddMeasurementClicked?.invoke()
        }
        
        binding.btnViewHistory.setOnClickListener {
            onViewHistoryClicked?.invoke()
        }
        
        binding.btnViewTrends.setOnClickListener {
            onViewTrendsClicked?.invoke()
        }
        
        // Make entire card clickable to view history when data exists
        binding.cardBloodPressure.setOnClickListener {
            if (currentReading != null) {
                onViewHistoryClicked?.invoke()
            }
        }
    }
    
    /**
     * Set callback for when user wants to add a new measurement
     */
    fun setOnAddMeasurementClickListener(callback: () -> Unit) {
        onAddMeasurementClicked = callback
    }
    
    /**
     * Set callback for when user wants to view measurement history
     */
    fun setOnViewHistoryClickListener(callback: () -> Unit) {
        onViewHistoryClicked = callback
    }
    
    /**
     * Set callback for when user wants to view trends and charts
     */
    fun setOnViewTrendsClickListener(callback: () -> Unit) {
        onViewTrendsClicked = callback
    }
    
    /**
     * Update the card with the latest blood pressure reading
     * @param reading The latest blood pressure reading, or null if no readings exist
     */
    fun updateWithLatestReading(reading: BloodPressureEntity?) {
        currentReading = reading
        
        if (reading == null) {
            showNoDataState()
        } else {
            showDataState(reading)
        }
    }
    
    /**
     * Update health settings for enhanced blood pressure assessment
     * @param age User's age (null if not available)
     * @param activityLevel User's activity level
     * @param guidelines Medical guidelines to use
     */
    fun updateHealthSettings(
        age: Int?,
        activityLevel: ActivityLevel,
        guidelines: MedicalGuidelines
    ) {
        this.userAge = age
        this.userActivityLevel = activityLevel
        this.medicalGuidelines = guidelines
        
        // Refresh display with current reading if available
        currentReading?.let { showDataState(it) }
    }
    
    /**
     * Update with reading and trend information
     * @param reading The latest reading
     * @param trend Trend description (e.g., "Improving", "Stable", "Worsening")
     */
    fun updateWithTrend(reading: BloodPressureEntity?, trend: String?) {
        updateWithLatestReading(reading)
        
        if (reading != null && !trend.isNullOrEmpty() && trend != "Insufficient data") {
            binding.tvTrend.visibility = View.VISIBLE
            binding.tvTrend.text = getTrendDisplayText(trend)
            binding.tvTrend.setTextColor(getTrendColor(trend))
        } else {
            binding.tvTrend.visibility = View.GONE
        }
    }
    
    private fun showNoDataState() {
        binding.layoutNoData.visibility = View.VISIBLE
        binding.layoutHasData.visibility = View.GONE
        binding.cardAlert.visibility = View.GONE
        
        binding.tvLastUpdated.text = "No measurements yet"
        binding.btnAddMeasurement.setIconResource(R.drawable.ic_add)
    }
    
    private fun showDataState(reading: BloodPressureEntity) {
        binding.layoutNoData.visibility = View.GONE
        binding.layoutHasData.visibility = View.VISIBLE
        
        // Update basic reading information
        binding.tvSystolic.text = reading.systolic.toString()
        binding.tvDiastolic.text = reading.diastolic.toString()
        binding.tvPulse.text = reading.pulse.toString()
        
        // Update last measured time
        binding.tvLastUpdated.text = getFormattedTimeAgo(reading.timestamp)
        
        // Update time of day
        binding.tvTimeOfDay.text = formatTimeOfDay(reading.timeOfDay)
        
        // Get enhanced assessment
        val assessment = reading.getFullAssessment(userAge, medicalGuidelines)
        
        // Update category chip with enhanced assessment
        updateCategoryChip(assessment.category, assessment.isAgeAdjusted)
        
        // Update colors based on category
        updateColorsForCategory(assessment.category)
        
        // Update guidelines indicator
        updateGuidelinesIndicator(assessment)
        
        // Show alert if high blood pressure (using enhanced logic)
        updateAlertSection(reading, assessment)
        
        // Update add button icon
        binding.btnAddMeasurement.setIconResource(R.drawable.ic_add)
    }
    
    private fun updateCategoryChip(category: BloodPressureCategory, isAgeAdjusted: Boolean = false) {
        val categoryText = if (isAgeAdjusted) {
            "${category.displayName} (age-adjusted)"
        } else {
            category.displayName
        }
        binding.chipCategory.text = categoryText
        
        val colorRes = when (category) {
            BloodPressureCategory.OPTIMAL -> R.color.green_500
            BloodPressureCategory.NORMAL -> R.color.green_400
            BloodPressureCategory.ELEVATED -> R.color.yellow_600
            BloodPressureCategory.STAGE_1 -> R.color.orange_500
            BloodPressureCategory.STAGE_2 -> R.color.deep_orange_500
            BloodPressureCategory.CRISIS -> R.color.red_500
        }
        
        val color = ContextCompat.getColor(context, colorRes)
        binding.chipCategory.chipIconTint = ColorStateList.valueOf(color)
        binding.chipCategory.setChipBackgroundColorResource(
            when (category.priority) {
                1, 2 -> R.color.green_50  // Optimal, Normal
                3 -> R.color.yellow_50     // Elevated
                4 -> R.color.orange_50     // Stage 1
                5, 6 -> R.color.red_50     // Stage 2, Crisis
                else -> R.color.gray_50
            }
        )
    }
    
    private fun updateColorsForCategory(category: BloodPressureCategory) {
        val colorRes = when (category) {
            BloodPressureCategory.OPTIMAL -> R.color.green_500
            BloodPressureCategory.NORMAL -> R.color.green_400
            BloodPressureCategory.ELEVATED -> R.color.yellow_600
            BloodPressureCategory.STAGE_1 -> R.color.orange_500
            BloodPressureCategory.STAGE_2 -> R.color.deep_orange_500
            BloodPressureCategory.CRISIS -> R.color.red_500
        }
        
        val color = ContextCompat.getColor(context, colorRes)
        
        // Apply subtle color tinting to main readings for severe cases
        if (category.priority >= 5) { // Stage 2 or Crisis
            binding.tvSystolic.setTextColor(color)
            binding.tvDiastolic.setTextColor(color)
        } else {
            // Use default text color for normal readings
            val defaultColor = ContextCompat.getColor(context, R.color.text_primary)
            binding.tvSystolic.setTextColor(defaultColor)
            binding.tvDiastolic.setTextColor(defaultColor)
        }
    }
    
    private fun updateAlertSection(reading: BloodPressureEntity, assessment: com.progresspal.app.utils.BloodPressureAssessment) {
        val requiresAttention = reading.requiresImmediateAttention(userAge, medicalGuidelines) ||
                               assessment.category.requiresMedicalAttention()
        
        if (requiresAttention) {
            binding.cardAlert.visibility = View.VISIBLE
            
            // Use personalized message from enhanced assessment
            val message = reading.getPersonalizedMessage(userAge, userActivityLevel, medicalGuidelines)
            
            binding.tvAlertMessage.text = message
        } else {
            binding.cardAlert.visibility = View.GONE
        }
    }
    
    private fun updateGuidelinesIndicator(assessment: com.progresspal.app.utils.BloodPressureAssessment) {
        binding.tvGuidelines.visibility = View.VISIBLE
        binding.tvGuidelines.text = BloodPressureUtils.getGuidelinesDisplayString(
            medicalGuidelines, 
            userAge
        )
    }
    
    private fun getFormattedTimeAgo(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        
        return when {
            diff < TimeUnit.MINUTES.toMillis(1) -> "Just now"
            diff < TimeUnit.HOURS.toMillis(1) -> {
                val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
                "${minutes} minute${if (minutes != 1L) "s" else ""} ago"
            }
            diff < TimeUnit.DAYS.toMillis(1) -> {
                val hours = TimeUnit.MILLISECONDS.toHours(diff)
                "${hours} hour${if (hours != 1L) "s" else ""} ago"
            }
            diff < TimeUnit.DAYS.toMillis(7) -> {
                val days = TimeUnit.MILLISECONDS.toDays(diff)
                "${days} day${if (days != 1L) "s" else ""} ago"
            }
            else -> {
                val dateFormat = SimpleDateFormat("MMM d", Locale.getDefault())
                "on ${dateFormat.format(Date(timestamp))}"
            }
        }
    }
    
    private fun formatTimeOfDay(timeOfDay: String): String {
        return when (timeOfDay) {
            BloodPressureEntity.TIME_MORNING -> "Morning"
            BloodPressureEntity.TIME_AFTERNOON -> "Afternoon"
            BloodPressureEntity.TIME_EVENING -> "Evening"
            else -> timeOfDay.replaceFirstChar { it.uppercase() }
        }
    }
    
    private fun getTrendDisplayText(trend: String): String {
        return when (trend.lowercase()) {
            "improving" -> "↘ Improving"
            "worsening" -> "↗ Worsening"
            "stable" -> "→ Stable"
            else -> trend
        }
    }
    
    private fun getTrendColor(trend: String): Int {
        return when (trend.lowercase()) {
            "improving" -> ContextCompat.getColor(context, R.color.green_600)
            "worsening" -> ContextCompat.getColor(context, R.color.red_600)
            "stable" -> ContextCompat.getColor(context, R.color.blue_600)
            else -> ContextCompat.getColor(context, R.color.text_secondary)
        }
    }
    
    /**
     * Show loading state while data is being fetched
     */
    fun showLoading() {
        binding.layoutNoData.visibility = View.GONE
        binding.layoutHasData.visibility = View.GONE
        binding.tvLastUpdated.text = "Loading..."
    }
    
    /**
     * Show error state when data loading fails
     */
    fun showError(message: String = "Unable to load blood pressure data") {
        binding.layoutNoData.visibility = View.VISIBLE
        binding.layoutHasData.visibility = View.GONE
        binding.cardAlert.visibility = View.GONE
        
        binding.tvLastUpdated.text = message
        binding.btnGetStarted.text = "Retry"
    }
    
    /**
     * Get the current reading displayed in the card
     */
    fun getCurrentReading(): BloodPressureEntity? = currentReading
    
    /**
     * Check if the card is showing data or empty state
     */
    fun hasData(): Boolean = currentReading != null
    
    /**
     * Force refresh of the display with current data
     */
    fun refresh() {
        currentReading?.let { updateWithLatestReading(it) }
    }
    
    companion object {
        private const val TAG = "BloodPressureCardView"
    }
}