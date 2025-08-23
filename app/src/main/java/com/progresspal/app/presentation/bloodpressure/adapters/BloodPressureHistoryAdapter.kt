package com.progresspal.app.presentation.bloodpressure.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.progresspal.app.R
import com.progresspal.app.data.database.entities.BloodPressureCategory
import com.progresspal.app.data.database.entities.BloodPressureEntity
import com.progresspal.app.databinding.ItemBloodPressureReadingBinding
import com.google.android.material.chip.Chip
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.*

/**
 * Adapter for displaying blood pressure readings in a RecyclerView
 * Handles the display of blood pressure data with category colors and formatting
 */
class BloodPressureHistoryAdapter(
    private val onItemClick: (BloodPressureEntity) -> Unit,
    private val onOptionsClick: (BloodPressureEntity) -> Unit
) : ListAdapter<BloodPressureEntity, BloodPressureHistoryAdapter.BloodPressureViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BloodPressureViewHolder {
        val binding = ItemBloodPressureReadingBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return BloodPressureViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BloodPressureViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class BloodPressureViewHolder(
        private val binding: ItemBloodPressureReadingBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(getItem(position))
                }
            }
            
            binding.btnOptions.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onOptionsClick(getItem(position))
                }
            }
        }

        fun bind(reading: BloodPressureEntity) {
            with(binding) {
                // Blood pressure reading
                tvBloodPressure.text = "${reading.systolic}/${reading.diastolic}"
                
                // Pulse
                tvPulse.text = reading.pulse.toString()
                
                // Category with color coding
                val category = reading.getCategory()
                chipCategory.text = category.displayName
                setCategoryChipColor(chipCategory, category)
                
                // Date and time
                formatDateTime(reading.timestamp)
                
                // Time of day
                tvTimeOfDay.text = when (reading.timeOfDay) {
                    BloodPressureEntity.TIME_MORNING -> "Morning"
                    BloodPressureEntity.TIME_AFTERNOON -> "Afternoon"
                    BloodPressureEntity.TIME_EVENING -> "Evening"
                    else -> "Unknown"
                }
                
                // Tags
                setupTags(reading.tags)
                
                // Notes
                setupNotes(reading.notes)
            }
        }
        
        private fun setCategoryChipColor(chip: Chip, category: BloodPressureCategory) {
            val context = chip.context
            val colorRes = when (category) {
                BloodPressureCategory.OPTIMAL -> R.color.green_500
                BloodPressureCategory.NORMAL -> R.color.green_400
                BloodPressureCategory.ELEVATED -> R.color.yellow_500
                BloodPressureCategory.STAGE_1 -> R.color.orange_500
                BloodPressureCategory.STAGE_2 -> R.color.deep_orange_500
                BloodPressureCategory.CRISIS -> R.color.red_500
            }
            
            val color = ContextCompat.getColor(context, colorRes)
            chip.chipBackgroundColor = android.content.res.ColorStateList.valueOf(color)
            chip.setTextColor(ContextCompat.getColor(context, android.R.color.white))
        }
        
        private fun formatDateTime(timestamp: Long) {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = timestamp
            
            val now = Calendar.getInstance()
            val dateFormat: SimpleDateFormat
            
            // Check if it's today
            if (isSameDay(calendar, now)) {
                dateFormat = SimpleDateFormat("'Today at' h:mm a", Locale.getDefault())
            } 
            // Check if it's yesterday
            else if (isYesterday(calendar, now)) {
                dateFormat = SimpleDateFormat("'Yesterday at' h:mm a", Locale.getDefault())
            }
            // Check if it's this week
            else if (isThisWeek(calendar, now)) {
                dateFormat = SimpleDateFormat("EEEE 'at' h:mm a", Locale.getDefault())
            }
            // Older dates
            else {
                dateFormat = SimpleDateFormat("MMM d 'at' h:mm a", Locale.getDefault())
            }
            
            binding.tvDateTime.text = dateFormat.format(calendar.time)
        }
        
        private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
            return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                   cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
        }
        
        private fun isYesterday(calendar: Calendar, now: Calendar): Boolean {
            val yesterday = Calendar.getInstance()
            yesterday.timeInMillis = now.timeInMillis
            yesterday.add(Calendar.DAY_OF_YEAR, -1)
            return isSameDay(calendar, yesterday)
        }
        
        private fun isThisWeek(calendar: Calendar, now: Calendar): Boolean {
            val weekStart = Calendar.getInstance()
            weekStart.timeInMillis = now.timeInMillis
            weekStart.set(Calendar.DAY_OF_WEEK, weekStart.firstDayOfWeek)
            
            return calendar.timeInMillis >= weekStart.timeInMillis &&
                   calendar.timeInMillis <= now.timeInMillis
        }
        
        private fun setupTags(tagsJson: String?) {
            if (tagsJson.isNullOrEmpty()) {
                binding.chipGroupTags.visibility = View.GONE
                return
            }
            
            try {
                val tagsArray = JSONArray(tagsJson)
                if (tagsArray.length() == 0) {
                    binding.chipGroupTags.visibility = View.GONE
                    return
                }
                
                binding.chipGroupTags.removeAllViews()
                binding.chipGroupTags.visibility = View.VISIBLE
                
                for (i in 0 until tagsArray.length()) {
                    val tag = tagsArray.getString(i)
                    val chip = Chip(binding.root.context)
                    chip.text = getTagDisplayName(tag)
                    chip.isClickable = false
                    chip.isCheckable = false
                    binding.chipGroupTags.addView(chip)
                }
                
            } catch (e: Exception) {
                binding.chipGroupTags.visibility = View.GONE
            }
        }
        
        private fun getTagDisplayName(tag: String): String {
            return when (tag) {
                BloodPressureEntity.TAG_BEFORE_MEAL -> "Before meal"
                BloodPressureEntity.TAG_AFTER_MEAL -> "After meal"
                BloodPressureEntity.TAG_AFTER_EXERCISE -> "After exercise"
                BloodPressureEntity.TAG_STRESSED -> "Stressed"
                BloodPressureEntity.TAG_RELAXED -> "Relaxed"
                BloodPressureEntity.TAG_MEDICATION -> "Medication"
                else -> tag.capitalize()
            }
        }
        
        private fun setupNotes(notes: String?) {
            if (notes.isNullOrEmpty()) {
                binding.tvNotes.visibility = View.GONE
            } else {
                binding.tvNotes.visibility = View.VISIBLE
                binding.tvNotes.text = notes
            }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<BloodPressureEntity>() {
            override fun areItemsTheSame(
                oldItem: BloodPressureEntity,
                newItem: BloodPressureEntity
            ): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(
                oldItem: BloodPressureEntity,
                newItem: BloodPressureEntity
            ): Boolean {
                return oldItem == newItem
            }
        }
    }
}