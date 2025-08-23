package com.progresspal.app.presentation.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.progresspal.app.data.database.ProgressPalDatabase
import com.progresspal.app.data.repository.UserRepository
import com.progresspal.app.data.repository.WeightRepository
import com.progresspal.app.databinding.FragmentSettingsBinding
import com.progresspal.app.domain.contracts.SettingsContract
import com.progresspal.app.domain.models.User
import com.progresspal.app.domain.models.MeasurementSystem
import com.progresspal.app.domain.models.MedicalGuidelines
import com.progresspal.app.domain.models.ActivityLevel
import com.progresspal.app.presentation.onboarding.OnboardingActivity
import com.progresspal.app.utils.DataBackupHelper
import com.progresspal.app.utils.DataImportHelper
import com.progresspal.app.utils.NotificationHelper
import com.progresspal.app.utils.WorkManagerHelper
import java.io.File

class SettingsFragment : Fragment(), SettingsContract.View {
    
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var presenter: SettingsContract.Presenter
    private lateinit var dataImportHelper: DataImportHelper
    private lateinit var dataBackupHelper: DataBackupHelper
    
    private val importLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { importDataFromCsv(it) }
    }
    
    private val restoreLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { restoreBackupFromFile(it) }
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupPresenter()
        setupViews()
        presenter.loadUserProfile()
    }
    
    private fun setupPresenter() {
        val database = ProgressPalDatabase.getDatabase(requireContext())
        val userRepository = UserRepository(database.userDao())
        val weightRepository = WeightRepository(database.weightDao())
        presenter = SettingsPresenter(userRepository, weightRepository)
        presenter.attachView(this)
        
        // Initialize helper classes
        dataImportHelper = DataImportHelper(requireContext(), weightRepository)
        dataBackupHelper = DataBackupHelper(requireContext(), userRepository, weightRepository)
        
        // Initialize notification channels
        NotificationHelper.createNotificationChannels(requireContext())
    }
    
    private fun setupViews() {
        // User Profile
        binding.btnEditProfile.setOnClickListener {
            presenter.onEditProfileClicked()
        }
        
        // Data Management
        binding.layoutExportData.setOnClickListener {
            presenter.onExportDataClicked()
        }
        
        binding.layoutImportData.setOnClickListener {
            importLauncher.launch("text/*")
        }
        
        binding.layoutCreateBackup.setOnClickListener {
            createBackup()
        }
        
        binding.layoutRestoreBackup.setOnClickListener {
            restoreLauncher.launch("application/json")
        }
        
        binding.layoutDeleteData.setOnClickListener {
            presenter.onDeleteAllDataClicked()
        }
        
        // Support & Info
        binding.layoutAbout.setOnClickListener {
            presenter.onAboutClicked()
        }
        
        binding.layoutPrivacy.setOnClickListener {
            presenter.onPrivacyPolicyClicked()
        }
        
        binding.layoutSupport.setOnClickListener {
            presenter.onSupportClicked()
        }
        
        // Check current reminder status and set switch accordingly
        binding.switchReminders.isChecked = WorkManagerHelper.isReminderScheduled(requireContext())
        
        // Set up reminder switch functionality
        binding.switchReminders.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Schedule daily reminder at 9:00 AM
                WorkManagerHelper.scheduleReminderNotification(requireContext(), 9, 0)
                showMessage("Daily reminders enabled at 9:00 AM")
            } else {
                WorkManagerHelper.cancelReminderNotification(requireContext())
                showMessage("Reminders disabled")
            }
        }
        
        // Set up measurement system RadioGroup
        binding.radioGroupMeasurementSystem.setOnCheckedChangeListener { _, checkedId ->
            val measurementSystem = when (checkedId) {
                binding.radioMetric.id -> MeasurementSystem.METRIC
                binding.radioImperial.id -> MeasurementSystem.IMPERIAL
                else -> MeasurementSystem.METRIC
            }
            presenter.onMeasurementSystemChanged(measurementSystem)
        }
        
        // Set up medical guidelines RadioGroup
        binding.radioGroupMedicalGuidelines.setOnCheckedChangeListener { _, checkedId ->
            val medicalGuidelines = when (checkedId) {
                binding.radioUsAha.id -> MedicalGuidelines.US_AHA
                binding.radioEuEsc.id -> MedicalGuidelines.EU_ESC
                else -> MedicalGuidelines.US_AHA
            }
            presenter.onMedicalGuidelinesChanged(medicalGuidelines)
        }
    }
    
    override fun showUser(user: User) {
        binding.tvUserName.text = user.name ?: "Guest User"
        
        val details = buildString {
            if (user.age != null) append("${user.age} years old • ")
            append("${user.height} cm • ${user.currentWeight} kg")
            if (user.targetWeight != null) {
                append(" → ${user.targetWeight} kg")
            }
        }
        binding.tvUserDetails.text = details
        
        // Set current measurement system selection
        when (user.measurementSystem) {
            "METRIC" -> binding.radioMetric.isChecked = true
            "IMPERIAL" -> binding.radioImperial.isChecked = true
            else -> binding.radioMetric.isChecked = true
        }
        
        // Set current medical guidelines selection
        when (user.medicalGuidelines) {
            "US_AHA" -> binding.radioUsAha.isChecked = true
            "EU_ESC" -> binding.radioEuEsc.isChecked = true
            else -> binding.radioUsAha.isChecked = true
        }
    }
    
    override fun showExportProgress() {
        binding.progressExport.visibility = View.VISIBLE
    }
    
    override fun hideExportProgress() {
        binding.progressExport.visibility = View.GONE
    }
    
    override fun showExportSuccess(file: File) {
        showMessage("Data exported successfully")
        
        // Offer to share the file
        AlertDialog.Builder(requireContext())
            .setTitle("Export Complete")
            .setMessage("Data exported to: ${file.name}\n\nWould you like to share or open the file?")
            .setPositiveButton("Share") { _, _ -> shareFile(file) }
            .setNegativeButton("OK", null)
            .show()
    }
    
    override fun showExportError(message: String) {
        showError(message)
    }
    
    override fun navigateToEditProfile() {
        showMessage("Profile editing functionality coming soon")
    }
    
    override fun showDeleteConfirmation() {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete All Data")
            .setMessage("Are you sure you want to permanently delete all your data? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                presenter.confirmDeleteAllData()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    override fun navigateToOnboarding() {
        val intent = Intent(requireContext(), OnboardingActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }
    
    override fun showAppInfo(version: String, buildDate: String) {
        binding.tvAppVersion.text = "ProgressPal v$version"
        
        AlertDialog.Builder(requireContext())
            .setTitle("About ProgressPal")
            .setMessage("Version: $version\nBuild Date: $buildDate\n\nProgressPal is your personal weight tracking companion, helping you monitor your fitness journey with detailed analytics and insights.")
            .setPositiveButton("OK", null)
            .show()
    }
    
    override fun showLoading() {
        // Could add a progress indicator
    }
    
    override fun hideLoading() {
        // Hide progress indicator
    }
    
    override fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }
    
    override fun showMessage(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
    
    override fun getContext(): Context? = super.getContext()
    
    private fun shareFile(file: File) {
        try {
            val uri = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.fileprovider",
                file
            )
            
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/*"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            
            startActivity(Intent.createChooser(shareIntent, "Share export file"))
        } catch (e: Exception) {
            showError("Could not share file: ${e.message}")
        }
    }
    
    private fun importDataFromCsv(uri: Uri) {
        lifecycleScope.launch {
            try {
                val database = ProgressPalDatabase.getDatabase(requireContext())
                val user = database.userDao().getUser()?.value
                
                if (user == null) {
                    showError("No user profile found. Please create a profile first.")
                    return@launch
                }
                
                binding.progressExport.visibility = View.VISIBLE
                
                when (val result = dataImportHelper.importWeightDataFromCsv(uri, user.id)) {
                    is DataImportHelper.ImportResult.Success -> {
                        binding.progressExport.visibility = View.GONE
                        showMessage("Import successful: ${result.successCount} entries imported" +
                                if (result.errorCount > 0) ", ${result.errorCount} errors" else "")
                    }
                    is DataImportHelper.ImportResult.Error -> {
                        binding.progressExport.visibility = View.GONE
                        showError("Import failed: ${result.message}")
                    }
                }
                
            } catch (e: Exception) {
                binding.progressExport.visibility = View.GONE
                showError("Import failed: ${e.message}")
            }
        }
    }
    
    private fun createBackup() {
        lifecycleScope.launch {
            try {
                binding.progressExport.visibility = View.VISIBLE
                
                when (val result = dataBackupHelper.createBackup()) {
                    is DataBackupHelper.BackupResult.Success -> {
                        binding.progressExport.visibility = View.GONE
                        showMessage("Backup created successfully")
                        
                        AlertDialog.Builder(requireContext())
                            .setTitle("Backup Complete")
                            .setMessage("Backup saved to: ${result.file.name}\n\nWould you like to share the backup file?")
                            .setPositiveButton("Share") { _, _ -> shareFile(result.file) }
                            .setNegativeButton("OK", null)
                            .show()
                    }
                    is DataBackupHelper.BackupResult.Error -> {
                        binding.progressExport.visibility = View.GONE
                        showError("Backup failed: ${result.message}")
                    }
                }
                
            } catch (e: Exception) {
                binding.progressExport.visibility = View.GONE
                showError("Backup failed: ${e.message}")
            }
        }
    }
    
    private fun restoreBackupFromFile(uri: Uri) {
        lifecycleScope.launch {
            try {
                val database = ProgressPalDatabase.getDatabase(requireContext())
                val user = database.userDao().getUser()?.value
                
                if (user == null) {
                    showError("No user profile found. Please create a profile first.")
                    return@launch
                }
                
                // Show confirmation dialog first
                AlertDialog.Builder(requireContext())
                    .setTitle("Restore Backup")
                    .setMessage("This will add weight entries from the backup to your current data. Are you sure you want to continue?")
                    .setPositiveButton("Restore") { _, _ ->
                        performRestore(uri, user.id)
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
                
            } catch (e: Exception) {
                showError("Restore preparation failed: ${e.message}")
            }
        }
    }
    
    private fun performRestore(uri: Uri, userId: Long) {
        lifecycleScope.launch {
            try {
                binding.progressExport.visibility = View.VISIBLE
                
                // Create temporary file from URI
                val tempFile = File(requireContext().cacheDir, "temp_backup.json")
                requireContext().contentResolver.openInputStream(uri)?.use { input ->
                    tempFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                
                when (val result = dataBackupHelper.restoreBackup(tempFile, userId)) {
                    is DataBackupHelper.RestoreResult.Success -> {
                        binding.progressExport.visibility = View.GONE
                        showMessage("Restore successful: ${result.restoredCount} weight entries restored")
                    }
                    is DataBackupHelper.RestoreResult.Error -> {
                        binding.progressExport.visibility = View.GONE
                        showError("Restore failed: ${result.message}")
                    }
                }
                
                // Clean up temp file
                tempFile.delete()
                
            } catch (e: Exception) {
                binding.progressExport.visibility = View.GONE
                showError("Restore failed: ${e.message}")
            }
        }
    }
    
    // Health Settings Display Methods
    override fun updateMeasurementSystemDisplay(system: MeasurementSystem) {
        // Update UI to show current measurement system
        // This can be implemented when the UI elements are added
    }
    
    override fun updateMedicalGuidelinesDisplay(guidelines: MedicalGuidelines) {
        // Update UI to show current medical guidelines
        // This can be implemented when the UI elements are added
    }
    
    override fun updateActivityLevelDisplay(level: ActivityLevel) {
        // Update UI to show current activity level
        // This can be implemented when the UI elements are added
    }
    
    override fun updateBirthDateDisplay(birthDate: java.util.Date?) {
        // Update UI to show current birth date
        // This can be implemented when the UI elements are added
    }
    
    // Health Settings Selection Dialog Methods
    override fun showMeasurementSystemDialog(current: MeasurementSystem) {
        // Show dialog to select measurement system
        // This can be implemented when needed
    }
    
    override fun showMedicalGuidelinesDialog(current: MedicalGuidelines) {
        // Show dialog to select medical guidelines
        // This can be implemented when needed
    }
    
    override fun showActivityLevelDialog(current: ActivityLevel) {
        // Show dialog to select activity level
        // This can be implemented when needed
    }
    
    override fun showBirthDatePicker(current: java.util.Date?) {
        // Show date picker for birth date
        // This can be implemented when needed
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        presenter.detachView()
        _binding = null
    }
}