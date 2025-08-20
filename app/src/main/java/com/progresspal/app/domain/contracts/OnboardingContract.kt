package com.progresspal.app.domain.contracts

interface OnboardingContract {
    interface View : BaseContract.View {
        fun showValidationError(field: String, message: String)
        fun clearValidationErrors()
        fun showUserSavedSuccess()
        fun navigateToMainActivity()
        fun setCurrentPage(position: Int)
        fun goToNextPage()
        fun goToPreviousPage()
    }
    
    interface Presenter : BaseContract.Presenter<View> {
        fun saveUserInfo(
            name: String?,
            age: String?,
            height: String,
            gender: String?,
            activityLevel: String?,
            currentWeight: String,
            targetWeight: String?,
            targetDate: String?,
            motivation: String?,
            trackMeasurements: Boolean,
            waist: String?,
            chest: String?,
            hips: String?
        )
        fun onNextClicked(currentPage: Int)
        fun onBackClicked(currentPage: Int)
        fun onSkipClicked()
        fun onFinishClicked()
    }
}