package com.progresspal.app.presentation.settings

import androidx.lifecycle.Observer
import com.progresspal.app.data.repository.UserRepository
import com.progresspal.app.data.repository.WeightRepository
import com.progresspal.app.domain.contracts.SettingsContract
import com.progresspal.app.domain.models.User
import kotlinx.coroutines.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class SettingsPresenter(
    private val userRepository: UserRepository,
    private val weightRepository: WeightRepository
) : SettingsContract.Presenter {
    
    private var view: SettingsContract.View? = null
    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.Main + job)
    
    private val userObserver = Observer<com.progresspal.app.data.database.entities.UserEntity?> { userEntity ->
        if (userEntity != null && isViewAttached()) {
            val user = User(
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
        }
    }
    
    override fun attachView(view: SettingsContract.View) {
        this.view = view
        userRepository.getUserLiveData().observeForever(userObserver)
    }
    
    override fun detachView() {
        this.view = null
        userRepository.getUserLiveData().removeObserver(userObserver)
        job.cancelChildren()
    }
    
    override fun isViewAttached(): Boolean = view != null
    
    override fun loadUserProfile() {
        // Data will be loaded through observer
    }
    
    override fun onEditProfileClicked() {
        view?.navigateToEditProfile()
    }
    
    override fun onExportDataClicked() {
        if (!isViewAttached()) return
        
        view?.showExportProgress()
        
        scope.launch(Dispatchers.IO) {
            try {
                val user = userRepository.getUserSync()
                val weights = if (user != null) weightRepository.getAllWeightsSync(user.id) else emptyList()
                
                val exportData = buildExportData(user, weights)
                val file = saveExportFile(exportData)
                
                withContext(Dispatchers.Main) {
                    if (isViewAttached()) {
                        view?.hideExportProgress()
                        view?.showExportSuccess(file)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    if (isViewAttached()) {
                        view?.hideExportProgress()
                        view?.showExportError("Failed to export data: ${e.message}")
                    }
                }
            }
        }
    }
    
    override fun onDeleteAllDataClicked() {
        view?.showDeleteConfirmation()
    }
    
    override fun confirmDeleteAllData() {
        if (!isViewAttached()) return
        
        view?.showLoading()
        
        scope.launch(Dispatchers.IO) {
            try {
                val user = userRepository.getUserSync()
                user?.let { 
                    weightRepository.deleteAllWeights(it.id)
                }
                userRepository.deleteUser()
                
                withContext(Dispatchers.Main) {
                    if (isViewAttached()) {
                        view?.hideLoading()
                        view?.showMessage("All data deleted successfully")
                        view?.navigateToOnboarding()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    if (isViewAttached()) {
                        view?.hideLoading()
                        view?.showError("Failed to delete data: ${e.message}")
                    }
                }
            }
        }
    }
    
    override fun onAboutClicked() {
        val version = "1.0.0" // BuildConfig.VERSION_NAME - temporarily hardcoded
        val buildDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        view?.showAppInfo(version, buildDate)
    }
    
    override fun onPrivacyPolicyClicked() {
        view?.showMessage("Privacy policy functionality coming soon")
    }
    
    override fun onSupportClicked() {
        view?.showMessage("Support functionality coming soon")
    }
    
    private fun buildExportData(
        user: com.progresspal.app.data.database.entities.UserEntity?,
        weights: List<com.progresspal.app.data.database.entities.WeightEntity>
    ): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val exportDate = dateFormat.format(Date())
        
        val sb = StringBuilder()
        sb.appendLine("ProgressPal Data Export")
        sb.appendLine("Export Date: $exportDate")
        sb.appendLine()
        
        // User Profile
        sb.appendLine("=== USER PROFILE ===")
        if (user != null) {
            sb.appendLine("Name: ${user.name ?: "Not set"}")
            sb.appendLine("Age: ${user.age ?: "Not set"}")
            sb.appendLine("Height: ${user.height} cm")
            sb.appendLine("Gender: ${user.gender ?: "Not set"}")
            sb.appendLine("Activity Level: ${user.activityLevel ?: "Not set"}")
            sb.appendLine("Initial Weight: ${user.initialWeight} kg")
            sb.appendLine("Current Weight: ${user.currentWeight} kg")
            sb.appendLine("Target Weight: ${user.targetWeight?.let { "$it kg" } ?: "Not set"}")
            sb.appendLine("Account Created: ${dateFormat.format(user.createdAt)}")
            sb.appendLine("Last Updated: ${dateFormat.format(user.updatedAt)}")
        } else {
            sb.appendLine("No user profile found")
        }
        sb.appendLine()
        
        // Weight History
        sb.appendLine("=== WEIGHT HISTORY ===")
        if (weights.isNotEmpty()) {
            sb.appendLine("Total Entries: ${weights.size}")
            sb.appendLine()
            sb.appendLine("Date,Time,Weight (kg),Notes")
            weights.sortedBy { it.date }.forEach { weight ->
                val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(weight.date)
                val time = weight.time ?: ""
                val notes = weight.notes?.replace(",", ";") ?: "" // Replace commas to avoid CSV issues
                sb.appendLine("$date,$time,${weight.weight},$notes")
            }
        } else {
            sb.appendLine("No weight entries found")
        }
        
        return sb.toString()
    }
    
    private fun saveExportFile(data: String): File {
        // Use Android's external files directory for app-specific files
        val fileName = "progresspal_export_${System.currentTimeMillis()}.csv"
        val externalDir = view?.getContext()?.getExternalFilesDir(null)
        val file = File(externalDir, fileName)
        file.writeText(data)
        return file
    }
}