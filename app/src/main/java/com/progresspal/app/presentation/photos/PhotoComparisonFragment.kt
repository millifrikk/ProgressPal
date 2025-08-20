package com.progresspal.app.presentation.photos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.progresspal.app.R
import java.io.File
import com.progresspal.app.data.database.ProgressPalDatabase
import com.progresspal.app.data.repository.PhotoRepository
import com.progresspal.app.data.repository.UserRepository
import com.progresspal.app.databinding.FragmentPhotoComparisonBinding
import kotlinx.coroutines.launch

/**
 * Fragment that displays before/after photo comparison
 * Shows the user's first photo and latest photo side by side
 */
class PhotoComparisonFragment : Fragment() {
    
    private var _binding: FragmentPhotoComparisonBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var photoRepository: PhotoRepository
    private lateinit var userRepository: UserRepository
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPhotoComparisonBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRepositories()
        loadPhotoComparison()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
    private fun setupRepositories() {
        val database = ProgressPalDatabase.getDatabase(requireContext())
        photoRepository = PhotoRepository(database.photoDao())
        userRepository = UserRepository(database.userDao())
    }
    
    private fun loadPhotoComparison() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val user = userRepository.getUserSync()
                if (user == null) {
                    showNoUserError()
                    return@launch
                }
                
                val firstPhoto = photoRepository.getFirstPhoto(user.id)
                val latestPhoto = photoRepository.getLatestPhoto(user.id)
                
                when {
                    firstPhoto == null && latestPhoto == null -> {
                        showNoPhotosState()
                    }
                    firstPhoto == null -> {
                        showSinglePhotoState(latestPhoto!!, isLatest = true)
                    }
                    latestPhoto == null || firstPhoto.id == latestPhoto.id -> {
                        showSinglePhotoState(firstPhoto, isLatest = false)
                    }
                    else -> {
                        showPhotoComparison(firstPhoto, latestPhoto)
                    }
                }
                
            } catch (e: Exception) {
                showErrorState("Failed to load photos: ${e.message}")
            }
        }
    }
    
    private fun showPhotoComparison(firstPhoto: com.progresspal.app.data.database.entities.PhotoEntity, latestPhoto: com.progresspal.app.data.database.entities.PhotoEntity) {
        binding.apply {
            // Show comparison layout
            layoutComparison.visibility = View.VISIBLE
            layoutNoPhotos.visibility = View.GONE
            layoutSinglePhoto.visibility = View.GONE
            layoutError.visibility = View.GONE
            
            // Load before photo
            val firstPhotoFile = File(firstPhoto.photoUri)
            if (firstPhotoFile.exists()) {
                Glide.with(this@PhotoComparisonFragment)
                    .load(firstPhotoFile)
                    .centerCrop()
                    .placeholder(R.drawable.ic_dashboard)
                    .error(R.drawable.ic_dashboard)
                    .into(ivBeforePhoto)
                
                tvBeforeDate.text = com.progresspal.app.utils.DateUtils.formatDisplayDate(firstPhoto.date)
            } else {
                showPhotoError(ivBeforePhoto, "Before photo not found")
            }
            
            // Load after photo
            val latestPhotoFile = File(latestPhoto.photoUri)
            if (latestPhotoFile.exists()) {
                Glide.with(this@PhotoComparisonFragment)
                    .load(latestPhotoFile)
                    .centerCrop()
                    .placeholder(R.drawable.ic_dashboard)
                    .error(R.drawable.ic_dashboard)
                    .into(ivAfterPhoto)
                
                tvAfterDate.text = com.progresspal.app.utils.DateUtils.formatDisplayDate(latestPhoto.date)
            } else {
                showPhotoError(ivAfterPhoto, "Latest photo not found")
            }
            
            // Calculate days between photos
            val daysBetween = calculateDaysBetween(firstPhoto.date, latestPhoto.date)
            tvProgressDuration.text = "Progress over $daysBetween days"
        }
    }
    
    private fun showSinglePhotoState(photo: com.progresspal.app.data.database.entities.PhotoEntity, isLatest: Boolean) {
        binding.apply {
            layoutSinglePhoto.visibility = View.VISIBLE
            layoutComparison.visibility = View.GONE
            layoutNoPhotos.visibility = View.GONE
            layoutError.visibility = View.GONE
            
            val photoFile = File(photo.photoUri)
            if (photoFile.exists()) {
                Glide.with(this@PhotoComparisonFragment)
                    .load(photoFile)
                    .centerCrop()
                    .placeholder(R.drawable.ic_dashboard)
                    .error(R.drawable.ic_dashboard)
                    .into(ivSinglePhoto)
                
                tvSinglePhotoDate.text = com.progresspal.app.utils.DateUtils.formatDisplayDate(photo.date)
                tvSinglePhotoLabel.text = if (isLatest) "Latest Progress Photo" else "First Progress Photo"
                tvSinglePhotoMessage.text = "Take more photos to see your before/after comparison!"
            } else {
                showErrorState("Photo file not found")
            }
        }
    }
    
    private fun showNoPhotosState() {
        binding.apply {
            layoutNoPhotos.visibility = View.VISIBLE
            layoutComparison.visibility = View.GONE
            layoutSinglePhoto.visibility = View.GONE
            layoutError.visibility = View.GONE
        }
    }
    
    private fun showErrorState(message: String) {
        binding.apply {
            layoutError.visibility = View.VISIBLE
            layoutComparison.visibility = View.GONE
            layoutSinglePhoto.visibility = View.GONE
            layoutNoPhotos.visibility = View.GONE
            
            tvErrorMessage.text = message
        }
    }
    
    private fun showNoUserError() {
        showErrorState("User not found. Please complete onboarding first.")
    }
    
    private fun showPhotoError(imageView: android.widget.ImageView, message: String) {
        imageView.setImageResource(R.drawable.ic_dashboard)
        // Could add an overlay or toast here to show the error
    }
    
    private fun calculateDaysBetween(startDate: java.util.Date, endDate: java.util.Date): Long {
        val diffInMillis = endDate.time - startDate.time
        return diffInMillis / (24 * 60 * 60 * 1000)
    }
    
    companion object {
        fun newInstance(): PhotoComparisonFragment {
            return PhotoComparisonFragment()
        }
    }
}