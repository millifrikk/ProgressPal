package com.progresspal.app.domain.contracts

import com.progresspal.app.data.database.entities.BloodPressureEntity
import com.progresspal.app.domain.models.User
import com.progresspal.app.domain.models.Weight
import com.progresspal.app.presentation.dialogs.BodyMeasurementsDialog

interface DashboardContract {
    interface View : BaseContract.View {
        fun showCurrentWeight(weight: Float)
        fun showGoalWeight(weight: Float?)
        fun showProgress(progress: Float)
        fun showBMI(bmi: Float, category: String)
        fun showBodyComposition(user: User, latestWeight: Weight)
        fun showQuickStats(weightEntries: List<Weight>)
        fun showWeightChart(data: List<Weight>)
        fun navigateToAddEntry()
        fun showEmptyState()
        fun showUser(user: User?)
        fun showBloodPressureData(reading: BloodPressureEntity?, trend: String?)
        fun navigateToAddBloodPressure()
        fun navigateToBloodPressureHistory()
        fun navigateToBloodPressureTrends()
    }
    
    interface Presenter : BaseContract.Presenter<View> {
        fun loadDashboardData()
        fun onAddEntryClicked()
        fun onRefresh()
        fun onAddBloodPressureClicked()
        fun onViewBloodPressureHistoryClicked()
        fun onViewBloodPressureTrendsClicked()
        fun onWaistMeasurementAdded(waistCm: Float)
        fun onBodyMeasurementsAdded(measurements: BodyMeasurementsDialog.BodyMeasurements)
    }
}