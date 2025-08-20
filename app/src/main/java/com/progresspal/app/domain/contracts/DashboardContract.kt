package com.progresspal.app.domain.contracts

import com.progresspal.app.domain.models.User
import com.progresspal.app.domain.models.Weight

interface DashboardContract {
    interface View : BaseContract.View {
        fun showCurrentWeight(weight: Float)
        fun showGoalWeight(weight: Float?)
        fun showProgress(progress: Float)
        fun showBMI(bmi: Float, category: String)
        fun showQuickStats(weightEntries: List<Weight>)
        fun showWeightChart(data: List<Weight>)
        fun navigateToAddEntry()
        fun showEmptyState()
        fun showUser(user: User?)
    }
    
    interface Presenter : BaseContract.Presenter<View> {
        fun loadDashboardData()
        fun onAddEntryClicked()
        fun onRefresh()
    }
}