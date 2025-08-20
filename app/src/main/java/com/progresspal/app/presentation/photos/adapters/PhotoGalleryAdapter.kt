package com.progresspal.app.presentation.photos.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.progresspal.app.R
import com.progresspal.app.data.database.entities.PhotoEntity
import com.progresspal.app.databinding.ItemPhotoGalleryBinding
import com.progresspal.app.utils.DateUtils
import java.io.File

/**
 * RecyclerView adapter for displaying photos in a grid layout
 * Uses ListAdapter for efficient updates with DiffUtil
 */
class PhotoGalleryAdapter(
    private val onPhotoClick: (PhotoEntity) -> Unit
) : ListAdapter<PhotoEntity, PhotoGalleryAdapter.PhotoViewHolder>(PhotoDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val binding = ItemPhotoGalleryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PhotoViewHolder(binding, onPhotoClick)
    }
    
    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    class PhotoViewHolder(
        private val binding: ItemPhotoGalleryBinding,
        private val onPhotoClick: (PhotoEntity) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(photo: PhotoEntity) {
            binding.apply {
                // Load photo using Glide
                val photoFile = File(photo.photoUri)
                if (photoFile.exists()) {
                    Glide.with(itemView.context)
                        .load(photoFile)
                        .centerCrop()
                        .placeholder(R.drawable.ic_dashboard)
                        .error(R.drawable.ic_dashboard)
                        .into(ivPhoto)
                } else {
                    ivPhoto.setImageResource(R.drawable.ic_dashboard)
                }
                
                // Set date
                tvDate.text = DateUtils.formatDisplayDate(photo.date)
                
                // Set photo type badge (if needed)
                when (photo.photoType) {
                    "progress" -> {
                        tvPhotoType.text = "Progress"
                        tvPhotoType.setBackgroundResource(R.drawable.badge_progress)
                    }
                    "milestone" -> {
                        tvPhotoType.text = "Milestone"
                        tvPhotoType.setBackgroundResource(R.drawable.badge_milestone)
                    }
                    else -> {
                        tvPhotoType.text = ""
                    }
                }
                
                // Set click listener
                root.setOnClickListener {
                    onPhotoClick(photo)
                }
                
                // Add animation on bind
                root.alpha = 0f
                root.animate()
                    .alpha(1f)
                    .setDuration(200)
                    .setStartDelay((adapterPosition * 50).toLong())
                    .start()
            }
        }
    }
    
    private class PhotoDiffCallback : DiffUtil.ItemCallback<PhotoEntity>() {
        override fun areItemsTheSame(oldItem: PhotoEntity, newItem: PhotoEntity): Boolean {
            return oldItem.id == newItem.id
        }
        
        override fun areContentsTheSame(oldItem: PhotoEntity, newItem: PhotoEntity): Boolean {
            return oldItem == newItem
        }
    }
}