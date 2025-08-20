package com.progresspal.app.data.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.progresspal.app.data.database.entities.WeightEntity
import java.util.Date

@Dao
interface WeightDao {
    
    @Query("SELECT * FROM weights WHERE user_id = :userId ORDER BY date DESC, created_at DESC")
    fun getAllWeights(userId: Long): LiveData<List<WeightEntity>>
    
    @Query("SELECT * FROM weights WHERE user_id = :userId ORDER BY date DESC, created_at DESC LIMIT :limit")
    fun getRecentWeights(userId: Long, limit: Int): LiveData<List<WeightEntity>>
    
    @Query("SELECT * FROM weights WHERE user_id = :userId ORDER BY date DESC, created_at DESC LIMIT 1")
    fun getLatestWeight(userId: Long): LiveData<WeightEntity?>
    
    @Query("SELECT * FROM weights WHERE user_id = :userId ORDER BY date DESC, created_at DESC LIMIT 1")
    suspend fun getLatestWeightSync(userId: Long): WeightEntity?
    
    @Query("SELECT * FROM weights WHERE user_id = :userId ORDER BY date DESC, created_at DESC")
    suspend fun getAllWeightsSync(userId: Long): List<WeightEntity>
    
    @Query("SELECT * FROM weights WHERE user_id = :userId AND date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    fun getWeightsInRange(userId: Long, startDate: Date, endDate: Date): LiveData<List<WeightEntity>>
    
    @Query("SELECT * FROM weights WHERE id = :weightId")
    suspend fun getWeightById(weightId: Long): WeightEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeight(weight: WeightEntity): Long
    
    @Update
    suspend fun updateWeight(weight: WeightEntity)
    
    @Delete
    suspend fun deleteWeight(weight: WeightEntity)
    
    @Query("DELETE FROM weights WHERE id = :weightId")
    suspend fun deleteWeightById(weightId: Long)
    
    @Query("DELETE FROM weights WHERE user_id = :userId")
    suspend fun deleteAllWeights(userId: Long)
    
    @Query("SELECT COUNT(*) FROM weights WHERE user_id = :userId")
    suspend fun getWeightCount(userId: Long): Int
    
    @Query("SELECT AVG(weight) FROM weights WHERE user_id = :userId AND date BETWEEN :startDate AND :endDate")
    suspend fun getAverageWeightInPeriod(userId: Long, startDate: Date, endDate: Date): Float?
}