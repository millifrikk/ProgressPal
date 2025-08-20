package com.progresspal.app.presentation.insights.viewholders

import androidx.recyclerview.widget.RecyclerView
import com.progresspal.app.R
import com.progresspal.app.databinding.ItemCardPlateauAnalysisBinding
import com.progresspal.app.domain.insights.models.PlateauSeverity
import com.progresspal.app.presentation.insights.models.InsightCard

/**
 * ViewHolder for displaying plateau analysis insight cards
 */
class PlateauAnalysisViewHolder(
    private val binding: ItemCardPlateauAnalysisBinding,
    private val onCardClick: (InsightCard) -> Unit
) : RecyclerView.ViewHolder(binding.root) {
    
    fun bind(card: InsightCard.PlateauAnalysis) {
        binding.apply {
            tvCardTitle.text = card.title
            tvPlateauStatus.text = card.status
            tvPrimaryAction.text = card.primaryAction
            tvEncouragement.text = card.encouragement
            
            // Set severity badge
            tvSeverityBadge.text = card.severity.getDescription()
            
            // Set badge color based on severity
            val badgeBackground = when (card.severity) {
                PlateauSeverity.NONE -> R.drawable.badge_progress
                PlateauSeverity.MILD -> R.drawable.badge_milestone  
                PlateauSeverity.MODERATE -> R.drawable.badge_milestone
                PlateauSeverity.SEVERE -> R.drawable.badge_milestone
            }
            tvSeverityBadge.setBackgroundResource(badgeBackground)
            
            // Set click listener
            root.setOnClickListener {
                onCardClick(card)
            }
        }
    }
}