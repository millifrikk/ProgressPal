package com.progresspal.app.presentation.insights.viewholders

import androidx.recyclerview.widget.RecyclerView
import com.progresspal.app.databinding.ItemCardProgressSummaryBinding
import com.progresspal.app.presentation.insights.models.InsightCard
import kotlin.math.abs

/**
 * ViewHolder for displaying progress summary insight cards
 */
class ProgressSummaryViewHolder(
    private val binding: ItemCardProgressSummaryBinding,
    private val onCardClick: (InsightCard) -> Unit
) : RecyclerView.ViewHolder(binding.root) {
    
    fun bind(card: InsightCard.ProgressSummary) {
        binding.apply {
            tvCardTitle.text = card.title
            tvProgressSummary.text = card.summary
            tvMotivation.text = card.motivationMessage
            
            // Set trend emoji
            tvTrendEmoji.text = card.trend.getEmoji()
            
            // Format weight change
            val weightChange = card.totalChange
            val formattedChange = if (weightChange < 0) {
                "-${abs(weightChange).format(1)} kg"
            } else {
                "+${weightChange.format(1)} kg"
            }
            tvWeightChange.text = formattedChange
            
            // Set weight change color based on trend
            val colorRes = when {
                weightChange < -0.5f -> android.R.color.holo_green_dark
                weightChange > 0.5f -> android.R.color.holo_orange_dark
                else -> android.R.color.darker_gray
            }
            tvWeightChange.setTextColor(binding.root.context.getColor(colorRes))
            
            // Set click listener
            root.setOnClickListener {
                onCardClick(card)
            }
        }
    }
    
    private fun Float.format(decimals: Int): String {
        return "%.${decimals}f".format(this)
    }
}