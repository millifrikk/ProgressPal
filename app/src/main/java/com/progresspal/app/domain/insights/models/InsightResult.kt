package com.progresspal.app.domain.insights.models

/**
 * Contains all calculated insights for a user's weight data
 */
data class InsightResult(
    val overallTrend: WeightTrend,
    val recentTrend: WeightTrend,
    val totalWeightChange: Float,
    val averageWeightLoss: Float, // per week
    val bestWeekProgress: Float,
    val currentStreak: Int,
    val patterns: List<ProgressPattern>,
    val milestones: List<String>,
    val predictions: Map<String, Float>, // time period -> predicted weight
    val tips: List<String>,
    val hasInsufficientData: Boolean = false
) {
    companion object {
        fun insufficientData(): InsightResult {
            return InsightResult(
                overallTrend = WeightTrend.STABLE,
                recentTrend = WeightTrend.STABLE,
                totalWeightChange = 0f,
                averageWeightLoss = 0f,
                bestWeekProgress = 0f,
                currentStreak = 0,
                patterns = emptyList(),
                milestones = emptyList(),
                predictions = emptyMap(),
                tips = listOf(
                    "📊 Keep tracking your weight to unlock insights!",
                    "🎯 Aim to log your weight at least 3 times to see patterns.",
                    "📈 Consistent tracking leads to better insights."
                ),
                hasInsufficientData = true
            )
        }
    }
    
    /**
     * Get user-friendly summary of progress
     */
    fun getProgressSummary(): String {
        return when {
            hasInsufficientData -> "Start tracking to see your progress!"
            totalWeightChange < -0.5f -> "You're making great progress! ${kotlin.math.abs(totalWeightChange).format(1)}kg lost!"
            totalWeightChange > 0.5f -> "Focus on consistency. ${totalWeightChange.format(1)}kg gained recently."
            else -> "Your weight is stable. Keep up the good work!"
        }
    }
    
    /**
     * Get trend description
     */
    fun getTrendDescription(): String {
        return when (recentTrend) {
            WeightTrend.LOSING -> "📉 Recent trend: Losing weight"
            WeightTrend.GAINING -> "📈 Recent trend: Gaining weight"
            WeightTrend.STABLE -> "⚖️ Recent trend: Weight stable"
        }
    }
    
    /**
     * Get motivation message based on current progress
     */
    fun getMotivationMessage(): String {
        return when {
            hasInsufficientData -> "🚀 Start your journey today!"
            currentStreak >= 7 -> "🔥 You're on fire! ${currentStreak} day streak!"
            totalWeightChange < -5f -> "🌟 Amazing transformation! Keep going!"
            averageWeightLoss > 0.5f -> "💪 Consistent progress! Great work!"
            else -> "🎯 Stay focused on your goals!"
        }
    }
}

/**
 * Extension function to format float to specified decimal places
 */
private fun Float.format(decimals: Int): String {
    return "%.${decimals}f".format(this)
}