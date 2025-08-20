package com.progresspal.app.presentation.insights.viewholders

import androidx.recyclerview.widget.RecyclerView
import com.progresspal.app.databinding.*
import com.progresspal.app.presentation.insights.models.InsightCard

/**
 * Simple view holders for insight cards that don't need complex layouts yet
 * These can be expanded later with full implementations
 */

class StreakViewHolder(
    private val binding: ItemCardProgressSummaryBinding, // Reusing layout for now
    private val onCardClick: (InsightCard) -> Unit
) : RecyclerView.ViewHolder(binding.root) {
    
    fun bind(card: InsightCard.Streak) {
        binding.apply {
            tvCardTitle.text = card.title
            tvProgressSummary.text = "${card.streakDays} day ${card.streakType} streak!"
            tvMotivation.text = card.encouragement
            tvTrendEmoji.text = "🔥"
            tvWeightChange.text = "${card.streakDays} days"
            
            root.setOnClickListener { onCardClick(card) }
        }
    }
}

class BestProgressViewHolder(
    private val binding: ItemCardProgressSummaryBinding, // Reusing layout for now
    private val onCardClick: (InsightCard) -> Unit
) : RecyclerView.ViewHolder(binding.root) {
    
    fun bind(card: InsightCard.BestProgress) {
        binding.apply {
            tvCardTitle.text = card.title
            tvProgressSummary.text = card.description
            tvMotivation.text = card.tip
            tvTrendEmoji.text = "🏆"
            tvWeightChange.text = "-${card.progressAmount.format(1)} kg"
            
            root.setOnClickListener { onCardClick(card) }
        }
    }
    
    private fun Float.format(decimals: Int): String {
        return "%.${decimals}f".format(this)
    }
}

class PatternsViewHolder(
    private val binding: ItemCardTipsBinding, // Reusing tips layout
    private val onCardClick: (InsightCard) -> Unit
) : RecyclerView.ViewHolder(binding.root) {
    
    fun bind(card: InsightCard.Patterns) {
        binding.apply {
            tvCardTitle.text = card.title
            tvCategory.text = "Patterns"
            
            // Convert patterns to string list for display
            val patternDescriptions = card.patterns.map { pattern ->
                when (pattern) {
                    is com.progresspal.app.domain.insights.models.ProgressPattern.Plateau -> pattern.getDescription()
                    is com.progresspal.app.domain.insights.models.ProgressPattern.RapidChange -> pattern.getDescription()
                    is com.progresspal.app.domain.insights.models.ProgressPattern.WeeklyCycle -> pattern.getDescription()
                    is com.progresspal.app.domain.insights.models.ProgressPattern.ConsistentProgress -> pattern.getDescription()
                    is com.progresspal.app.domain.insights.models.ProgressPattern.IrregularPattern -> pattern.getDescription()
                }
            }
            
            // Set up adapter (simplified for now)
            // In a full implementation, this would use a custom adapter
            
            root.setOnClickListener { onCardClick(card) }
        }
    }
}

class PredictionsViewHolder(
    private val binding: ItemCardTipsBinding, // Reusing tips layout
    private val onCardClick: (InsightCard) -> Unit
) : RecyclerView.ViewHolder(binding.root) {
    
    fun bind(card: InsightCard.Predictions) {
        binding.apply {
            tvCardTitle.text = card.title
            tvCategory.text = "Predictions"
            
            root.setOnClickListener { onCardClick(card) }
        }
    }
}

class PlateauStrategiesViewHolder(
    private val binding: ItemCardTipsBinding, // Reusing tips layout
    private val onCardClick: (InsightCard) -> Unit
) : RecyclerView.ViewHolder(binding.root) {
    
    fun bind(card: InsightCard.PlateauStrategies) {
        binding.apply {
            tvCardTitle.text = card.title
            tvCategory.text = card.timeframe
            
            root.setOnClickListener { onCardClick(card) }
        }
    }
}

class MilestonesViewHolder(
    private val binding: ItemCardTipsBinding, // Reusing tips layout
    private val onCardClick: (InsightCard) -> Unit
) : RecyclerView.ViewHolder(binding.root) {
    
    fun bind(card: InsightCard.Milestones) {
        binding.apply {
            tvCardTitle.text = card.title
            tvCategory.text = card.celebration
            
            root.setOnClickListener { onCardClick(card) }
        }
    }
}

class TrendAnalysisViewHolder(
    private val binding: ItemCardProgressSummaryBinding, // Reusing layout
    private val onCardClick: (InsightCard) -> Unit
) : RecyclerView.ViewHolder(binding.root) {
    
    fun bind(card: InsightCard.TrendAnalysis) {
        binding.apply {
            tvCardTitle.text = card.title
            tvProgressSummary.text = card.analysis
            tvMotivation.text = "Overall: ${card.overallTrend.getDescription()}"
            tvTrendEmoji.text = card.recentTrend.getEmoji()
            tvWeightChange.text = "${card.averageWeightLoss.format(1)} kg/week"
            
            root.setOnClickListener { onCardClick(card) }
        }
    }
    
    private fun Float.format(decimals: Int): String {
        return "%.${decimals}f".format(this)
    }
}

class WeeklySummaryViewHolder(
    private val binding: ItemCardProgressSummaryBinding, // Reusing layout
    private val onCardClick: (InsightCard) -> Unit
) : RecyclerView.ViewHolder(binding.root) {
    
    fun bind(card: InsightCard.WeeklySummary) {
        binding.apply {
            tvCardTitle.text = card.title
            tvProgressSummary.text = card.consistency
            tvMotivation.text = card.highlights.firstOrNull() ?: "Keep up the good work!"
            tvTrendEmoji.text = "📊"
            tvWeightChange.text = "${card.weeklyChange.format(1)} kg"
            
            root.setOnClickListener { onCardClick(card) }
        }
    }
    
    private fun Float.format(decimals: Int): String {
        return "%.${decimals}f".format(this)
    }
}

class GoalProgressViewHolder(
    private val binding: ItemCardProgressSummaryBinding, // Reusing layout
    private val onCardClick: (InsightCard) -> Unit
) : RecyclerView.ViewHolder(binding.root) {
    
    fun bind(card: InsightCard.GoalProgress) {
        binding.apply {
            tvCardTitle.text = card.title
            tvProgressSummary.text = "Current: ${card.currentWeight.format(1)}kg → Target: ${card.targetWeight.format(1)}kg"
            tvMotivation.text = "ETA: ${card.estimatedTimeToGoal}"
            tvTrendEmoji.text = "🎯"
            tvWeightChange.text = "${card.progressPercentage.format(0)}%"
            
            root.setOnClickListener { onCardClick(card) }
        }
    }
    
    private fun Float.format(decimals: Int): String {
        return "%.${decimals}f".format(this)
    }
}