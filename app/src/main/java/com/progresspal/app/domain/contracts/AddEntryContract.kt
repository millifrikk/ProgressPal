package com.progresspal.app.domain.contracts

import java.util.Date

interface AddEntryContract {
    interface View : BaseContract.View {
        fun showWeightValidationError(message: String)
        fun showDateValidationError(message: String)
        fun clearValidationErrors()
        fun showEntrySavedSuccess(message: String)
        fun navigateBack()
        fun setCurrentDate()
        fun showDatePicker(currentDate: Date)
    }
    
    interface Presenter : BaseContract.Presenter<View> {
        fun saveEntry(weight: String, date: Date, photoPath: String?, notes: String?)
        fun onDateClicked(currentDate: Date)
    }
}