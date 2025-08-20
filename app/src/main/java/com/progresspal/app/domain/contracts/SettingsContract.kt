package com.progresspal.app.domain.contracts

import android.content.Context
import com.progresspal.app.domain.models.User
import java.io.File

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
    }
}