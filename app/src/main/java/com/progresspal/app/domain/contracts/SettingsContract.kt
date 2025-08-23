package com.progresspal.app.domain.contracts

import android.content.Context
import com.progresspal.app.domain.models.User
import com.progresspal.app.domain.models.MeasurementSystem
import com.progresspal.app.domain.models.MedicalGuidelines
import com.progresspal.app.domain.models.ActivityLevel
import java.io.File
import java.util.Date

interface SettingsContract {
    interface View : BaseContract.View {
        fun showUser(user: User)
        fun showExportProgress()
        fun hideExportProgress()
        fun showExportSuccess(file: File)
        fun showExportError(message: String)
        fun navigateToEditProfile()
        fun showDeleteConfirmation()
        fun navigateToOnboarding()
        fun showAppInfo(version: String, buildDate: String)
        fun getContext(): Context?
        
        // Health Settings Display
        fun updateMeasurementSystemDisplay(system: MeasurementSystem)
        fun updateMedicalGuidelinesDisplay(guidelines: MedicalGuidelines)
        fun updateActivityLevelDisplay(level: ActivityLevel)
        fun updateBirthDateDisplay(birthDate: Date?)
        
        // Health Settings Selection Dialogs
        fun showMeasurementSystemDialog(current: MeasurementSystem)
        fun showMedicalGuidelinesDialog(current: MedicalGuidelines)
        fun showActivityLevelDialog(current: ActivityLevel)
        fun showBirthDatePicker(current: Date?)
    }
    
    interface Presenter : BaseContract.Presenter<View> {
        fun loadUserProfile()
        fun onEditProfileClicked()
        fun onExportDataClicked()
        fun onDeleteAllDataClicked()
        fun confirmDeleteAllData()
        fun onAboutClicked()
        fun onPrivacyPolicyClicked()
        fun onSupportClicked()
        
        // Health Settings Actions
        fun onMeasurementSystemClicked()
        fun onMedicalGuidelinesClicked()
        fun onActivityLevelClicked()
        fun onBirthDateClicked()
        
        // Health Settings Updates
        fun updateMeasurementSystem(system: MeasurementSystem)
        fun updateMedicalGuidelines(guidelines: MedicalGuidelines)
        fun updateActivityLevel(level: ActivityLevel)
        fun updateBirthDate(birthDate: Date?)
        
        // Direct change handlers for RadioGroup selections
        fun onMeasurementSystemChanged(system: MeasurementSystem)
        fun onMedicalGuidelinesChanged(guidelines: MedicalGuidelines)
    }
}