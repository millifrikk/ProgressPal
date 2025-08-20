package com.progresspal.app.presentation.insights.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.progresspal.app.databinding.*
import com.progresspal.app.presentation.insights.models.CardType
import com.progresspal.app.presentation.insights.models.InsightCard
import com.progresspal.app.presentation.insights.viewholders.*

/**
 * RecyclerView adapter for displaying different types of insight cards
 * Uses multiple view types to handle different card layouts
 */
class InsightCardsAdapter(
    private val onCardClick: (InsightCard) -> Unit
) : ListAdapter<InsightCard, RecyclerView.ViewHolder>(InsightCardDiffCallback()) {
    
    override fun getItemViewType(position: Int): Int {
        return getItem(position).cardType.ordinal
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        
        return when (CardType.values()[viewType]) {
            CardType.PROGRESS_SUMMARY -> {
                val binding = ItemCardProgressSummaryBinding.inflate(layoutInflater, parent, false)
                ProgressSummaryViewHolder(binding, onCardClick)
            }
            CardType.STREAK -> {
                val binding = ItemCardProgressSummaryBinding.inflate(layoutInflater, parent, false)
                StreakViewHolder(binding, onCardClick)
            }
            CardType.PLATEAU_ANALYSIS -> {
                val binding = ItemCardPlateauAnalysisBinding.inflate(layoutInflater, parent, false)
                PlateauAnalysisViewHolder(binding, onCardClick)
            }
            CardType.BEST_PROGRESS -> {
                val binding = ItemCardProgressSummaryBinding.inflate(layoutInflater, parent, false)
                BestProgressViewHolder(binding, onCardClick)
            }
            CardType.PATTERNS -> {
                val binding = ItemCardTipsBinding.inflate(layoutInflater, parent, false)
                PatternsViewHolder(binding, onCardClick)
            }
            CardType.PREDICTIONS -> {
                val binding = ItemCardTipsBinding.inflate(layoutInflater, parent, false)
                PredictionsViewHolder(binding, onCardClick)
            }
            CardType.TIPS -> {
                val binding = ItemCardTipsBinding.inflate(layoutInflater, parent, false)
                TipsViewHolder(binding, onCardClick)
            }
            CardType.PLATEAU_STRATEGIES -> {
                val binding = ItemCardTipsBinding.inflate(layoutInflater, parent, false)
                PlateauStrategiesViewHolder(binding, onCardClick)
            }
            CardType.MILESTONES -> {
                val binding = ItemCardTipsBinding.inflate(layoutInflater, parent, false)
                MilestonesViewHolder(binding, onCardClick)
            }
            CardType.TREND_ANALYSIS -> {
                val binding = ItemCardProgressSummaryBinding.inflate(layoutInflater, parent, false)
                TrendAnalysisViewHolder(binding, onCardClick)
            }
            CardType.WEEKLY_SUMMARY -> {
                val binding = ItemCardProgressSummaryBinding.inflate(layoutInflater, parent, false)
                WeeklySummaryViewHolder(binding, onCardClick)
            }
            CardType.GOAL_PROGRESS -> {
                val binding = ItemCardProgressSummaryBinding.inflate(layoutInflater, parent, false)
                GoalProgressViewHolder(binding, onCardClick)
            }
        }
    }
    
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val card = getItem(position)
        
        when (holder) {
            is ProgressSummaryViewHolder -> holder.bind(card as InsightCard.ProgressSummary)
            is StreakViewHolder -> holder.bind(card as InsightCard.Streak)
            is PlateauAnalysisViewHolder -> holder.bind(card as InsightCard.PlateauAnalysis)
            is BestProgressViewHolder -> holder.bind(card as InsightCard.BestProgress)
            is PatternsViewHolder -> holder.bind(card as InsightCard.Patterns)
            is PredictionsViewHolder -> holder.bind(card as InsightCard.Predictions)
            is TipsViewHolder -> holder.bind(card as InsightCard.Tips)
            is PlateauStrategiesViewHolder -> holder.bind(card as InsightCard.PlateauStrategies)
            is MilestonesViewHolder -> holder.bind(card as InsightCard.Milestones)
            is TrendAnalysisViewHolder -> holder.bind(card as InsightCard.TrendAnalysis)
            is WeeklySummaryViewHolder -> holder.bind(card as InsightCard.WeeklySummary)
            is GoalProgressViewHolder -> holder.bind(card as InsightCard.GoalProgress)
        }
    }
    
    private class InsightCardDiffCallback : DiffUtil.ItemCallback<InsightCard>() {
        override fun areItemsTheSame(oldItem: InsightCard, newItem: InsightCard): Boolean {
            return oldItem.cardType == newItem.cardType && oldItem.title == newItem.title
        }
        
        override fun areContentsTheSame(oldItem: InsightCard, newItem: InsightCard): Boolean {
            return oldItem == newItem
        }
    }
}