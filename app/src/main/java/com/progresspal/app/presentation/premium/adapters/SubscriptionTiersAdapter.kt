package com.progresspal.app.presentation.premium.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.progresspal.app.R
import com.progresspal.app.databinding.ItemSubscriptionTierBinding
import com.progresspal.app.domain.premium.SubscriptionTier

/**
 * Adapter for displaying subscription pricing tiers
 */
class SubscriptionTiersAdapter(
    private val onTierSelected: (SubscriptionTier) -> Unit
) : ListAdapter<SubscriptionTier, SubscriptionTiersAdapter.TierViewHolder>(TierDiffCallback()) {
    
    private var selectedTier: SubscriptionTier? = null
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TierViewHolder {
        val binding = ItemSubscriptionTierBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TierViewHolder(binding, onTierSelected)
    }
    
    override fun onBindViewHolder(holder: TierViewHolder, position: Int) {
        val tier = getItem(position)
        val isSelected = tier == selectedTier
        holder.bind(tier, isSelected)
    }
    
    fun setSelectedTier(tier: SubscriptionTier) {
        val oldSelected = selectedTier
        selectedTier = tier
        
        // Update UI for both old and new selections
        currentList.forEachIndexed { index, item ->
            if (item == oldSelected || item == tier) {
                notifyItemChanged(index)
            }
        }
    }
    
    class TierViewHolder(
        private val binding: ItemSubscriptionTierBinding,
        private val onTierSelected: (SubscriptionTier) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(tier: SubscriptionTier, isSelected: Boolean) {
            binding.apply {
                tvTierName.text = tier.type.getDisplayName()
                tvTierPrice.text = tier.price
                tvBillingPeriod.text = "per ${tier.billingPeriod}"
                
                // Show monthly equivalent for yearly/lifetime
                if (tier.type.name != "MONTHLY") {
                    tvMonthlyEquivalent.text = "${tier.getMonthlyEquivalent()}/month"
                    tvMonthlyEquivalent.visibility = android.view.View.VISIBLE
                } else {
                    tvMonthlyEquivalent.visibility = android.view.View.GONE
                }
                
                // Show savings badge
                if (tier.savings != null) {
                    tvSavingsBadge.text = tier.savings
                    tvSavingsBadge.visibility = android.view.View.VISIBLE
                } else {
                    tvSavingsBadge.visibility = android.view.View.GONE
                }
                
                // Show popular badge
                tvPopularBadge.visibility = if (tier.isPopular) {
                    android.view.View.VISIBLE
                } else {
                    android.view.View.GONE
                }
                
                // Update selection state
                updateSelectionState(isSelected)
                
                // Set click listener
                root.setOnClickListener {
                    onTierSelected(tier)
                }
            }
        }
        
        private fun updateSelectionState(isSelected: Boolean) {
            binding.apply {
                val context = root.context
                
                if (isSelected) {
                    cardContainer.strokeWidth = 4
                    cardContainer.strokeColor = context.getColor(R.color.pal_primary)
                    cardContainer.setCardBackgroundColor(context.getColor(R.color.pal_primary_light))
                    tvTierName.setTextColor(context.getColor(R.color.pal_primary))
                    tvTierPrice.setTextColor(context.getColor(R.color.pal_primary))
                } else {
                    cardContainer.strokeWidth = 1
                    cardContainer.strokeColor = context.getColor(R.color.pal_border)
                    cardContainer.setCardBackgroundColor(context.getColor(R.color.pal_surface))
                    tvTierName.setTextColor(context.getColor(R.color.pal_text_primary))
                    tvTierPrice.setTextColor(context.getColor(R.color.pal_text_primary))
                }
            }
        }
    }
    
    private class TierDiffCallback : DiffUtil.ItemCallback<SubscriptionTier>() {
        override fun areItemsTheSame(oldItem: SubscriptionTier, newItem: SubscriptionTier): Boolean {
            return oldItem.type == newItem.type
        }
        
        override fun areContentsTheSame(oldItem: SubscriptionTier, newItem: SubscriptionTier): Boolean {
            return oldItem == newItem
        }
    }
}