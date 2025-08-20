package com.progresspal.app.presentation.photos

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.progresspal.app.R
import com.progresspal.app.data.database.ProgressPalDatabase
import com.progresspal.app.data.repository.PhotoRepository
import com.progresspal.app.databinding.ActivityPhotoDetailBinding
import com.progresspal.app.utils.DateUtils
import kotlinx.coroutines.launch
import java.io.File

/**
 * Activity that displays a single photo in full screen
 * Shows photo details and allows for actions like delete, share, etc.
 */
class PhotoDetailActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityPhotoDetailBinding
    private lateinit var photoRepository: PhotoRepository
    
    private var photoId: Long = -1L
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPhotoDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupRepositories()
        getPhotoIdFromIntent()
        setupToolbar()
        loadPhoto()
    }
    
    private fun setupRepositories() {
        val database = ProgressPalDatabase.getDatabase(this)
        photoRepository = PhotoRepository(database.photoDao())
    }
    
    private fun getPhotoIdFromIntent() {
        photoId = intent.getLongExtra(EXTRA_PHOTO_ID, -1L)
        if (photoId == -1L) {
            finish()
            return
        }
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "Photo Details"
        }
    }
    
    private fun loadPhoto() {
        lifecycleScope.launch {
            try {
                val photo = photoRepository.getPhotoById(photoId)
                if (photo == null) {
                    showErrorState("Photo not found")
                    return@launch
                }
                
                displayPhoto(photo)
                
            } catch (e: Exception) {
                showErrorState("Failed to load photo: ${e.message}")
            }
        }
    }
    
    private fun displayPhoto(photo: com.progresspal.app.data.database.entities.PhotoEntity) {
        binding.apply {
            // Load the full-size photo
            val photoFile = File(photo.photoUri)
            if (photoFile.exists()) {
                Glide.with(this@PhotoDetailActivity)
                    .load(photoFile)
                    .fitCenter()
                    .placeholder(R.drawable.ic_dashboard)
                    .error(R.drawable.ic_dashboard)
                    .into(ivPhoto)
                
                layoutPhotoDetails.visibility = View.VISIBLE
                layoutError.visibility = View.GONE
                
                // Set photo details
                tvPhotoDate.text = DateUtils.formatDisplayDate(photo.date)
                tvPhotoTime.text = DateUtils.formatDisplayTime(photo.createdAt)
                
                // Set photo type
                when (photo.photoType) {
                    "progress" -> {
                        tvPhotoType.text = "Progress Photo"
                        tvPhotoType.setBackgroundResource(R.drawable.badge_progress)
                    }
                    "milestone" -> {
                        tvPhotoType.text = "Milestone Photo"
                        tvPhotoType.setBackgroundResource(R.drawable.badge_milestone)
                    }
                    else -> {
                        tvPhotoType.visibility = View.GONE
                    }
                }
                
                // Show file info
                val fileSizeKB = photoFile.length() / 1024
                tvFileInfo.text = "File size: ${fileSizeKB} KB"
                
            } else {
                showErrorState("Photo file not found")
            }
        }
    }
    
    private fun showErrorState(message: String) {
        binding.apply {
            layoutPhotoDetails.visibility = View.GONE
            layoutError.visibility = View.VISIBLE
            tvErrorMessage.text = message
        }
    }
    
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
    
    companion object {
        private const val EXTRA_PHOTO_ID = "extra_photo_id"
        
        fun launch(context: Context, photoId: Long) {
            val intent = Intent(context, PhotoDetailActivity::class.java).apply {
                putExtra(EXTRA_PHOTO_ID, photoId)
            }
            context.startActivity(intent)
        }
    }
}