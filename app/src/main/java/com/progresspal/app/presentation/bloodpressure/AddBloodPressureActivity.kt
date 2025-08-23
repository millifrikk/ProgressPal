package com.progresspal.app.presentation.bloodpressure

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.progresspal.app.R
import com.progresspal.app.data.database.ProgressPalDatabase
import com.progresspal.app.data.database.entities.BloodPressureEntity
import com.progresspal.app.data.database.entities.BloodPressureCategory
import com.progresspal.app.data.repository.BloodPressureRepository
import com.progresspal.app.data.repository.UserRepository
import com.progresspal.app.databinding.ActivityAddBloodPressureBinding
import com.progresspal.app.utils.DateUtils
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.*

/**
 * Activity for adding new blood pressure measurements
 * Implements Material Design 3 patterns with comprehensive input validation
 * Provides real-time feedback on blood pressure categories
 */
class AddBloodPressureActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityAddBloodPressureBinding
    private lateinit var bloodPressureRepository: BloodPressureRepository
    private lateinit var userRepository: UserRepository
    
    private var selectedDateTime = Calendar.getInstance()
    private var selectedTimeOfDay = BloodPressureEntity.TIME_MORNING
    private val selectedTags = mutableSetOf<String>()
    
    private var currentUserId: Long? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddBloodPressureBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupDatabase()
        setupToolbar()
        setupViews()
        setupValidation()
        loadCurrentUser()
    }
    
    private fun setupDatabase() {
        val database = ProgressPalDatabase.getDatabase(this)
        bloodPressureRepository = BloodPressureRepository(database.bloodPressureDao())
        userRepository = UserRepository(database.userDao())
    }
    
    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }
    
    private fun setupViews() {
        setupTimeOfDayChips()
        setupTagChips()
        setupDateTimeDisplay()
        setupSaveButton()
        setupInputValidation()
    }
    
    private fun setupTimeOfDayChips() {
        binding.chipGroupTimeOfDay.setOnCheckedStateChangeListener { group, checkedIds ->
            when (checkedIds.firstOrNull()) {
                R.id.chipMorning -> selectedTimeOfDay = BloodPressureEntity.TIME_MORNING
                R.id.chipAfternoon -> selectedTimeOfDay = BloodPressureEntity.TIME_AFTERNOON
                R.id.chipEvening -> selectedTimeOfDay = BloodPressureEntity.TIME_EVENING
                else -> selectedTimeOfDay = BloodPressureEntity.TIME_MORNING
            }
        }
        
        // Set default selection
        binding.chipMorning.isChecked = true
    }
    
    private fun setupTagChips() {
        val tagChips = mapOf(
            binding.chipBeforeMeal to BloodPressureEntity.TAG_BEFORE_MEAL,
            binding.chipAfterMeal to BloodPressureEntity.TAG_AFTER_MEAL,
            binding.chipAfterExercise to BloodPressureEntity.TAG_AFTER_EXERCISE,
            binding.chipStressed to BloodPressureEntity.TAG_STRESSED,
            binding.chipRelaxed to BloodPressureEntity.TAG_RELAXED,
            binding.chipMedication to BloodPressureEntity.TAG_MEDICATION
        )
        
        tagChips.forEach { (chip, tag) ->
            chip.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    selectedTags.add(tag)
                } else {
                    selectedTags.remove(tag)
                }
            }
        }
    }
    
    private fun setupDateTimeDisplay() {
        updateDateTimeDisplay()
        
        binding.btnChangeDateTime.setOnClickListener {
            showDateTimePicker()
        }
    }
    
    private fun updateDateTimeDisplay() {
        val dateFormat = SimpleDateFormat("EEEE, MMM d", Locale.getDefault())
        val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
        
        val dateStr = dateFormat.format(selectedDateTime.time)
        val timeStr = timeFormat.format(selectedDateTime.time)
        
        binding.tvDateTime.text = "$dateStr at $timeStr"
    }
    
    private fun showDateTimePicker() {
        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                selectedDateTime.set(Calendar.YEAR, year)
                selectedDateTime.set(Calendar.MONTH, month)
                selectedDateTime.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                
                // After date is selected, show time picker
                val timePickerDialog = TimePickerDialog(
                    this,
                    { _, hourOfDay, minute ->
                        selectedDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay)
                        selectedDateTime.set(Calendar.MINUTE, minute)
                        updateDateTimeDisplay()
                    },
                    selectedDateTime.get(Calendar.HOUR_OF_DAY),
                    selectedDateTime.get(Calendar.MINUTE),
                    false // 12-hour format
                )
                timePickerDialog.show()
            },
            selectedDateTime.get(Calendar.YEAR),
            selectedDateTime.get(Calendar.MONTH),
            selectedDateTime.get(Calendar.DAY_OF_MONTH)
        )
        
        // Set max date to today
        datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
        datePickerDialog.show()
    }
    
    private fun setupSaveButton() {
        binding.btnSave.setOnClickListener {
            attemptSave()
        }
    }
    
    private fun setupInputValidation() {
        // Real-time validation for blood pressure inputs
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                validateInputsAndUpdateUI()
            }
        }
        
        binding.etSystolic.addTextChangedListener(textWatcher)
        binding.etDiastolic.addTextChangedListener(textWatcher)
        binding.etPulse.addTextChangedListener(textWatcher)
    }
    
    private fun validateInputsAndUpdateUI() {
        val systolicText = binding.etSystolic.text?.toString()
        val diastolicText = binding.etDiastolic.text?.toString()
        val pulseText = binding.etPulse.text?.toString()
        
        // Clear previous error states
        binding.tilSystolic.error = null
        binding.tilDiastolic.error = null
        binding.tilPulse.error = null
        
        var isValid = true
        
        // Validate systolic
        val systolic = systolicText?.toIntOrNull()
        if (systolic == null && !systolicText.isNullOrEmpty()) {
            binding.tilSystolic.error = "Please enter a valid number"
            isValid = false
        } else if (systolic != null && (systolic < 70 || systolic > 250)) {
            binding.tilSystolic.error = "Systolic should be between 70-250 mmHg"
            isValid = false
        }
        
        // Validate diastolic
        val diastolic = diastolicText?.toIntOrNull()
        if (diastolic == null && !diastolicText.isNullOrEmpty()) {
            binding.tilDiastolic.error = "Please enter a valid number"
            isValid = false
        } else if (diastolic != null && (diastolic < 40 || diastolic > 150)) {
            binding.tilDiastolic.error = "Diastolic should be between 40-150 mmHg"
            isValid = false
        }
        
        // Validate pulse
        val pulse = pulseText?.toIntOrNull()
        if (pulse == null && !pulseText.isNullOrEmpty()) {
            binding.tilPulse.error = "Please enter a valid number"
            isValid = false
        } else if (pulse != null && (pulse < 30 || pulse > 220)) {
            binding.tilPulse.error = "Pulse should be between 30-220 bpm"
            isValid = false
        }
        
        // Cross-validation: diastolic should be lower than systolic
        if (systolic != null && diastolic != null && diastolic >= systolic) {
            binding.tilDiastolic.error = "Diastolic must be lower than systolic"
            isValid = false
        }
        
        // Update save button state
        binding.btnSave.isEnabled = isValid && 
                systolic != null && diastolic != null && pulse != null
        
        // Show blood pressure category if valid
        if (systolic != null && diastolic != null) {
            showBloodPressureCategory(systolic, diastolic)
        }
    }
    
    private fun showBloodPressureCategory(systolic: Int, diastolic: Int) {
        val tempEntity = BloodPressureEntity(
            userId = 0L, 
            systolic = systolic, 
            diastolic = diastolic, 
            pulse = 70,
            timeOfDay = selectedTimeOfDay
        )
        val category = tempEntity.getCategory()
        
        // Update systolic field colors based on category
        val colorRes = when (category) {
            BloodPressureCategory.OPTIMAL -> R.color.green_500
            BloodPressureCategory.NORMAL -> R.color.green_400
            BloodPressureCategory.ELEVATED -> R.color.yellow_500
            BloodPressureCategory.STAGE_1 -> R.color.orange_500
            BloodPressureCategory.STAGE_2 -> R.color.deep_orange_500
            BloodPressureCategory.CRISIS -> R.color.red_500
        }
        
        val color = ContextCompat.getColor(this, colorRes)
        binding.tilSystolic.boxStrokeColor = color
        binding.tilDiastolic.boxStrokeColor = color
        
        // Show helper text with category
        binding.tilSystolic.helperText = "${category.displayName} range"
    }
    
    private fun setupValidation() {
        // Enable/disable save button based on required fields
        validateInputsAndUpdateUI()
    }
    
    private fun loadCurrentUser() {
        lifecycleScope.launch {
            try {
                val user = userRepository.getUser()
                if (user != null) {
                    currentUserId = user.id
                } else {
                    showError("No user found. Please complete app setup first.")
                    finish()
                }
            } catch (e: Exception) {
                showError("Error loading user data: ${e.message}")
                finish()
            }
        }
    }
    
    private fun attemptSave() {
        val systolic = binding.etSystolic.text?.toString()?.toIntOrNull()
        val diastolic = binding.etDiastolic.text?.toString()?.toIntOrNull()
        val pulse = binding.etPulse.text?.toString()?.toIntOrNull()
        val notes = binding.etNotes.text?.toString()?.trim()
        
        if (systolic == null || diastolic == null || pulse == null) {
            showError("Please fill in all required fields")
            return
        }
        
        if (currentUserId == null) {
            showError("User not found. Please try again.")
            return
        }
        
        // Show confirmation for high blood pressure readings
        val tempEntity = BloodPressureEntity(
            userId = currentUserId!!,
            systolic = systolic,
            diastolic = diastolic,
            pulse = pulse,
            timeOfDay = selectedTimeOfDay
        )
        
        if (tempEntity.requiresAttention()) {
            showHighBloodPressureWarning(tempEntity)
        } else {
            saveMeasurement(tempEntity, notes)
        }
    }
    
    private fun showHighBloodPressureWarning(entity: BloodPressureEntity) {
        val category = entity.getCategory()
        val message = when (category) {
            BloodPressureCategory.CRISIS -> 
                "This reading indicates a hypertensive crisis. Please seek immediate medical attention."
            BloodPressureCategory.STAGE_2 -> 
                "This reading indicates Stage 2 Hypertension. Consider consulting your healthcare provider."
            else -> 
                "This reading is elevated. Consider discussing with your healthcare provider."
        }
        
        MaterialAlertDialogBuilder(this)
            .setTitle("High Blood Pressure Detected")
            .setMessage("$message\n\nDo you want to save this measurement?")
            .setPositiveButton("Save") { _, _ ->
                saveMeasurement(entity, binding.etNotes.text?.toString()?.trim())
            }
            .setNegativeButton("Review", null)
            .setIcon(R.drawable.ic_warning)
            .show()
    }
    
    private fun saveMeasurement(entity: BloodPressureEntity, notes: String?) {
        binding.btnSave.isEnabled = false
        binding.btnSave.text = "Saving..."
        
        lifecycleScope.launch {
            try {
                val bloodPressureEntity = entity.copy(
                    timestamp = selectedDateTime.timeInMillis,
                    tags = if (selectedTags.isNotEmpty()) {
                        JSONArray(selectedTags.toList()).toString()
                    } else null,
                    notes = if (notes.isNullOrEmpty()) null else notes
                )
                
                val id = bloodPressureRepository.insert(bloodPressureEntity)
                
                if (id > 0) {
                    Toast.makeText(
                        this@AddBloodPressureActivity,
                        "Blood pressure measurement saved successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                    
                    setResult(RESULT_OK)
                    finish()
                } else {
                    showError("Failed to save measurement. Please try again.")
                }
                
            } catch (e: Exception) {
                showError("Error saving measurement: ${e.message}")
            } finally {
                binding.btnSave.isEnabled = true
                binding.btnSave.text = "Save Measurement"
            }
        }
    }
    
    private fun showError(message: String) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Error")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .setIcon(R.drawable.ic_error)
            .show()
    }
    
    companion object {
        const val REQUEST_CODE_ADD_BP = 1001
    }
}