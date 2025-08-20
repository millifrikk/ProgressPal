package com.progresspal.app.data.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.progresspal.app.data.database.entities.MeasurementEntity
import java.util.Date

@Dao
interface MeasurementDao {
    
    @Query("SELECT * FROM measurements WHERE user_id = :userId ORDER BY date DESC, created_at DESC")
    fun getAllMeasurements(userId: Long): LiveData<List<MeasurementEntity>>
    
    @Query("SELECT * FROM measurements WHERE user_id = :userId AND measurement_type = :type ORDER BY date DESC, created_at DESC")
    fun getMeasurementsByType(userId: Long, type: String): LiveData<List<MeasurementEntity>>
    
    @Query("SELECT * FROM measurements WHERE user_id = :userId AND measurement_type = :type ORDER BY date DESC, created_at DESC LIMIT 1")
    suspend fun getLatestMeasurementByType(userId: Long, type: String): MeasurementEntity?
    
    @Query("SELECT * FROM measurements WHERE user_id = :userId AND date = :date")
    fun getMeasurementsByDate(userId: Long, date: Date): LiveData<List<MeasurementEntity>>
    
    @Query("SELECT * FROM measurements WHERE user_id = :userId AND date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    fun getMeasurementsInRange(userId: Long, startDate: Date, endDate: Date): LiveData<List<MeasurementEntity>>
    
    @Query("SELECT DISTINCT measurement_type FROM measurements WHERE user_id = :userId ORDER BY measurement_type")
    fun getMeasurementTypes(userId: Long): LiveData<List<String>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeasurement(measurement: MeasurementEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeasurements(measurements: List<MeasurementEntity>)
    
    @Update
    suspend fun updateMeasurement(measurement: MeasurementEntity)
    
    @Delete
    suspend fun deleteMeasurement(measurement: MeasurementEntity)
    
    @Query("DELETE FROM measurements WHERE id = :measurementId")
    suspend fun deleteMeasurementById(measurementId: Long)
    
    @Query("DELETE FROM measurements WHERE user_id = :userId AND date = :date")
    suspend fun deleteMeasurementsByDate(userId: Long, date: Date)
    
    @Query("SELECT COUNT(*) FROM measurements WHERE user_id = :userId")
    suspend fun getMeasurementCount(userId: Long): Int
}