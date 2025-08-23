package com.progresspal.app.data.repository

import androidx.lifecycle.LiveData
import com.progresspal.app.data.database.dao.BloodPressureDao
import com.progresspal.app.data.database.entities.BloodPressureEntity
import com.progresspal.app.data.database.entities.BloodPressureAverages
import com.progresspal.app.data.database.dao.DailyBloodPressureAverage
import com.progresspal.app.data.database.dao.WeeklyBloodPressureAverage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

/**
 * Repository class for blood pressure data management
 * Provides a clean API for blood pressure operations following the Repository pattern
 * Handles data transformations and business logic for blood pressure measurements
 */
class BloodPressureRepository(private val bloodPressureDao: BloodPressureDao) {
    
    // ========================= CRUD OPERATIONS =========================
    
    /**
     * Insert a new blood pressure measurement
     * @param bloodPressure The measurement to insert
     * @return The row ID of the inserted measurement
     */
    suspend fun insert(bloodPressure: BloodPressureEntity): Long {
        return withContext(Dispatchers.IO) {
            bloodPressureDao.insert(bloodPressure)
        }
    }
    
    /**
     * Insert multiple blood pressure measurements
     * @param bloodPressures List of measurements to insert
     * @return List of row IDs for inserted measurements
     */
    suspend fun insertAll(bloodPressures: List<BloodPressureEntity>): List<Long> {
        return withContext(Dispatchers.IO) {
            bloodPressureDao.insertAll(bloodPressures)
        }
    }
    
    /**
     * Update an existing blood pressure measurement
     * @param bloodPressure The measurement to update
     * @return Number of rows affected
     */
    suspend fun update(bloodPressure: BloodPressureEntity): Int {
        return withContext(Dispatchers.IO) {
            bloodPressureDao.update(bloodPressure)
        }
    }
    
    /**
     * Delete a blood pressure measurement
     * @param bloodPressure The measurement to delete
     * @return Number of rows affected
     */
    suspend fun delete(bloodPressure: BloodPressureEntity): Int {
        return withContext(Dispatchers.IO) {
            bloodPressureDao.delete(bloodPressure)
        }
    }
    
    /**
     * Delete measurement by ID
     * @param id The ID of the measurement to delete
     * @return Number of rows affected
     */
    suspend fun deleteById(id: String): Int {
        return withContext(Dispatchers.IO) {
            bloodPressureDao.deleteById(id)
        }
    }
    
    // ========================= DATA RETRIEVAL =========================
    
    /**
     * Get all blood pressure measurements for a user (LiveData)
     * @param userId The user ID
     * @return LiveData list of measurements ordered by timestamp descending
     */
    fun getAllForUser(userId: Long): LiveData<List<BloodPressureEntity>> {
        return bloodPressureDao.getAllForUser(userId)
    }
    
    /**
     * Get all blood pressure measurements for a user (suspend function)
     * @param userId The user ID
     * @return List of measurements ordered by timestamp descending
     */
    suspend fun getAllForUserSync(userId: Long): List<BloodPressureEntity> {
        return withContext(Dispatchers.IO) {
            bloodPressureDao.getAllForUserSync(userId)
        }
    }
    
    /**
     * Get the most recent blood pressure measurement for a user
     * @param userId The user ID
     * @return The latest measurement or null if none exists
     */
    suspend fun getLatestForUser(userId: Long): BloodPressureEntity? {
        return withContext(Dispatchers.IO) {
            bloodPressureDao.getLatestForUser(userId)
        }
    }
    
    /**
     * Get the most recent blood pressure measurement for a user (LiveData)
     * @param userId The user ID
     * @return LiveData of the latest measurement
     */
    fun getLatestForUserLive(userId: Long): LiveData<BloodPressureEntity?> {
        return bloodPressureDao.getLatestForUserLive(userId)
    }
    
    /**
     * Get blood pressure measurement by ID
     * @param id The measurement ID
     * @return The measurement or null if not found
     */
    suspend fun getById(id: String): BloodPressureEntity? {
        return withContext(Dispatchers.IO) {
            bloodPressureDao.getById(id)
        }
    }
    
    // ========================= TIME-BASED QUERIES =========================
    
    /**
     * Get measurements from the last 7 days
     * @param userId The user ID
     * @return List of measurements from the past week
     */
    suspend fun getLastWeekReadings(userId: Long): List<BloodPressureEntity> {
        val sevenDaysAgo = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(7)
        return withContext(Dispatchers.IO) {
            bloodPressureDao.getRecentReadings(userId, sevenDaysAgo)
        }
    }
    
    /**
     * Get measurements from the last 30 days
     * @param userId The user ID
     * @return List of measurements from the past month
     */
    suspend fun getLastMonthReadings(userId: Long): List<BloodPressureEntity> {
        val thirtyDaysAgo = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(30)
        return withContext(Dispatchers.IO) {
            bloodPressureDao.getRecentReadings(userId, thirtyDaysAgo)
        }
    }
    
    /**
     * Get measurements from the last 90 days
     * @param userId The user ID
     * @return List of measurements from the past 3 months
     */
    suspend fun getLastThreeMonthsReadings(userId: Long): List<BloodPressureEntity> {
        val ninetyDaysAgo = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(90)
        return withContext(Dispatchers.IO) {
            bloodPressureDao.getRecentReadings(userId, ninetyDaysAgo)
        }
    }
    
    /**
     * Get measurements within a custom date range
     * @param userId The user ID
     * @param startTime Start timestamp
     * @param endTime End timestamp
     * @return List of measurements in the specified range
     */
    suspend fun getInDateRange(userId: Long, startTime: Long, endTime: Long): List<BloodPressureEntity> {
        return withContext(Dispatchers.IO) {
            bloodPressureDao.getInDateRange(userId, startTime, endTime)
        }
    }
    
    /**
     * Get measurements for a specific time of day
     * @param userId The user ID
     * @param timeOfDay The time of day ("morning", "afternoon", "evening")
     * @param limit Maximum number of results (default: 30)
     * @return List of measurements for the specified time of day
     */
    suspend fun getByTimeOfDay(userId: Long, timeOfDay: String, limit: Int = 30): List<BloodPressureEntity> {
        return withContext(Dispatchers.IO) {
            bloodPressureDao.getByTimeOfDay(userId, timeOfDay, limit)
        }
    }
    
    // ========================= STATISTICS & ANALYTICS =========================
    
    /**
     * Get average blood pressure and pulse for the last week
     * @param userId The user ID
     * @return Averages data or null if no measurements
     */
    suspend fun getLastWeekAverages(userId: Long): BloodPressureAverages? {
        val sevenDaysAgo = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(7)
        return withContext(Dispatchers.IO) {
            bloodPressureDao.getAverages(userId, sevenDaysAgo)
        }
    }
    
    /**
     * Get average blood pressure and pulse for the last month
     * @param userId The user ID
     * @return Averages data or null if no measurements
     */
    suspend fun getLastMonthAverages(userId: Long): BloodPressureAverages? {
        val thirtyDaysAgo = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(30)
        return withContext(Dispatchers.IO) {
            bloodPressureDao.getAverages(userId, thirtyDaysAgo)
        }
    }
    
    /**
     * Get averages for all time
     * @param userId The user ID
     * @return Averages data or null if no measurements
     */
    suspend fun getAllTimeAverages(userId: Long): BloodPressureAverages? {
        return withContext(Dispatchers.IO) {
            bloodPressureDao.getAllTimeAverages(userId)
        }
    }
    
    /**
     * Get count of measurements for a user
     * @param userId The user ID
     * @return Total number of measurements
     */
    suspend fun getCountForUser(userId: Long): Int {
        return withContext(Dispatchers.IO) {
            bloodPressureDao.getCountForUser(userId)
        }
    }
    
    /**
     * Get measurements with high blood pressure (Stage 1 or higher)
     * @param userId The user ID
     * @param days Number of days to look back (default: 30)
     * @return List of high BP measurements
     */
    suspend fun getHighBloodPressureReadings(userId: Long, days: Int = 30): List<BloodPressureEntity> {
        val daysAgo = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(days.toLong())
        return withContext(Dispatchers.IO) {
            bloodPressureDao.getHighBloodPressureReadings(userId, daysAgo)
        }
    }
    
    /**
     * Get measurements requiring immediate attention (Crisis level)
     * @param userId The user ID
     * @param days Number of days to look back (default: 30)
     * @return List of crisis-level measurements
     */
    suspend fun getCrisisLevelReadings(userId: Long, days: Int = 30): List<BloodPressureEntity> {
        val daysAgo = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(days.toLong())
        return withContext(Dispatchers.IO) {
            bloodPressureDao.getCrisisLevelReadings(userId, daysAgo)
        }
    }
    
    // ========================= CHART DATA =========================
    
    /**
     * Get daily averages for charting
     * @param userId The user ID
     * @param days Number of days to include (default: 30)
     * @return List of daily average data points
     */
    suspend fun getDailyAveragesForChart(userId: Long, days: Int = 30): List<DailyBloodPressureAverage> {
        val daysAgo = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(days.toLong())
        return withContext(Dispatchers.IO) {
            bloodPressureDao.getDailyAverages(userId, daysAgo)
        }
    }
    
    /**
     * Get weekly averages for charting
     * @param userId The user ID
     * @param weeks Number of weeks to include (default: 12)
     * @return List of weekly average data points
     */
    suspend fun getWeeklyAveragesForChart(userId: Long, weeks: Int = 12): List<WeeklyBloodPressureAverage> {
        val weeksAgo = System.currentTimeMillis() - TimeUnit.DAYS.toMillis((weeks * 7).toLong())
        return withContext(Dispatchers.IO) {
            bloodPressureDao.getWeeklyAverages(userId, weeksAgo)
        }
    }
    
    // ========================= SEARCH & FILTERING =========================
    
    /**
     * Search measurements by tags
     * @param userId The user ID
     * @param tag Tag to search for
     * @return List of measurements containing the tag
     */
    suspend fun searchByTag(userId: Long, tag: String): List<BloodPressureEntity> {
        return withContext(Dispatchers.IO) {
            bloodPressureDao.searchByTag(userId, tag)
        }
    }
    
    /**
     * Search measurements by notes content
     * @param userId The user ID
     * @param searchTerm Term to search for in notes
     * @return List of measurements with matching notes
     */
    suspend fun searchByNotes(userId: Long, searchTerm: String): List<BloodPressureEntity> {
        return withContext(Dispatchers.IO) {
            bloodPressureDao.searchByNotes(userId, searchTerm)
        }
    }
    
    // ========================= BUSINESS LOGIC METHODS =========================
    
    /**
     * Check if user has any blood pressure measurements
     * @param userId The user ID
     * @return True if user has measurements, false otherwise
     */
    suspend fun hasAnyMeasurements(userId: Long): Boolean {
        return withContext(Dispatchers.IO) {
            bloodPressureDao.getCountForUser(userId) > 0
        }
    }
    
    /**
     * Check if user has measurements in the last week
     * @param userId The user ID
     * @return True if user has recent measurements, false otherwise
     */
    suspend fun hasRecentMeasurements(userId: Long): Boolean {
        val sevenDaysAgo = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(7)
        return withContext(Dispatchers.IO) {
            bloodPressureDao.getCountInRange(userId, sevenDaysAgo, System.currentTimeMillis()) > 0
        }
    }
    
    /**
     * Get the user's blood pressure trend (improving, stable, worsening)
     * @param userId The user ID
     * @return Trend analysis string
     */
    suspend fun getBloodPressureTrend(userId: Long): String {
        return withContext(Dispatchers.IO) {
            val recent30Days = getLastMonthReadings(userId)
            if (recent30Days.size < 3) return@withContext "Insufficient data"
            
            // Split into first half and second half of the period
            val midPoint = recent30Days.size / 2
            val recentHalf = recent30Days.take(midPoint)
            val olderHalf = recent30Days.drop(midPoint)
            
            val recentAvgSystolic = recentHalf.map { it.systolic }.average()
            val olderAvgSystolic = olderHalf.map { it.systolic }.average()
            
            when {
                recentAvgSystolic < olderAvgSystolic - 5 -> "Improving"
                recentAvgSystolic > olderAvgSystolic + 5 -> "Worsening" 
                else -> "Stable"
            }
        }
    }
    
    /**
     * Get recommendations based on recent readings
     * @param userId The user ID
     * @return List of personalized recommendations
     */
    suspend fun getPersonalizedRecommendations(userId: Long): List<String> {
        return withContext(Dispatchers.IO) {
            val recommendations = mutableListOf<String>()
            val recentReadings = getLastWeekReadings(userId)
            
            if (recentReadings.isEmpty()) {
                recommendations.add("Start tracking your blood pressure regularly for better health insights")
                return@withContext recommendations
            }
            
            // Check for high blood pressure
            val highReadings = recentReadings.filter { 
                it.getCategory().priority >= 4 // Stage 1 or higher
            }
            
            if (highReadings.isNotEmpty()) {
                recommendations.add("Consider consulting your healthcare provider about elevated blood pressure readings")
            }
            
            // Check measurement frequency
            if (recentReadings.size < 3) {
                recommendations.add("Try to measure your blood pressure at least 3 times per week for better tracking")
            }
            
            // Check for consistency in timing
            val timeVariety = recentReadings.map { it.timeOfDay }.distinct()
            if (timeVariety.size == 1) {
                recommendations.add("Consider measuring at different times of day for a complete picture")
            }
            
            // Check pulse variations
            val pulseReadings = recentReadings.map { it.pulse }
            val avgPulse = pulseReadings.average()
            if (avgPulse < 60) {
                recommendations.add("Your resting heart rate is quite low - this may be normal if you're very fit")
            } else if (avgPulse > 100) {
                recommendations.add("Your resting heart rate is elevated - consider discussing with a healthcare provider")
            }
            
            recommendations
        }
    }
    
    // ========================= DATA CLEANUP =========================
    
    /**
     * Delete old measurements (older than specified days)
     * @param userId The user ID
     * @param keepDays Number of days to keep (default: 365)
     * @return Number of rows deleted
     */
    suspend fun cleanupOldMeasurements(userId: Long, keepDays: Int = 365): Int {
        val cutoffTime = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(keepDays.toLong())
        return withContext(Dispatchers.IO) {
            bloodPressureDao.deleteOlderThan(userId, cutoffTime)
        }
    }
    
    /**
     * Delete all measurements for a user
     * @param userId The user ID
     * @return Number of rows deleted
     */
    suspend fun deleteAllForUser(userId: Long): Int {
        return withContext(Dispatchers.IO) {
            bloodPressureDao.deleteAllForUser(userId)
        }
    }
}