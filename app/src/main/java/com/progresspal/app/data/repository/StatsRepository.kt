package com.progresspal.app.data.repository

import com.progresspal.app.data.database.entities.WeightEntity
import com.progresspal.app.data.database.entities.MeasurementEntity
import com.progresspal.app.utils.BMICalculator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.math.abs

class StatsRepository(
    private val weightRepository: WeightRepository,
    private val measurementRepository: MeasurementRepository,
    private val userRepository: UserRepository
) {
    
    suspend fun calculateBMI(userId: Long): Float? = withContext(Dispatchers.IO) {
        val user = userRepository.getUserSync() ?: return@withContext null
        val latestWeight = weightRepository.getLatestWeightSync(userId) ?: return@withContext null
        
        BMICalculator.calculate(latestWeight.weight, user.height)
    }
    
    suspend fun calculateWeightProgress(userId: Long): Float? = withContext(Dispatchers.IO) {
        val user = userRepository.getUserSync() ?: return@withContext null
        val targetWeight = user.targetWeight ?: return@withContext null
        val latestWeight = weightRepository.getLatestWeightSync(userId) ?: return@withContext null
        
        val totalToLose = user.initialWeight - targetWeight
        val actualLost = user.initialWeight - latestWeight.weight
        
        if (totalToLose == 0f) return@withContext 100f
        (actualLost / totalToLose) * 100f
    }
    
    suspend fun getWeeklyAverageChange(userId: Long): Float? = withContext(Dispatchers.IO) {
        val calendar = Calendar.getInstance()
        val endDate = calendar.time
        calendar.add(Calendar.WEEK_OF_YEAR, -4) // Last 4 weeks for better average
        val startDate = calendar.time
        
        val weights = weightRepository.getWeightsInRange(userId, startDate, endDate).value
            ?: return@withContext null
        
        if (weights.size < 2) return@withContext null
        
        val sortedWeights = weights.sortedBy { it.date }
        val firstWeight = sortedWeights.first()
        val lastWeight = sortedWeights.last()
        
        val daysDiff = ((lastWeight.date.time - firstWeight.date.time) / (1000 * 60 * 60 * 24)).toInt()
        val weeksDiff = daysDiff / 7.0f
        
        if (weeksDiff == 0f) return@withContext null
        
        (lastWeight.weight - firstWeight.weight) / weeksDiff
    }
    
    suspend fun estimateDaysToGoal(userId: Long): Int? = withContext(Dispatchers.IO) {
        val user = userRepository.getUserSync() ?: return@withContext null
        val targetWeight = user.targetWeight ?: return@withContext null
        val latestWeight = weightRepository.getLatestWeightSync(userId) ?: return@withContext null
        val weeklyChange = getWeeklyAverageChange(userId) ?: return@withContext null
        
        if (weeklyChange == 0f) return@withContext null
        
        val totalChange = abs(targetWeight - latestWeight.weight)
        val dailyChange = weeklyChange / 7f
        
        if (abs(dailyChange) < 0.01f) return@withContext null
        
        (totalChange / abs(dailyChange)).toInt()
    }
    
    suspend fun getMeasurementChange(userId: Long, measurementType: String, daysPeriod: Int = 30): Float? = 
        withContext(Dispatchers.IO) {
            val calendar = Calendar.getInstance()
            val endDate = calendar.time
            calendar.add(Calendar.DAY_OF_YEAR, -daysPeriod)
            val startDate = calendar.time
            
            val measurements = measurementRepository.getMeasurementsInRange(userId, startDate, endDate).value
                ?.filter { it.measurementType == measurementType }
                ?.sortedBy { it.date }
                ?: return@withContext null
            
            if (measurements.size < 2) return@withContext null
            
            measurements.last().value - measurements.first().value
        }
    
    data class QuickStat(
        val label: String,
        val value: String,
        val change: String? = null,
        val isPositive: Boolean = true
    )
    
    suspend fun getQuickStats(userId: Long): List<QuickStat> = withContext(Dispatchers.IO) {
        val stats = mutableListOf<QuickStat>()
        
        // BMI
        calculateBMI(userId)?.let { bmi ->
            val category = BMICalculator.getCategory(bmi)
            stats.add(
                QuickStat(
                    label = "BMI",
                    value = String.format("%.1f (%s)", bmi, category)
                )
            )
        }
        
        // Weight Progress
        calculateWeightProgress(userId)?.let { progress ->
            stats.add(
                QuickStat(
                    label = "Progress",
                    value = String.format("%.1f%%", progress),
                    isPositive = progress >= 0
                )
            )
        }
        
        // Waist measurement
        getMeasurementChange(userId, "waist")?.let { change ->
            val latest = measurementRepository.getLatestMeasurementByType(userId, "waist")
            latest?.let { measurement ->
                stats.add(
                    QuickStat(
                        label = "Waist",
                        value = "${measurement.value.toInt()} cm",
                        change = if (change != 0f) String.format("%.1f cm", change) else null,
                        isPositive = change <= 0 // Negative change is positive for waist
                    )
                )
            }
        }
        
        stats
    }
}