package com.progresspal.app.presentation.insights.models

import com.progresspal.app.domain.insights.models.PlateauSeverity
import com.progresspal.app.domain.insights.models.ProgressPattern
import com.progresspal.app.domain.insights.models.WeightTrend

/**
 * Sealed class representing different types of insight cards
 * Each card type has specific data and UI requirements
 */
sealed class InsightCard {
    abstract val title: String
    abstract val cardType: CardType
    
    /**
     * Progress summary card showing overall trends and changes
     */
    data class ProgressSummary(
        override val title: String,
        val summary: String,
        val trend: WeightTrend,
        val totalChange: Float,
        val motivationMessage: String
    ) : InsightCard() {
        override val cardType = CardType.PROGRESS_SUMMARY
    }
    
    /**
     * Current streak card showing consecutive progress days
     */
    data class Streak(
        override val title: String,
        val streakDays: Int,
        val streakType: String, // "weight loss", "tracking", etc.
        val encouragement: String
    ) : InsightCard() {
        override val cardType = CardType.STREAK
    }
    
    /**
     * Plateau analysis card with severity and recommendations
     */
    data class PlateauAnalysis(
        override val title: String,
        val status: String,
        val severity: PlateauSeverity,
        val primaryAction: String,
        val encouragement: String
    ) : InsightCard() {
        override val cardType = CardType.PLATEAU_ANALYSIS
    }
    
    /**
     * Best progress achievement card
     */
    data class BestProgress(
        override val title: String,
        val progressAmount: Float,
        val description: String,
        val tip: String
    ) : InsightCard() {
        override val cardType = CardType.BEST_PROGRESS
    }
    
    /**
     * Detected patterns card
     */
    data class Patterns(
        override val title: String,
        val patterns: List<ProgressPattern>,
        val description: String
    ) : InsightCard() {
        override val cardType = CardType.PATTERNS
    }
    
    /**
     * Weight predictions card
     */
    data class Predictions(
        override val title: String,
        val predictions: Map<String, Float>,
        val disclaimer: String
    ) : InsightCard() {
        override val cardType = CardType.PREDICTIONS
    }
    
    /**
     * Personalized tips card
     */
    data class Tips(
        override val title: String,
        val tips: List<String>,
        val category: String
    ) : InsightCard() {
        override val cardType = CardType.TIPS
    }
    
    /**
     * Plateau breakthrough strategies card
     */
    data class PlateauStrategies(
        override val title: String,
        val strategies: List<String>,
        val timeframe: String
    ) : InsightCard() {
        override val cardType = CardType.PLATEAU_STRATEGIES
    }
    
    /**
     * Milestones and achievements card
     */
    data class Milestones(
        override val title: String,
        val milestones: List<String>,
        val celebration: String
    ) : InsightCard() {
        override val cardType = CardType.MILESTONES
    }
    
    /**
     * Trend analysis card with detailed statistics
     */
    data class TrendAnalysis(
        override val title: String,
        val overallTrend: WeightTrend,
        val recentTrend: WeightTrend,
        val averageWeightLoss: Float,
        val analysis: String
    ) : InsightCard() {
        override val cardType = CardType.TREND_ANALYSIS
    }
    
    /**
     * Weekly summary card
     */
    data class WeeklySummary(
        override val title: String,
        val weeklyChange: Float,
        val consistency: String,
        val highlights: List<String>
    ) : InsightCard() {
        override val cardType = CardType.WEEKLY_SUMMARY
    }
    
    /**
     * Goal progress card
     */
    data class GoalProgress(
        override val title: String,
        val currentWeight: Float,
        val targetWeight: Float,
        val progressPercentage: Float,
        val estimatedTimeToGoal: String
    ) : InsightCard() {
        override val cardType = CardType.GOAL_PROGRESS
    }
}

/**
 * Enum representing different insight card types for view type handling
 */
enum class CardType {
    PROGRESS_SUMMARY,
    STREAK,
    PLATEAU_ANALYSIS,
    BEST_PROGRESS,
    PATTERNS,
    PREDICTIONS,
    TIPS,
    PLATEAU_STRATEGIES,
    MILESTONES,
    TREND_ANALYSIS,
    WEEKLY_SUMMARY,
    GOAL_PROGRESS
}