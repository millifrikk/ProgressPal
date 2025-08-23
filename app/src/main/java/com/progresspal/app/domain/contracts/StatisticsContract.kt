package com.progresspal.app.domain.contracts

import com.progresspal.app.data.database.entities.BloodPressureEntity
import com.progresspal.app.domain.models.User
import com.progresspal.app.domain.models.Weight

interface StatisticsContract {
    interface View : BaseContract.View {
        fun showUser(user: User)
        fun showWeightStatistics(
            totalEntries: Int,
            averageWeight: Float,
            weightLoss: Float,
            weightGain: Float,
            averageWeeklyChange: Float
        )
        fun showProgressStatistics(
            startWeight: Float,
            currentWeight: Float,
            goalWeight: Float?,
            progressPercentage: Float,
            daysActive: Int,
            estimatedDaysToGoal: Int?
        )
        fun showTrendAnalysis(
            currentTrend: String,
            weeklyTrend: String,
            monthlyTrend: String,
            longestStreak: Int,
            currentStreak: Int
        )
        fun showBMIAnalysis(
            currentBMI: Float,
            bmiCategory: String,
            bmiChange: Float,
            targetBMI: Float?
        )
        fun showBloodPressureStatistics(
            totalReadings: Int,
            averageSystolic: Float,
            averageDiastolic: Float,
            averagePulse: Float,
            trend: String,
            highReadings: Int,
            categoryBreakdown: String?
        )
        fun showEmptyState()
        fun showWeightChart(data: List<Weight>)
        fun showBMIChart(data: List<Pair<Weight, Float>>) // Weight entry with calculated BMI
    }
    
    interface Presenter : BaseContract.Presenter<View> {
        fun loadStatistics()
        fun onRefresh()
        fun exportData()
        fun onTimeRangeSelected(range: String)
    }
}