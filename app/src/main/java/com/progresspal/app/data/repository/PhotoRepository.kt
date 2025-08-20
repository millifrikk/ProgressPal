package com.progresspal.app.data.repository

import androidx.lifecycle.LiveData
import com.progresspal.app.data.database.dao.PhotoDao
import com.progresspal.app.data.database.entities.PhotoEntity
import java.util.Date

/**
 * Repository for managing photo data operations
 * Provides a clean API layer between the UI and the database
 */
class PhotoRepository(private val photoDao: PhotoDao) {
    
    /**
     * Get all photos for a user (LiveData for UI observation)
     */
    fun getAllPhotos(userId: Long): LiveData<List<PhotoEntity>> {
        return photoDao.getAllPhotos(userId)
    }
    
    /**
     * Get photos by type for a user
     */
    fun getPhotosByType(userId: Long, type: String): LiveData<List<PhotoEntity>> {
        return photoDao.getPhotosByType(userId, type)
    }
    
    /**
     * Get photos associated with a specific weight entry
     */
    fun getPhotosByWeightId(weightId: Long): LiveData<List<PhotoEntity>> {
        return photoDao.getPhotosByWeightId(weightId)
    }
    
    /**
     * Get photos within a date range
     */
    fun getPhotosInRange(userId: Long, startDate: Date, endDate: Date): LiveData<List<PhotoEntity>> {
        return photoDao.getPhotosInRange(userId, startDate, endDate)
    }
    
    /**
     * Get a single photo by ID
     */
    suspend fun getPhotoById(photoId: Long): PhotoEntity? {
        return photoDao.getPhotoById(photoId)
    }
    
    /**
     * Insert a new photo
     */
    suspend fun insertPhoto(photo: PhotoEntity): Long {
        return photoDao.insertPhoto(photo)
    }
    
    /**
     * Update an existing photo
     */
    suspend fun updatePhoto(photo: PhotoEntity) {
        photoDao.updatePhoto(photo)
    }
    
    /**
     * Delete a photo
     */
    suspend fun deletePhoto(photo: PhotoEntity) {
        photoDao.deletePhoto(photo)
    }
    
    /**
     * Delete a photo by ID
     */
    suspend fun deletePhotoById(photoId: Long) {
        photoDao.deletePhotoById(photoId)
    }
    
    /**
     * Get photo count for a user
     */
    suspend fun getPhotoCount(userId: Long): Int {
        return photoDao.getPhotoCount(userId)
    }
    
    /**
     * Get all photos synchronously (for background operations)
     */
    suspend fun getAllPhotosSync(userId: Long): List<PhotoEntity> {
        return photoDao.getAllPhotosSync(userId)
    }
    
    /**
     * Get the first photo for before/after comparisons
     */
    suspend fun getFirstPhoto(userId: Long): PhotoEntity? {
        return photoDao.getFirstPhoto(userId)
    }
    
    /**
     * Get the latest photo for before/after comparisons
     */
    suspend fun getLatestPhoto(userId: Long): PhotoEntity? {
        return photoDao.getLatestPhoto(userId)
    }
    
    /**
     * Delete all photos for a user (for account deletion)
     */
    suspend fun deleteAllPhotos(userId: Long) {
        photoDao.deleteAllPhotos(userId)
    }
}