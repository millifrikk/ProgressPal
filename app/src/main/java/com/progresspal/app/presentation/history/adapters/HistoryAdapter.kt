package com.progresspal.app.presentation.history.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.progresspal.app.databinding.ItemWeightHistoryBinding
import com.progresspal.app.domain.models.Weight
import com.progresspal.app.utils.DateUtils

class HistoryAdapter(
    private val onItemClick: (Weight) -> Unit,
    private val onDeleteClick: (Weight) -> Unit
) : ListAdapter<Weight, HistoryAdapter.WeightViewHolder>(WeightDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeightViewHolder {
        val binding = ItemWeightHistoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return WeightViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: WeightViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    inner class WeightViewHolder(
        private val binding: ItemWeightHistoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(weight: Weight) {
            binding.apply {
                tvWeight.text = "${weight.weight} kg"
                tvDate.text = DateUtils.formatDisplayDate(weight.date)
                tvTime.text = weight.time ?: ""
                
                // Show notes if available
                if (!weight.notes.isNullOrBlank()) {
                    tvNotes.text = weight.notes
                    tvNotes.visibility = android.view.View.VISIBLE
                } else {
                    tvNotes.visibility = android.view.View.GONE
                }
                
                // Show weight change if not first item
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION && position < itemCount - 1) {
                    val previousWeight = getItem(position + 1).weight
                    val change = weight.weight - previousWeight
                    val changeText = when {
                        change > 0 -> "+${String.format("%.1f", change)} kg"
                        change < 0 -> "${String.format("%.1f", change)} kg"
                        else -> "No change"
                    }
                    tvWeightChange.text = changeText
                    tvWeightChange.visibility = android.view.View.VISIBLE
                    
                    // Color coding for weight change
                    val colorRes = when {
                        change > 0 -> android.R.color.holo_red_light
                        change < 0 -> android.R.color.holo_green_light
                        else -> android.R.color.darker_gray
                    }
                    tvWeightChange.setTextColor(itemView.context.getColor(colorRes))
                } else {
                    tvWeightChange.visibility = android.view.View.GONE
                }
                
                // Click handlers
                root.setOnClickListener { onItemClick(weight) }
                btnDelete.setOnClickListener { onDeleteClick(weight) }
            }
        }
    }
    
    private class WeightDiffCallback : DiffUtil.ItemCallback<Weight>() {
        override fun areItemsTheSame(oldItem: Weight, newItem: Weight): Boolean {
            return oldItem.id == newItem.id
        }
        
        override fun areContentsTheSame(oldItem: Weight, newItem: Weight): Boolean {
            return oldItem == newItem
        }
    }
}