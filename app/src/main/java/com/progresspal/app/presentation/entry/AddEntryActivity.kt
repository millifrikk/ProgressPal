package com.progresspal.app.presentation.entry

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.progresspal.app.data.database.ProgressPalDatabase
import com.progresspal.app.data.repository.PhotoRepository
import com.progresspal.app.data.repository.UserRepository
import com.progresspal.app.data.repository.WeightRepository
import com.progresspal.app.databinding.ActivityAddEntryBinding
import com.progresspal.app.domain.contracts.AddEntryContract
import com.progresspal.app.utils.DateUtils
import com.progresspal.app.utils.PhotoCaptureHelper
import java.io.File
import java.util.*

class AddEntryActivity : AppCompatActivity(), AddEntryContract.View {
    
    private lateinit var binding: ActivityAddEntryBinding
    private lateinit var presenter: AddEntryPresenter
    private var selectedDate: Date = Date()
    private lateinit var photoCaptureHelper: PhotoCaptureHelper
    private var capturedPhotoFile: File? = null
    
    // Activity result launchers
    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            capturedPhotoFile?.let { file ->
                displayPhoto(file)
            }
        }
    }
    
    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.data?.let { uri ->
                photoCaptureHelper.copyGalleryImageToFile(uri,
                    onPhotoReady = { file ->
                        capturedPhotoFile = file
                        displayPhoto(file)
                    },
                    onError = { error ->
                        showError(error)
                    }
                )
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEntryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupPresenter()
        setupToolbar()
        setupViews()
        setupPhotoCapture()
        
        presenter.attachView(this)
    }
    
    override fun onDestroy() {
        presenter.detachView()
        super.onDestroy()
    }
    
    private fun setupPresenter() {
        val database = ProgressPalDatabase.getDatabase(this)
        val userRepository = UserRepository(database.userDao())
        val weightRepository = WeightRepository(database.weightDao())
        val photoRepository = PhotoRepository(database.photoDao())
        presenter = AddEntryPresenter(userRepository, weightRepository, photoRepository)
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Add Entry"
    }
    
    private fun setupViews() {
        binding.btnSave.setOnClickListener {
            val weight = binding.etWeight.text.toString().trim()
            val notes = binding.etNotes.text.toString().trim()
            val photoPath = capturedPhotoFile?.absolutePath
            presenter.saveEntry(weight, selectedDate, photoPath, notes)
        }
        
        binding.btnCancel.setOnClickListener {
            finish()
        }
        
        binding.etDate.setOnClickListener {
            presenter.onDateClicked(selectedDate)
        }
        
        // Photo capture buttons
        binding.btnCamera.setOnClickListener {
            capturePhoto()
        }
        
        binding.btnGallery.setOnClickListener {
            selectFromGallery()
        }
        
        binding.btnRemovePhoto.setOnClickListener {
            removePhoto()
        }
    }
    
    private fun setupPhotoCapture() {
        photoCaptureHelper = PhotoCaptureHelper(this)
        setCurrentDate()
    }
    
    private fun capturePhoto() {
        photoCaptureHelper.capturePhoto(
            cameraLauncher = cameraLauncher,
            onPhotoReady = { file ->
                capturedPhotoFile = file
            },
            onError = { error ->
                showError(error)
            }
        )
    }
    
    private fun selectFromGallery() {
        photoCaptureHelper.selectFromGallery(
            galleryLauncher = galleryLauncher,
            onPhotoReady = { file ->
                capturedPhotoFile = file
            },
            onError = { error ->
                showError(error)
            }
        )
    }
    
    private fun displayPhoto(file: File) {
        Glide.with(this)
            .load(file)
            .centerCrop()
            .into(binding.ivProgressPhoto)
        
        binding.ivProgressPhoto.visibility = View.VISIBLE
        binding.btnRemovePhoto.visibility = View.VISIBLE
    }
    
    private fun removePhoto() {
        capturedPhotoFile?.delete()
        capturedPhotoFile = null
        
        binding.ivProgressPhoto.visibility = View.GONE
        binding.btnRemovePhoto.visibility = View.GONE
        binding.ivProgressPhoto.setImageDrawable(null)
    }
    
    // AddEntryContract.View implementation
    override fun showLoading() {
        binding.btnSave.isEnabled = false
        // Could add a progress indicator here
    }
    
    override fun hideLoading() {
        binding.btnSave.isEnabled = true
    }
    
    override fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
    
    override fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
    
    override fun showWeightValidationError(message: String) {
        binding.tilWeight.error = message
    }
    
    override fun showDateValidationError(message: String) {
        binding.tilDate.error = message
    }
    
    override fun clearValidationErrors() {
        binding.tilWeight.error = null
        binding.tilDate.error = null
    }
    
    override fun showEntrySavedSuccess(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
    
    override fun navigateBack() {
        finish()
    }
    
    override fun setCurrentDate() {
        selectedDate = Date()
        binding.etDate.setText(DateUtils.formatDisplayDate(selectedDate))
    }
    
    override fun showDatePicker(currentDate: Date) {
        val calendar = Calendar.getInstance()
        calendar.time = currentDate
        
        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val newCalendar = Calendar.getInstance()
                newCalendar.set(year, month, dayOfMonth)
                selectedDate = newCalendar.time
                binding.etDate.setText(DateUtils.formatDisplayDate(selectedDate))
                clearValidationErrors()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        
        // Don't allow future dates
        datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
        datePickerDialog.show()
    }
    
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}