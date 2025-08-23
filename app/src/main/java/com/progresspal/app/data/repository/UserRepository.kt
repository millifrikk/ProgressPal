package com.progresspal.app.data.repository

import androidx.lifecycle.LiveData
import com.progresspal.app.data.database.dao.UserDao
import com.progresspal.app.data.database.entities.UserEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Date

class UserRepository(private val userDao: UserDao) {
    
    fun getUserLiveData(): LiveData<UserEntity?> = userDao.getUser()
    
    suspend fun getUserSync(): UserEntity? = withContext(Dispatchers.IO) {
        userDao.getUserSync()
    }
    
    suspend fun getUser(): UserEntity? = withContext(Dispatchers.IO) {
        userDao.getUserSync()
    }
    
    suspend fun insertUser(user: UserEntity): Long = withContext(Dispatchers.IO) {
        userDao.insertUser(user)
    }
    
    suspend fun updateUser(user: UserEntity) = withContext(Dispatchers.IO) {
        userDao.updateUser(user)
    }
    
    suspend fun updateCurrentWeight(userId: Long, weight: Float) = withContext(Dispatchers.IO) {
        userDao.updateCurrentWeight(userId, weight)
    }
    
    suspend fun deleteUser(userId: Long) = withContext(Dispatchers.IO) {
        userDao.deleteUser(userId)
    }
    
    suspend fun deleteUser() = withContext(Dispatchers.IO) {
        // Delete the current user - assuming single user for now
        val currentUser = userDao.getUserSync()
        currentUser?.let {
            userDao.deleteUser(it.id)
        }
    }
    
    suspend fun getUserCount(): Int = withContext(Dispatchers.IO) {
        userDao.getUserCount()
    }
    
    suspend fun hasUser(): Boolean = withContext(Dispatchers.IO) {
        userDao.getUserCount() > 0
    }
    
    // Health Settings Update Methods
    suspend fun updateMeasurementSystem(measurementSystem: String) = withContext(Dispatchers.IO) {
        val currentUser = userDao.getUserSync()
        currentUser?.let { user ->
            val updatedUser = user.copy(
                measurementSystem = measurementSystem,
                updatedAt = Date()
            )
            userDao.updateUser(updatedUser)
        }
    }
    
    suspend fun updateMedicalGuidelines(medicalGuidelines: String) = withContext(Dispatchers.IO) {
        val currentUser = userDao.getUserSync()
        currentUser?.let { user ->
            val updatedUser = user.copy(
                medicalGuidelines = medicalGuidelines,
                updatedAt = Date()
            )
            userDao.updateUser(updatedUser)
        }
    }
    
    suspend fun updateActivityLevel(activityLevel: String) = withContext(Dispatchers.IO) {
        val currentUser = userDao.getUserSync()
        currentUser?.let { user ->
            val updatedUser = user.copy(
                activityLevel = activityLevel,
                updatedAt = Date()
            )
            userDao.updateUser(updatedUser)
        }
    }
    
    suspend fun updateBirthDate(birthDate: Date?) = withContext(Dispatchers.IO) {
        val currentUser = userDao.getUserSync()
        currentUser?.let { user ->
            val updatedUser = user.copy(
                birthDate = birthDate,
                updatedAt = Date()
            )
            userDao.updateUser(updatedUser)
        }
    }
    
    suspend fun updateWaistCircumference(waistCircumference: Float?) = withContext(Dispatchers.IO) {
        val currentUser = userDao.getUserSync()
        currentUser?.let { user ->
            val updatedUser = user.copy(
                waistCircumference = waistCircumference,
                updatedAt = Date()
            )
            userDao.updateUser(updatedUser)
        }
    }
    
    suspend fun updateHipCircumference(hipCircumference: Float?) = withContext(Dispatchers.IO) {
        val currentUser = userDao.getUserSync()
        currentUser?.let { user ->
            val updatedUser = user.copy(
                hipCircumference = hipCircumference,
                updatedAt = Date()
            )
            userDao.updateUser(updatedUser)
        }
    }
    
    suspend fun updatePreferredLanguage(preferredLanguage: String) = withContext(Dispatchers.IO) {
        val currentUser = userDao.getUserSync()
        currentUser?.let { user ->
            val updatedUser = user.copy(
                preferredLanguage = preferredLanguage,
                updatedAt = Date()
            )
            userDao.updateUser(updatedUser)
        }
    }
}