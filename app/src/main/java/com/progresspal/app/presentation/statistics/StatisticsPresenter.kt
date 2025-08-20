package com.progresspal.app.presentation.statistics

import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.progresspal.app.data.repository.UserRepository
import com.progresspal.app.data.repository.WeightRepository
import com.progresspal.app.domain.contracts.StatisticsContract
import com.progresspal.app.domain.models.Weight
import com.progresspal.app.utils.BMIUtils
import kotlinx.coroutines.*
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.abs

class StatisticsPresenter(
    private val userRepository: UserRepository,
    private val weightRepository: WeightRepository
) : StatisticsContract.Presenter {
    
    private var view: StatisticsContract.View? = null
    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.Main + job)
    private var weightsLiveData: LiveData<List<com.progresspal.app.data.database.entities.WeightEntity>>? = null
    private var currentTimeRange = "All Time"
    private var allWeights: List<Weight> = emptyList()
    
    private val userObserver = Observer<com.progresspal.app.data.database.entities.UserEntity?> { userEntity ->
        if (isViewAttached()) {
            if (userEntity != null) {
                val user = com.progresspal.app.domain.models.User(
                    id = userEntity.id,
                    name = userEntity.name,
                    age = userEntity.age,
                    height = userEntity.height,
                    gender = userEntity.gender,
                    activityLevel = userEntity.activityLevel,
                    initialWeight = userEntity.initialWeight,
                    currentWeight = userEntity.currentWeight,
                    targetWeight = userEntity.targetWeight,
                    createdAt = userEntity.createdAt,
                    updatedAt = userEntity.updatedAt
                )
                view?.showUser(user)
                loadDetailedStatistics(user)
            } else {
                // No user found - stop loading and show empty state
                view?.hideLoading()
                view?.showEmptyState()
            }
        }
    }
    
    private val weightsObserver = Observer<List<com.progresspal.app.data.database.entities.WeightEntity>> { weightEntities ->
        if (isViewAttached()) {
            allWeights = weightEntities.map { entity ->
                Weight(
                    id = entity.id,
                    userId = entity.userId,
                    weight = entity.weight,
                    date = entity.date,
                    time = entity.time,
                    notes = entity.notes
                )
            }.sortedByDescending { it.date }
            
            applyTimeRangeFilter()
        }
    }
    
    override fun attachView(view: StatisticsContract.View) {
        this.view = view
        userRepository.getUserLiveData().observeForever(userObserver)
        // Get user first to pass userId to getAllWeights
        scope.launch {
            val user = userRepository.getUserSync()
            if (user != null) {
                withContext(Dispatchers.Main) {
                    weightsLiveData = weightRepository.getAllWeightsLiveData(user.id).also { liveData ->
                        liveData.observeForever(weightsObserver)
                    }
                }
            } else {
                // No user exists - show empty state immediately
                withContext(Dispatchers.Main) {
                    view.hideLoading()
                    view.showEmptyState()
                }
            }
        }
    }
    
    override fun detachView() {
        this.view = null
        userRepository.getUserLiveData().removeObserver(userObserver)
        weightsLiveData?.removeObserver(weightsObserver)
        weightsLiveData = null
        job.cancelChildren()
    }
    
    override fun isViewAttached(): Boolean = view != null
    
    override fun loadStatistics() {
        if (!isViewAttached()) return
        view?.showLoading()
        // Data will be loaded through observers
    }
    
    override fun onRefresh() {
        loadStatistics()
    }
    
    override fun exportData() {
        // TODO: Implement data export functionality
        view?.showMessage("Export functionality coming soon!")
    }
    
    override fun onTimeRangeSelected(range: String) {
        currentTimeRange = range
        applyTimeRangeFilter()
    }
    
    private fun applyTimeRangeFilter() {
        if (!isViewAttached() || allWeights.isEmpty()) {
            view?.showEmptyState()
            return
        }
        
        val filteredWeights = when (currentTimeRange) {
            "Last 7 Days" -> {
                val cutoffDate = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_YEAR, -7)
                }.time
                allWeights.filter { it.date.after(cutoffDate) }
            }
            "Last 30 Days" -> {
                val cutoffDate = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_YEAR, -30)
                }.time
                allWeights.filter { it.date.after(cutoffDate) }
            }
            "Last 3 Months" -> {
                val cutoffDate = Calendar.getInstance().apply {
                    add(Calendar.MONTH, -3)
                }.time
                allWeights.filter { it.date.after(cutoffDate) }
            }
            "Last 6 Months" -> {
                val cutoffDate = Calendar.getInstance().apply {
                    add(Calendar.MONTH, -6)
                }.time
                allWeights.filter { it.date.after(cutoffDate) }
            }
            "Last Year" -> {
                val cutoffDate = Calendar.getInstance().apply {
                    add(Calendar.YEAR, -1)
                }.time
                allWeights.filter { it.date.after(cutoffDate) }
            }
            else -> allWeights // "All Time"
        }
        
        if (filteredWeights.isNotEmpty()) {
            view?.showWeightChart(filteredWeights)
            calculateWeightStatistics(filteredWeights)
        } else {
            view?.showEmptyState()
        }
    }
    
    private fun loadDetailedStatistics(user: com.progresspal.app.domain.models.User) {
        scope.launch(Dispatchers.IO) {
            try {
                val weights = weightRepository.getAllWeightsSync(user.id)
                    .map { entity ->
                        Weight(
                            id = entity.id,
                            userId = entity.userId,
                            weight = entity.weight,
                            date = entity.date,
                            time = entity.time,
                            notes = entity.notes
                        )
                    }.sortedBy { it.date }
                
                withContext(Dispatchers.Main) {
                    if (isViewAttached()) {
                        if (weights.isNotEmpty()) {
                            calculateProgressStatistics(user, weights)
                            calculateTrendAnalysis(weights)
                            calculateBMIAnalysis(user, weights)
                            view?.showBMIChart(calculateBMIHistory(user, weights))
                        }
                        view?.hideLoading()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    if (isViewAttached()) {
                        view?.hideLoading()
                        view?.showError("Failed to load statistics: ${e.message}")
                    }
                }
            }
        }
    }
    
    private fun calculateWeightStatistics(weights: List<Weight>) {
        if (weights.isEmpty()) return
        
        val totalEntries = weights.size
        val averageWeight = weights.map { it.weight }.average().toFloat()
        
        // Calculate total weight changes
        var totalLoss = 0f
        var totalGain = 0f
        
        for (i in 1 until weights.size) {
            val change = weights[i-1].weight - weights[i].weight // Sorted by date ascending
            if (change > 0) totalLoss += change
            else totalGain += abs(change)
        }
        
        // Calculate average weekly change
        val daysBetweenFirstAndLast = if (weights.size > 1) {
            TimeUnit.MILLISECONDS.toDays(weights.last().date.time - weights.first().date.time)
        } else 0L
        
        val averageWeeklyChange = if (daysBetweenFirstAndLast > 0 && weights.size > 1) {
            val totalChange = weights.last().weight - weights.first().weight
            (totalChange / daysBetweenFirstAndLast) * 7 // Convert to weekly
        } else 0f
        
        view?.showWeightStatistics(
            totalEntries = totalEntries,
            averageWeight = averageWeight,
            weightLoss = totalLoss,
            weightGain = totalGain,
            averageWeeklyChange = averageWeeklyChange
        )
    }
    
    private fun calculateProgressStatistics(user: com.progresspal.app.domain.models.User, weights: List<Weight>) {
        if (weights.isEmpty()) return
        
        val startWeight = weights.first().weight
        val currentWeight = weights.last().weight
        val goalWeight = user.targetWeight
        
        val progressPercentage = if (goalWeight != null) {
            val totalToLose = startWeight - goalWeight
            val lostSoFar = startWeight - currentWeight
            if (totalToLose != 0f) (lostSoFar / totalToLose) * 100 else 0f
        } else 0f
        
        val daysActive = TimeUnit.MILLISECONDS.toDays(weights.last().date.time - weights.first().date.time).toInt()
        
        val estimatedDaysToGoal = if (goalWeight != null && weights.size > 1 && progressPercentage > 0) {
            val averageDailyChange = (startWeight - currentWeight) / maxOf(1, daysActive)
            val remainingWeight = abs(currentWeight - goalWeight)
            if (averageDailyChange > 0) {
                (remainingWeight / averageDailyChange).toInt()
            } else null
        } else null
        
        view?.showProgressStatistics(
            startWeight = startWeight,
            currentWeight = currentWeight,
            goalWeight = goalWeight,
            progressPercentage = progressPercentage,
            daysActive = daysActive,
            estimatedDaysToGoal = estimatedDaysToGoal
        )
    }
    
    private fun calculateTrendAnalysis(weights: List<Weight>) {
        if (weights.size < 2) return
        
        val currentTrend = calculateTrend(weights.takeLast(3))
        val weeklyTrend = calculateTrend(weights.filter { 
            System.currentTimeMillis() - it.date.time <= TimeUnit.DAYS.toMillis(7) 
        })
        val monthlyTrend = calculateTrend(weights.filter { 
            System.currentTimeMillis() - it.date.time <= TimeUnit.DAYS.toMillis(30) 
        })
        
        val (longestStreak, currentStreak) = calculateStreaks(weights)
        
        view?.showTrendAnalysis(
            currentTrend = currentTrend,
            weeklyTrend = weeklyTrend,
            monthlyTrend = monthlyTrend,
            longestStreak = longestStreak,
            currentStreak = currentStreak
        )
    }
    
    private fun calculateBMIAnalysis(user: com.progresspal.app.domain.models.User, weights: List<Weight>) {
        if (weights.isEmpty()) return
        
        val currentBMI = BMIUtils.calculateBMI(weights.last().weight, user.height)
        val bmiCategory = BMIUtils.getBMICategory(currentBMI)
        
        val bmiChange = if (weights.size > 1) {
            val previousBMI = BMIUtils.calculateBMI(weights.first().weight, user.height)
            currentBMI - previousBMI
        } else 0f
        
        val targetBMI = user.targetWeight?.let { BMIUtils.calculateBMI(it, user.height) }
        
        view?.showBMIAnalysis(
            currentBMI = currentBMI,
            bmiCategory = bmiCategory,
            bmiChange = bmiChange,
            targetBMI = targetBMI
        )
    }
    
    private fun calculateTrend(weights: List<Weight>): String {
        return when {
            weights.size < 2 -> "Insufficient data"
            weights.last().weight < weights.first().weight -> "Decreasing"
            weights.last().weight > weights.first().weight -> "Increasing"
            else -> "Stable"
        }
    }
    
    private fun calculateStreaks(weights: List<Weight>): Pair<Int, Int> {
        // Calculate longest streak of weight loss and current streak
        var longestStreak = 0
        var currentStreak = 0
        var tempStreak = 0
        
        for (i in 1 until weights.size) {
            if (weights[i].weight < weights[i-1].weight) {
                tempStreak++
            } else {
                longestStreak = maxOf(longestStreak, tempStreak)
                tempStreak = 0
            }
        }
        longestStreak = maxOf(longestStreak, tempStreak)
        
        // Calculate current streak from the end
        for (i in weights.size - 1 downTo 1) {
            if (weights[i].weight < weights[i-1].weight) {
                currentStreak++
            } else {
                break
            }
        }
        
        return Pair(longestStreak, currentStreak)
    }
    
    private fun calculateBMIHistory(user: com.progresspal.app.domain.models.User, weights: List<Weight>): List<Pair<Weight, Float>> {
        return weights.map { weight ->
            Pair(weight, BMIUtils.calculateBMI(weight.weight, user.height))
        }
    }
}