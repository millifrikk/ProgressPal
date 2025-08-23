package com.progresspal.app.data.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.progresspal.app.data.database.entities.BloodPressureEntity
import com.progresspal.app.data.database.entities.BloodPressureAverages

/**
 * Data Access Object for blood pressure measurements
 * Provides comprehensive CRUD operations with LiveData support
 * Follows AndroidX Room best practices for async operations
 */
@Dao
interface BloodPressureDao {
    
    // ========================= INSERT OPERATIONS =========================
    
    /**
     * Insert a new blood pressure measurement
     * @param bloodPressure The measurement to insert
     * @return The row ID of the inserted measurement
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(bloodPressure: BloodPressureEntity): Long
    
    /**
     * Insert multiple blood pressure measurements
     * @param bloodPressures List of measurements to insert
     * @return List of row IDs for inserted measurements
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(bloodPressures: List<BloodPressureEntity>): List<Long>
    
    // ========================= UPDATE OPERATIONS =========================
    
    /**
     * Update an existing blood pressure measurement
     * @param bloodPressure The measurement to update
     * @return Number of rows affected
     */
    @Update
    suspend fun update(bloodPressure: BloodPressureEntity): Int
    
    /**
     * Update multiple blood pressure measurements
     * @param bloodPressures List of measurements to update
     * @return Number of rows affected
     */
    @Update
    suspend fun updateAll(bloodPressures: List<BloodPressureEntity>): Int
    
    // ========================= DELETE OPERATIONS =========================
    
    /**
     * Delete a blood pressure measurement
     * @param bloodPressure The measurement to delete
     * @return Number of rows affected
     */
    @Delete
    suspend fun delete(bloodPressure: BloodPressureEntity): Int
    
    /**
     * Delete measurement by ID
     * @param id The ID of the measurement to delete
     * @return Number of rows affected
     */
    @Query("DELETE FROM blood_pressure WHERE id = :id")
    suspend fun deleteById(id: String): Int
    
    /**
     * Delete all measurements for a specific user
     * @param userId The user ID
     * @return Number of rows affected
     */
    @Query("DELETE FROM blood_pressure WHERE userId = :userId")
    suspend fun deleteAllForUser(userId: Long): Int
    
    /**
     * Delete measurements older than specified timestamp
     * @param userId The user ID
     * @param olderThan Timestamp threshold
     * @return Number of rows affected
     */
    @Query("DELETE FROM blood_pressure WHERE userId = :userId AND timestamp < :olderThan")
    suspend fun deleteOlderThan(userId: Long, olderThan: Long): Int
    
    // ========================= SELECT OPERATIONS =========================
    
    /**
     * Get all blood pressure measurements for a user (LiveData)
     * @param userId The user ID
     * @return LiveData list of measurements ordered by timestamp descending
     */
    @Query("SELECT * FROM blood_pressure WHERE userId = :userId ORDER BY timestamp DESC")
    fun getAllForUser(userId: Long): LiveData<List<BloodPressureEntity>>
    
    /**
     * Get all blood pressure measurements for a user (suspend function)
     * @param userId The user ID
     * @return List of measurements ordered by timestamp descending
     */
    @Query("SELECT * FROM blood_pressure WHERE userId = :userId ORDER BY timestamp DESC")
    suspend fun getAllForUserSync(userId: Long): List<BloodPressureEntity>
    
    /**
     * Get the most recent blood pressure measurement for a user
     * @param userId The user ID
     * @return The latest measurement or null if none exists
     */
    @Query("SELECT * FROM blood_pressure WHERE userId = :userId ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestForUser(userId: Long): BloodPressureEntity?
    
    /**
     * Get the most recent blood pressure measurement for a user (LiveData)
     * @param userId The user ID
     * @return LiveData of the latest measurement
     */
    @Query("SELECT * FROM blood_pressure WHERE userId = :userId ORDER BY timestamp DESC LIMIT 1")
    fun getLatestForUserLive(userId: Long): LiveData<BloodPressureEntity?>
    
    /**
     * Get blood pressure measurement by ID
     * @param id The measurement ID
     * @return The measurement or null if not found
     */
    @Query("SELECT * FROM blood_pressure WHERE id = :id")
    suspend fun getById(id: String): BloodPressureEntity?
    
    /**
     * Get measurements within a date range
     * @param userId The user ID
     * @param startTime Start timestamp
     * @param endTime End timestamp
     * @return List of measurements in the specified range
     */
    @Query("SELECT * FROM blood_pressure WHERE userId = :userId AND timestamp >= :startTime AND timestamp <= :endTime ORDER BY timestamp DESC")
    suspend fun getInDateRange(userId: Long, startTime: Long, endTime: Long): List<BloodPressureEntity>
    
    /**
     * Get recent measurements (within specified time)
     * @param userId The user ID
     * @param sinceTime Timestamp to get measurements since
     * @return List of recent measurements
     */
    @Query("SELECT * FROM blood_pressure WHERE userId = :userId AND timestamp >= :sinceTime ORDER BY timestamp DESC")
    suspend fun getRecentReadings(userId: Long, sinceTime: Long): List<BloodPressureEntity>
    
    /**
     * Get measurements for a specific time of day
     * @param userId The user ID
     * @param timeOfDay The time of day ("morning", "afternoon", "evening")
     * @param limit Maximum number of results
     * @return List of measurements for the specified time of day
     */
    @Query("SELECT * FROM blood_pressure WHERE userId = :userId AND timeOfDay = :timeOfDay ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getByTimeOfDay(userId: Long, timeOfDay: String, limit: Int = 30): List<BloodPressureEntity>
    
    // ========================= STATISTICS OPERATIONS =========================
    
    /**
     * Get average blood pressure and pulse for a user
     * @param userId The user ID
     * @param sinceTime Timestamp to calculate averages since
     * @return Averages data or null if no measurements
     */
    @Query("""
        SELECT 
            AVG(systolic) as avgSystolic,
            AVG(diastolic) as avgDiastolic,
            AVG(pulse) as avgPulse,
            COUNT(*) as readingCount
        FROM blood_pressure 
        WHERE userId = :userId AND timestamp >= :sinceTime
    """)
    suspend fun getAverages(userId: Long, sinceTime: Long): BloodPressureAverages?
    
    /**
     * Get averages for all time
     * @param userId The user ID
     * @return Averages data or null if no measurements
     */
    @Query("""
        SELECT 
            AVG(systolic) as avgSystolic,
            AVG(diastolic) as avgDiastolic,
            AVG(pulse) as avgPulse,
            COUNT(*) as readingCount
        FROM blood_pressure 
        WHERE userId = :userId
    """)
    suspend fun getAllTimeAverages(userId: Long): BloodPressureAverages?
    
    /**
     * Get count of measurements for a user
     * @param userId The user ID
     * @return Total number of measurements
     */
    @Query("SELECT COUNT(*) FROM blood_pressure WHERE userId = :userId")
    suspend fun getCountForUser(userId: Long): Int
    
    /**
     * Get count of measurements in a date range
     * @param userId The user ID
     * @param startTime Start timestamp
     * @param endTime End timestamp
     * @return Number of measurements in range
     */
    @Query("SELECT COUNT(*) FROM blood_pressure WHERE userId = :userId AND timestamp >= :startTime AND timestamp <= :endTime")
    suspend fun getCountInRange(userId: Long, startTime: Long, endTime: Long): Int
    
    /**
     * Get highest systolic reading for a user
     * @param userId The user ID
     * @param sinceTime Timestamp to check since
     * @return Highest systolic value or null
     */
    @Query("SELECT MAX(systolic) FROM blood_pressure WHERE userId = :userId AND timestamp >= :sinceTime")
    suspend fun getMaxSystolic(userId: Long, sinceTime: Long): Int?
    
    /**
     * Get lowest diastolic reading for a user
     * @param userId The user ID
     * @param sinceTime Timestamp to check since
     * @return Lowest diastolic value or null
     */
    @Query("SELECT MIN(diastolic) FROM blood_pressure WHERE userId = :userId AND timestamp >= :sinceTime")
    suspend fun getMinDiastolic(userId: Long, sinceTime: Long): Int?
    
    /**
     * Get measurements with high blood pressure (Stage 1 or higher)
     * @param userId The user ID
     * @param sinceTime Timestamp to check since
     * @return List of high BP measurements
     */
    @Query("SELECT * FROM blood_pressure WHERE userId = :userId AND timestamp >= :sinceTime AND (systolic >= 130 OR diastolic >= 80) ORDER BY timestamp DESC")
    suspend fun getHighBloodPressureReadings(userId: Long, sinceTime: Long): List<BloodPressureEntity>
    
    /**
     * Get measurements requiring immediate attention (Crisis level)
     * @param userId The user ID
     * @param sinceTime Timestamp to check since
     * @return List of crisis-level measurements
     */
    @Query("SELECT * FROM blood_pressure WHERE userId = :userId AND timestamp >= :sinceTime AND (systolic >= 180 OR diastolic >= 120) ORDER BY timestamp DESC")
    suspend fun getCrisisLevelReadings(userId: Long, sinceTime: Long): List<BloodPressureEntity>
    
    // ========================= SEARCH OPERATIONS =========================
    
    /**
     * Search measurements by tags
     * @param userId The user ID
     * @param tag Tag to search for
     * @return List of measurements containing the tag
     */
    @Query("SELECT * FROM blood_pressure WHERE userId = :userId AND tags LIKE '%' || :tag || '%' ORDER BY timestamp DESC")
    suspend fun searchByTag(userId: Long, tag: String): List<BloodPressureEntity>
    
    /**
     * Search measurements by notes content
     * @param userId The user ID
     * @param searchTerm Term to search for in notes
     * @return List of measurements with matching notes
     */
    @Query("SELECT * FROM blood_pressure WHERE userId = :userId AND notes LIKE '%' || :searchTerm || '%' ORDER BY timestamp DESC")
    suspend fun searchByNotes(userId: Long, searchTerm: String): List<BloodPressureEntity>
    
    // ========================= TRENDING DATA =========================
    
    /**
     * Get daily averages for charting
     * @param userId The user ID
     * @param sinceTime Timestamp to get data since
     * @return List of daily average data points
     */
    @Query("""
        SELECT 
            DATE(timestamp/1000, 'unixepoch') as date,
            AVG(systolic) as avgSystolic,
            AVG(diastolic) as avgDiastolic,
            AVG(pulse) as avgPulse,
            COUNT(*) as readingCount
        FROM blood_pressure 
        WHERE userId = :userId AND timestamp >= :sinceTime
        GROUP BY DATE(timestamp/1000, 'unixepoch')
        ORDER BY date DESC
    """)
    suspend fun getDailyAverages(userId: Long, sinceTime: Long): List<DailyBloodPressureAverage>
    
    /**
     * Get weekly averages for charting
     * @param userId The user ID
     * @param sinceTime Timestamp to get data since
     * @return List of weekly average data points
     */
    @Query("""
        SELECT 
            strftime('%Y-W%W', timestamp/1000, 'unixepoch') as week,
            AVG(systolic) as avgSystolic,
            AVG(diastolic) as avgDiastolic,
            AVG(pulse) as avgPulse,
            COUNT(*) as readingCount
        FROM blood_pressure 
        WHERE userId = :userId AND timestamp >= :sinceTime
        GROUP BY strftime('%Y-W%W', timestamp/1000, 'unixepoch')
        ORDER BY week DESC
    """)
    suspend fun getWeeklyAverages(userId: Long, sinceTime: Long): List<WeeklyBloodPressureAverage>
}

/**
 * Data class for daily blood pressure averages
 */
data class DailyBloodPressureAverage(
    val date: String,
    val avgSystolic: Double,
    val avgDiastolic: Double,
    val avgPulse: Double,
    val readingCount: Int
)

/**
 * Data class for weekly blood pressure averages
 */
data class WeeklyBloodPressureAverage(
    val week: String,
    val avgSystolic: Double,
    val avgDiastolic: Double,
    val avgPulse: Double,
    val readingCount: Int
)