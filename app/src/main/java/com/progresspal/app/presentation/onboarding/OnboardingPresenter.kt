package com.progresspal.app.presentation.onboarding

import com.progresspal.app.data.database.entities.UserEntity
import com.progresspal.app.data.repository.UserRepository
import com.progresspal.app.domain.contracts.OnboardingContract
import com.progresspal.app.utils.Constants
import kotlinx.coroutines.*
import java.util.*

class OnboardingPresenter(
    private val userRepository: UserRepository
) : OnboardingContract.Presenter {
    
    private var view: OnboardingContract.View? = null
    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.Main + job)
    
    override fun attachView(view: OnboardingContract.View) {
        this.view = view
    }
    
    override fun detachView() {
        this.view = null
        job.cancelChildren()
    }
    
    override fun isViewAttached(): Boolean = view != null
    
    override fun saveUserInfo(
        name: String?,
        age: String?,
        height: String,
        gender: String?,
        activityLevel: String?,
        currentWeight: String,
        targetWeight: String?,
        targetDate: String?,
        motivation: String?,
        trackMeasurements: Boolean,
        waist: String?,
        chest: String?,
        hips: String?
    ) {
        if (!isViewAttached()) return
        
        view?.clearValidationErrors()
        
        // Validate required fields
        val heightValue = validateHeight(height)
        if (heightValue == null) {
            view?.showValidationError("height", "Please enter a valid height between ${Constants.MIN_HEIGHT_CM}-${Constants.MAX_HEIGHT_CM} cm")
            return
        }
        
        val weightValue = validateWeight(currentWeight)
        if (weightValue == null) {
            view?.showValidationError("weight", "Please enter a valid weight between ${Constants.MIN_WEIGHT_KG}-${Constants.MAX_WEIGHT_KG} kg")
            return
        }
        
        // Validate optional fields
        val ageValue = age?.takeIf { it.isNotBlank() }?.let { validateAge(it) }
        if (!age.isNullOrBlank() && ageValue == null) {
            view?.showValidationError("age", "Please enter a valid age between 10-120")
            return
        }
        
        val targetWeightValue = targetWeight?.takeIf { it.isNotBlank() }?.let { validateWeight(it) }
        if (!targetWeight.isNullOrBlank() && targetWeightValue == null) {
            view?.showValidationError("targetWeight", "Please enter a valid target weight")
            return
        }
        
        // Validate measurements if tracking is enabled
        var waistValue: Float? = null
        var chestValue: Float? = null
        var hipsValue: Float? = null
        
        if (trackMeasurements) {
            waistValue = waist?.takeIf { it.isNotBlank() }?.let { validateMeasurement(it) }
            chestValue = chest?.takeIf { it.isNotBlank() }?.let { validateMeasurement(it) }
            hipsValue = hips?.takeIf { it.isNotBlank() }?.let { validateMeasurement(it) }
            
            if (!waist.isNullOrBlank() && waistValue == null) {
                view?.showValidationError("waist", "Please enter a valid waist measurement")
                return
            }
            if (!chest.isNullOrBlank() && chestValue == null) {
                view?.showValidationError("chest", "Please enter a valid chest measurement")
                return
            }
            if (!hips.isNullOrBlank() && hipsValue == null) {
                view?.showValidationError("hips", "Please enter a valid hips measurement")
                return
            }
        }
        
        view?.showLoading()
        
        scope.launch {
            try {
                // Check if user already exists
                val existingUser = userRepository.getUserSync()
                
                val userEntity = UserEntity(
                    id = existingUser?.id ?: 0,
                    name = name?.takeIf { it.isNotBlank() },
                    age = ageValue,
                    height = heightValue,
                    gender = gender?.takeIf { it.isNotBlank() },
                    activityLevel = activityLevel?.takeIf { it.isNotBlank() },
                    initialWeight = weightValue,
                    currentWeight = weightValue,
                    targetWeight = targetWeightValue,
                    initialWaist = waistValue,
                    initialChest = chestValue,
                    initialHips = hipsValue,
                    trackMeasurements = trackMeasurements,
                    createdAt = existingUser?.createdAt ?: Date(),
                    updatedAt = Date()
                )
                
                if (existingUser == null) {
                    userRepository.insertUser(userEntity)
                } else {
                    userRepository.updateUser(userEntity)
                }
                
                view?.hideLoading()
                view?.showUserSavedSuccess()
                view?.navigateToMainActivity()
                
            } catch (e: Exception) {
                view?.hideLoading()
                view?.showError("Failed to save user information: ${e.message}")
            }
        }
    }
    
    override fun onNextClicked(currentPage: Int) {
        view?.goToNextPage()
    }
    
    override fun onBackClicked(currentPage: Int) {
        view?.goToPreviousPage()
    }
    
    override fun onSkipClicked() {
        view?.navigateToMainActivity()
    }
    
    override fun onFinishClicked() {
        // This would typically be called from the last page with collected data
        view?.navigateToMainActivity()
    }
    
    private fun validateHeight(heightStr: String): Float? {
        return try {
            val height = heightStr.trim().toFloat()
            if (height >= Constants.MIN_HEIGHT_CM && height <= Constants.MAX_HEIGHT_CM) {
                height
            } else {
                null
            }
        } catch (e: NumberFormatException) {
            null
        }
    }
    
    private fun validateWeight(weightStr: String): Float? {
        return try {
            val weight = weightStr.trim().toFloat()
            if (weight >= Constants.MIN_WEIGHT_KG && weight <= Constants.MAX_WEIGHT_KG) {
                weight
            } else {
                null
            }
        } catch (e: NumberFormatException) {
            null
        }
    }
    
    private fun validateAge(ageStr: String): Int? {
        return try {
            val age = ageStr.trim().toInt()
            if (age in 10..120) {
                age
            } else {
                null
            }
        } catch (e: NumberFormatException) {
            null
        }
    }
    
    private fun validateMeasurement(measurementStr: String): Float? {
        return try {
            val measurement = measurementStr.trim().toFloat()
            if (measurement >= Constants.MIN_MEASUREMENT_CM && measurement <= Constants.MAX_MEASUREMENT_CM) {
                measurement
            } else {
                null
            }
        } catch (e: NumberFormatException) {
            null
        }
    }
}