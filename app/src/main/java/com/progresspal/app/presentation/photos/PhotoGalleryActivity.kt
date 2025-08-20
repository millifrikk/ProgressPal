package com.progresspal.app.presentation.photos

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.progresspal.app.data.database.ProgressPalDatabase
import com.progresspal.app.data.repository.PhotoRepository
import com.progresspal.app.data.repository.UserRepository
import com.progresspal.app.databinding.ActivityPhotoGalleryBinding
import com.progresspal.app.presentation.photos.adapters.PhotoGalleryAdapter
import kotlinx.coroutines.launch

/**
 * Activity that displays all progress photos in a grid layout
 * Provides gallery view with photo metadata and navigation to detail screens
 */
class PhotoGalleryActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityPhotoGalleryBinding
    private lateinit var userRepository: UserRepository
    private lateinit var photoRepository: PhotoRepository
    private lateinit var photoAdapter: PhotoGalleryAdapter
    
    private var currentUserId: Long = -1
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPhotoGalleryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        setupRepositories()
        setupRecyclerView()
        loadPhotos()
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = "Progress Photos"
        }
    }
    
    private fun setupRepositories() {
        val database = ProgressPalDatabase.getDatabase(this)
        photoRepository = PhotoRepository(database.photoDao())
        userRepository = UserRepository(database.userDao())
    }
    
    private fun setupRecyclerView() {
        photoAdapter = PhotoGalleryAdapter { photo ->
            // Handle photo click - open full screen view
            PhotoDetailActivity.launch(this, photo.id)
        }
        
        binding.recyclerViewPhotos.apply {
            layoutManager = GridLayoutManager(this@PhotoGalleryActivity, 2)
            adapter = photoAdapter
            setHasFixedSize(true)
        }
    }
    
    private fun loadPhotos() {
        lifecycleScope.launch {
            try {
                val user = userRepository.getUserSync()
                if (user == null) {
                    showErrorState("User not found. Please complete onboarding first.")
                    return@launch
                }
                
                currentUserId = user.id
                
                // Observe photos from database
                photoRepository.getAllPhotos(user.id).observe(this@PhotoGalleryActivity) { photos ->
                    if (photos.isNullOrEmpty()) {
                        showEmptyState()
                    } else {
                        showPhotosState()
                        photoAdapter.submitList(photos)
                        binding.tvPhotoCount.text = "${photos.size} photos"
                    }
                }
                
            } catch (e: Exception) {
                showErrorState("Failed to load photos: ${e.message}")
            }
        }
    }
    
    private fun showEmptyState() {
        binding.apply {
            recyclerViewPhotos.visibility = View.GONE
            layoutEmpty.visibility = View.VISIBLE
            layoutError.visibility = View.GONE
            tvPhotoCount.visibility = View.GONE
        }
    }
    
    private fun showPhotosState() {
        binding.apply {
            recyclerViewPhotos.visibility = View.VISIBLE
            layoutEmpty.visibility = View.GONE
            layoutError.visibility = View.GONE
            tvPhotoCount.visibility = View.VISIBLE
        }
    }
    
    private fun showErrorState(message: String) {
        binding.apply {
            recyclerViewPhotos.visibility = View.GONE
            layoutEmpty.visibility = View.GONE
            layoutError.visibility = View.VISIBLE
            tvErrorMessage.text = message
            tvPhotoCount.visibility = View.GONE
        }
    }
    
    companion object {
        fun createIntent(context: Context): Intent {
            return Intent(context, PhotoGalleryActivity::class.java)
        }
    }
}