package com.progresspal.app.presentation.premium.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.progresspal.app.databinding.ItemPremiumBenefitBinding
import com.progresspal.app.domain.premium.PremiumBenefit

/**
 * Adapter for displaying premium benefits in upgrade screen
 */
class BenefitsAdapter : ListAdapter<PremiumBenefit, BenefitsAdapter.BenefitViewHolder>(BenefitDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BenefitViewHolder {
        val binding = ItemPremiumBenefitBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return BenefitViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: BenefitViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    class BenefitViewHolder(
        private val binding: ItemPremiumBenefitBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(benefit: PremiumBenefit) {
            binding.apply {
                tvBenefitIcon.text = benefit.icon
                tvBenefitTitle.text = benefit.title
                tvBenefitDescription.text = benefit.description
                
                // Add animation
                root.alpha = 0f
                root.animate()
                    .alpha(1f)
                    .setDuration(300)
                    .setStartDelay((adapterPosition * 100).toLong())
                    .start()
            }
        }
    }
    
    private class BenefitDiffCallback : DiffUtil.ItemCallback<PremiumBenefit>() {
        override fun areItemsTheSame(oldItem: PremiumBenefit, newItem: PremiumBenefit): Boolean {
            return oldItem.title == newItem.title
        }
        
        override fun areContentsTheSame(oldItem: PremiumBenefit, newItem: PremiumBenefit): Boolean {
            return oldItem == newItem
        }
    }
}