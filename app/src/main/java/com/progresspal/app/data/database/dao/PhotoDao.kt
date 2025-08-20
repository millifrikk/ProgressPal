package com.progresspal.app.data.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.progresspal.app.data.database.entities.PhotoEntity
import java.util.Date

@Dao
interface PhotoDao {
    
    @Query("SELECT * FROM photos WHERE user_id = :userId ORDER BY date DESC, created_at DESC")
    fun getAllPhotos(userId: Long): LiveData<List<PhotoEntity>>
    
    @Query("SELECT * FROM photos WHERE user_id = :userId AND photo_type = :type ORDER BY date DESC, created_at DESC")
    fun getPhotosByType(userId: Long, type: String): LiveData<List<PhotoEntity>>
    
    @Query("SELECT * FROM photos WHERE weight_id = :weightId")
    fun getPhotosByWeightId(weightId: Long): LiveData<List<PhotoEntity>>
    
    @Query("SELECT * FROM photos WHERE user_id = :userId AND date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    fun getPhotosInRange(userId: Long, startDate: Date, endDate: Date): LiveData<List<PhotoEntity>>
    
    @Query("SELECT * FROM photos WHERE id = :photoId")
    suspend fun getPhotoById(photoId: Long): PhotoEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhoto(photo: PhotoEntity): Long
    
    @Update
    suspend fun updatePhoto(photo: PhotoEntity)
    
    @Delete
    suspend fun deletePhoto(photo: PhotoEntity)
    
    @Query("DELETE FROM photos WHERE id = :photoId")
    suspend fun deletePhotoById(photoId: Long)
    
    @Query("SELECT COUNT(*) FROM photos WHERE user_id = :userId")
    suspend fun getPhotoCount(userId: Long): Int
    
    @Query("SELECT * FROM photos WHERE user_id = :userId ORDER BY date DESC, created_at DESC")
    suspend fun getAllPhotosSync(userId: Long): List<PhotoEntity>
    
    @Query("SELECT * FROM photos WHERE user_id = :userId ORDER BY date ASC LIMIT 1")
    suspend fun getFirstPhoto(userId: Long): PhotoEntity?
    
    @Query("SELECT * FROM photos WHERE user_id = :userId ORDER BY date DESC LIMIT 1")
    suspend fun getLatestPhoto(userId: Long): PhotoEntity?
    
    @Query("DELETE FROM photos WHERE user_id = :userId")
    suspend fun deleteAllPhotos(userId: Long)
}