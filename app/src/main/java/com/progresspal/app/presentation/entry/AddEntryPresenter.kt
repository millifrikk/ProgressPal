package com.progresspal.app.presentation.entry

import com.progresspal.app.data.database.entities.PhotoEntity
import com.progresspal.app.data.database.entities.WeightEntity
import com.progresspal.app.data.repository.PhotoRepository
import com.progresspal.app.data.repository.UserRepository
import com.progresspal.app.data.repository.WeightRepository
import com.progresspal.app.domain.contracts.AddEntryContract
import com.progresspal.app.utils.DateUtils
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*

class AddEntryPresenter(
    private val userRepository: UserRepository,
    private val weightRepository: WeightRepository,
    private val photoRepository: PhotoRepository
) : AddEntryContract.Presenter {
    
    private var view: AddEntryContract.View? = null
    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.Main + job)
    
    override fun attachView(view: AddEntryContract.View) {
        this.view = view
        view.setCurrentDate()
    }
    
    override fun detachView() {
        this.view = null
        job.cancelChildren()
    }
    
    override fun isViewAttached(): Boolean = view != null
    
    override fun saveEntry(weight: String, date: Date, photoPath: String?, notes: String?) {
        if (!isViewAttached()) return
        
        view?.clearValidationErrors()
        
        // Validate weight
        val weightValue = validateWeight(weight)
        if (weightValue == null) {
            view?.showWeightValidationError("Please enter a valid weight")
            return
        }
        
        // Validate date (cannot be in future)
        if (date.after(Date())) {
            view?.showDateValidationError("Cannot add entry for future date")
            return
        }
        
        view?.showLoading()
        
        scope.launch {
            try {
                // Get current user
                val user = userRepository.getUserSync()
                if (user == null) {
                    view?.hideLoading()
                    view?.showError("User not found. Please complete onboarding first.")
                    return@launch
                }
                
                // Format time
                val timeString = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
                
                // Create weight entity
                val weightEntity = WeightEntity(
                    userId = user.id,
                    weight = weightValue,
                    date = date,
                    time = timeString,
                    notes = notes?.takeIf { it.isNotBlank() },
                    createdAt = Date()
                )
                
                // Save to database
                val weightId = weightRepository.insertWeight(weightEntity)
                
                // Save photo if provided
                photoPath?.let { path ->
                    val photoEntity = PhotoEntity(
                        userId = user.id,
                        weightId = weightId,
                        photoUri = path,
                        photoType = "progress",
                        date = date,
                        createdAt = Date()
                    )
                    photoRepository.insertPhoto(photoEntity)
                }
                
                // Update user's current weight
                userRepository.updateCurrentWeight(user.id, weightValue)
                
                view?.hideLoading()
                view?.showEntrySavedSuccess("Entry saved successfully!")
                
                // Navigate back after a short delay
                delay(500)
                view?.navigateBack()
                
            } catch (e: Exception) {
                view?.hideLoading()
                view?.showError("Failed to save entry: ${e.message}")
            }
        }
    }
    
    override fun onDateClicked(currentDate: Date) {
        view?.showDatePicker(currentDate)
    }
    
    private fun validateWeight(weightStr: String): Float? {
        return try {
            val weight = weightStr.trim().toFloat()
            if (weight > 0 && weight <= 1000) { // Reasonable weight limits
                weight
            } else {
                null
            }
        } catch (e: NumberFormatException) {
            null
        }
    }
}