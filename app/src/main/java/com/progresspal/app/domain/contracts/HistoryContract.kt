package com.progresspal.app.domain.contracts

import com.progresspal.app.domain.models.Weight

interface HistoryContract {
    interface View : BaseContract.View {
        fun showWeightHistory(weights: List<Weight>)
        fun showEmptyState()
        fun showDeleteConfirmation(weight: Weight)
        fun showEditDialog(weight: Weight)
        fun navigateToEditEntry(weightId: Long)
    }
    
    interface Presenter : BaseContract.Presenter<View> {
        fun loadWeightHistory()
        fun onWeightClicked(weight: Weight)
        fun onDeleteClicked(weight: Weight)
        fun onDeleteConfirmed(weight: Weight)
        fun onRefresh()
    }
}