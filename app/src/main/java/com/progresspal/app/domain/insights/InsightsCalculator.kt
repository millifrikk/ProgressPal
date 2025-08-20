package com.progresspal.app.domain.insights

import com.progresspal.app.data.database.entities.WeightEntity
import com.progresspal.app.domain.insights.models.InsightResult
import com.progresspal.app.domain.insights.models.ProgressPattern
import com.progresspal.app.domain.insights.models.WeightTrend
import java.util.*
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Calculates various insights and patterns from weight data
 * Provides analytics for user progress tracking and motivation
 * Optimized: Added caching and improved algorithm complexity
 */
class InsightsCalculator {
    
    // Cache for expensive calculations
    private val regressionCache = mutableMapOf<String, Float>()
    private val insightCache = mutableMapOf<String, CachedInsightResult>()
    
    data class CachedInsightResult(
        val result: InsightResult,
        val timestamp: Long,
        val dataHash: Int
    ) {
        fun isExpired(maxAge: Long = 5 * 60 * 1000): Boolean { // 5 minutes
            return System.currentTimeMillis() - timestamp > maxAge
        }
    }
    
    companion object {
        private const val MIN_DATA_POINTS = 3
        private const val TREND_WINDOW_DAYS = 14
        private const val PLATEAU_THRESHOLD_DAYS = 21
        private const val SIGNIFICANT_WEIGHT_CHANGE = 1.0f // kg
    }
    
    /**
     * Analyze weight data and generate comprehensive insights
     * Optimized: Added caching to prevent redundant calculations
     */
    suspend fun generateInsights(weightEntries: List<WeightEntity>): InsightResult {
        if (weightEntries.size < MIN_DATA_POINTS) {
            return InsightResult.insufficientData()
        }
        
        val sortedEntries = weightEntries.sortedBy { it.date }
        val dataHash = sortedEntries.hashCode()
        val cacheKey = "insights_$dataHash"
        
        // Check cache first
        insightCache[cacheKey]?.let { cached ->
            if (!cached.isExpired() && cached.dataHash == dataHash) {
                return cached.result
            }
        }
        
        // Calculate insights with optimized algorithms
        val result = InsightResult(
            overallTrend = calculateOverallTrend(sortedEntries),
            recentTrend = calculateRecentTrend(sortedEntries),
            totalWeightChange = calculateTotalWeightChange(sortedEntries),
            averageWeightLoss = calculateAverageWeightLoss(sortedEntries),
            bestWeekProgress = findBestWeekProgressOptimized(sortedEntries),
            currentStreak = calculateCurrentStreak(sortedEntries),
            patterns = detectPatterns(sortedEntries),
            milestones = detectMilestones(sortedEntries),
            predictions = generatePredictions(sortedEntries),
            tips = generatePersonalizedTips(sortedEntries)
        )
        
        // Cache the result
        insightCache[cacheKey] = CachedInsightResult(
            result = result,
            timestamp = System.currentTimeMillis(),
            dataHash = dataHash
        )
        
        return result
    }
    
    /**
     * Calculate overall weight trend using linear regression
     */
    private fun calculateOverallTrend(entries: List<WeightEntity>): WeightTrend {
        if (entries.size < 2) return WeightTrend.STABLE
        
        val slope = calculateLinearRegressionSlope(entries)
        
        return when {
            slope < -0.1f -> WeightTrend.LOSING
            slope > 0.1f -> WeightTrend.GAINING
            else -> WeightTrend.STABLE
        }
    }
    
    /**
     * Calculate recent trend (last 2 weeks)
     */
    private fun calculateRecentTrend(entries: List<WeightEntity>): WeightTrend {
        val recentEntries = getRecentEntries(entries, TREND_WINDOW_DAYS)
        return calculateOverallTrend(recentEntries)
    }
    
    /**
     * Calculate total weight change from first to last entry
     */
    private fun calculateTotalWeightChange(entries: List<WeightEntity>): Float {
        if (entries.isEmpty()) return 0f
        return entries.last().weight - entries.first().weight
    }
    
    /**
     * Calculate average weight loss per week
     */
    private fun calculateAverageWeightLoss(entries: List<WeightEntity>): Float {
        if (entries.size < 2) return 0f
        
        val totalChange = calculateTotalWeightChange(entries)
        val daysBetween = daysBetween(entries.first().date, entries.last().date)
        val weeks = daysBetween / 7.0f
        
        return if (weeks > 0) -totalChange / weeks else 0f
    }
    
    /**
     * Find the week with best progress - OPTIMIZED VERSION
     * Changed from O(n²) to O(n) using sliding window approach
     */
    private fun findBestWeekProgressOptimized(entries: List<WeightEntity>): Float {
        if (entries.size < 2) return 0f
        
        var bestWeeklyChange = 0f
        val sortedEntries = entries.sortedBy { it.date }
        
        // Use sliding window approach - O(n) instead of O(n²)
        var startIndex = 0
        
        for (endIndex in 1 until sortedEntries.size) {
            val daysDiff = daysBetween(sortedEntries[startIndex].date, sortedEntries[endIndex].date)
            
            when {
                daysDiff in 6..8 -> {
                    // Perfect 7-day window found
                    val weeklyChange = sortedEntries[startIndex].weight - sortedEntries[endIndex].weight
                    bestWeeklyChange = maxOf(bestWeeklyChange, weeklyChange)
                    startIndex++
                }
                daysDiff < 6 -> {
                    // Window too small, keep expanding
                    continue
                }
                else -> {
                    // Window too large, move start forward
                    startIndex++
                    if (startIndex >= endIndex) {
                        startIndex = endIndex - 1
                    }
                }
            }
        }
        
        return bestWeeklyChange
    }
    
    /**
     * Legacy method kept for compatibility
     * @deprecated Use findBestWeekProgressOptimized instead
     */
    @Deprecated("Use optimized version", ReplaceWith("findBestWeekProgressOptimized(entries)"))
    private fun findBestWeekProgress(entries: List<WeightEntity>): Float {
        return findBestWeekProgressOptimized(entries)
    }
    
    /**
     * Calculate current consecutive streak of weight loss/gain
     */
    private fun calculateCurrentStreak(entries: List<WeightEntity>): Int {
        if (entries.size < 2) return 0
        
        val sortedEntries = entries.sortedByDescending { it.date }
        var streak = 0
        var lastTrend: WeightTrend? = null
        
        for (i in 0 until sortedEntries.size - 1) {
            val currentWeight = sortedEntries[i].weight
            val previousWeight = sortedEntries[i + 1].weight
            val change = previousWeight - currentWeight
            
            val currentTrend = when {
                change > 0.2f -> WeightTrend.LOSING
                change < -0.2f -> WeightTrend.GAINING
                else -> WeightTrend.STABLE
            }
            
            if (lastTrend == null) {
                lastTrend = currentTrend
                if (currentTrend != WeightTrend.STABLE) streak = 1
            } else if (currentTrend == lastTrend && currentTrend != WeightTrend.STABLE) {
                streak++
            } else {
                break
            }
        }
        
        return streak
    }
    
    /**
     * Detect patterns in weight data
     */
    private fun detectPatterns(entries: List<WeightEntity>): List<ProgressPattern> {
        val patterns = mutableListOf<ProgressPattern>()
        
        // Detect plateaus
        detectPlateaus(entries)?.let { patterns.add(it) }
        
        // Detect weekly cycles
        detectWeeklyCycles(entries)?.let { patterns.add(it) }
        
        // Detect rapid changes
        detectRapidChanges(entries)?.let { patterns.add(it) }
        
        return patterns
    }
    
    /**
     * Detect weight plateaus
     */
    private fun detectPlateaus(entries: List<WeightEntity>): ProgressPattern? {
        if (entries.size < PLATEAU_THRESHOLD_DAYS) return null
        
        val recentEntries = getRecentEntries(entries, PLATEAU_THRESHOLD_DAYS)
        val weightVariation = calculateWeightVariation(recentEntries)
        
        return if (weightVariation < 1.0f) {
            ProgressPattern.Plateau(
                duration = PLATEAU_THRESHOLD_DAYS,
                averageWeight = recentEntries.map { it.weight }.average().toFloat()
            )
        } else null
    }
    
    /**
     * Detect weekly weight cycles
     */
    private fun detectWeeklyCycles(entries: List<WeightEntity>): ProgressPattern? {
        // Implementation for detecting weekly patterns
        // This would analyze if there are recurring weekly patterns
        return null // Placeholder
    }
    
    /**
     * Detect rapid weight changes
     */
    private fun detectRapidChanges(entries: List<WeightEntity>): ProgressPattern? {
        if (entries.size < 7) return null
        
        val recentEntries = getRecentEntries(entries, 7)
        val totalChange = abs(recentEntries.last().weight - recentEntries.first().weight)
        
        return if (totalChange > SIGNIFICANT_WEIGHT_CHANGE * 2) {
            ProgressPattern.RapidChange(
                change = recentEntries.last().weight - recentEntries.first().weight,
                days = 7
            )
        } else null
    }
    
    /**
     * Detect milestone achievements
     */
    private fun detectMilestones(entries: List<WeightEntity>): List<String> {
        val milestones = mutableListOf<String>()
        
        if (entries.size < 2) return milestones
        
        val totalLoss = entries.first().weight - entries.last().weight
        val daysTracking = daysBetween(entries.first().date, entries.last().date)
        
        // Weight loss milestones
        when {
            totalLoss >= 20f -> milestones.add("🎉 Amazing! You've lost over 20kg!")
            totalLoss >= 15f -> milestones.add("🎉 Fantastic! You've lost over 15kg!")
            totalLoss >= 10f -> milestones.add("🎉 Great job! You've lost over 10kg!")
            totalLoss >= 5f -> milestones.add("🎉 Well done! You've lost over 5kg!")
            totalLoss >= 2f -> milestones.add("🎉 You're making progress! 2kg lost!")
        }
        
        // Consistency milestones
        when {
            daysTracking >= 365 -> milestones.add("🏆 One year of tracking! You're dedicated!")
            daysTracking >= 180 -> milestones.add("🏆 Six months of tracking! Keep it up!")
            daysTracking >= 90 -> milestones.add("🏆 Three months of tracking! Great habit!")
            daysTracking >= 30 -> milestones.add("🏆 One month of tracking! You're building a habit!")
        }
        
        return milestones
    }
    
    /**
     * Generate weight predictions based on current trends
     */
    private fun generatePredictions(entries: List<WeightEntity>): Map<String, Float> {
        val predictions = mutableMapOf<String, Float>()
        
        if (entries.size < MIN_DATA_POINTS) return predictions
        
        // Reuse cached slope calculation
        val slope = calculateLinearRegressionSlope(entries)
        val currentWeight = entries.last().weight
        
        // Predict weight in 1 week, 1 month, 3 months
        // Add reasonable bounds to predictions to prevent unrealistic values
        val minWeight = entries.minOf { it.weight } - 10f // Allow 10kg below minimum
        val maxWeight = entries.maxOf { it.weight } + 10f // Allow 10kg above maximum
        
        predictions["1_week"] = (currentWeight + (slope * 7)).coerceIn(minWeight, maxWeight)
        predictions["1_month"] = (currentWeight + (slope * 30)).coerceIn(minWeight, maxWeight)
        predictions["3_months"] = (currentWeight + (slope * 90)).coerceIn(minWeight, maxWeight)
        
        return predictions
    }
    
    /**
     * Generate personalized tips based on patterns
     */
    private fun generatePersonalizedTips(entries: List<WeightEntity>): List<String> {
        val tips = mutableListOf<String>()
        
        val recentTrend = calculateRecentTrend(entries)
        val overallTrend = calculateOverallTrend(entries)
        
        when {
            recentTrend == WeightTrend.LOSING && overallTrend == WeightTrend.LOSING -> {
                tips.add("💪 You're doing great! Keep up your current routine.")
                tips.add("📷 Consider taking progress photos to see visual changes.")
            }
            recentTrend == WeightTrend.STABLE && overallTrend == WeightTrend.LOSING -> {
                tips.add("⚖️ You might be hitting a plateau. Try varying your routine.")
                tips.add("🍎 Consider adjusting your calorie intake slightly.")
            }
            recentTrend == WeightTrend.GAINING -> {
                tips.add("🎯 Focus on consistency in your diet and exercise.")
                tips.add("💧 Make sure you're staying hydrated.")
                tips.add("😴 Ensure you're getting adequate sleep.")
            }
            else -> {
                tips.add("📈 Keep tracking your progress consistently.")
                tips.add("🎯 Set small, achievable weekly goals.")
            }
        }
        
        return tips
    }
    
    // Helper functions
    
    private fun calculateLinearRegressionSlope(entries: List<WeightEntity>): Float {
        if (entries.size < 2) return 0f
        
        // Use caching for expensive regression calculations
        val cacheKey = "regression_${entries.hashCode()}"
        regressionCache[cacheKey]?.let { return it }
        
        val n = entries.size
        val xValues = entries.indices.map { it.toFloat() }
        val yValues = entries.map { it.weight }
        
        val xMean = xValues.average().toFloat()
        val yMean = yValues.average().toFloat()
        
        val numerator = xValues.zip(yValues) { x, y -> (x - xMean) * (y - yMean) }.sum()
        val denominator = xValues.map { (it - xMean).pow(2) }.sum()
        
        val slope = if (denominator != 0f) numerator / denominator else 0f
        
        // Cache the result
        regressionCache[cacheKey] = slope
        
        // Limit cache size to prevent memory growth
        if (regressionCache.size > 100) {
            regressionCache.clear()
        }
        
        return slope
    }
    
    private fun getRecentEntries(entries: List<WeightEntity>, days: Int): List<WeightEntity> {
        val cutoffDate = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -days)
        }.time
        
        return entries.filter { it.date.after(cutoffDate) }
    }
    
    private fun calculateWeightVariation(entries: List<WeightEntity>): Float {
        if (entries.isEmpty()) return 0f
        
        val weights = entries.map { it.weight }
        val mean = weights.average().toFloat()
        val variance = weights.map { (it - mean).pow(2) }.average().toFloat()
        
        return sqrt(variance)
    }
    
    private fun daysBetween(startDate: Date, endDate: Date): Int {
        val diffInMillis = endDate.time - startDate.time
        return (diffInMillis / (24 * 60 * 60 * 1000)).toInt()
    }
}