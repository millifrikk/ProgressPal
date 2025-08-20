package com.progresspal.app.presentation.insights.viewholders

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.progresspal.app.databinding.ItemCardTipsBinding
import com.progresspal.app.presentation.insights.adapters.TipsAdapter
import com.progresspal.app.presentation.insights.models.InsightCard

/**
 * ViewHolder for displaying tips insight cards
 */
class TipsViewHolder(
    private val binding: ItemCardTipsBinding,
    private val onCardClick: (InsightCard) -> Unit
) : RecyclerView.ViewHolder(binding.root) {
    
    private val tipsAdapter = TipsAdapter()
    
    init {
        binding.recyclerViewTips.apply {
            layoutManager = LinearLayoutManager(binding.root.context)
            adapter = tipsAdapter
            setHasFixedSize(true)
        }
    }
    
    fun bind(card: InsightCard.Tips) {
        binding.apply {
            tvCardTitle.text = card.title
            tvCategory.text = card.category
            
            // Submit tips to the nested RecyclerView
            tipsAdapter.submitList(card.tips)
            
            // Set click listener
            root.setOnClickListener {
                onCardClick(card)
            }
        }
    }
}