package com.progresspal.app.presentation.bloodpressure

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.progresspal.app.data.database.ProgressPalDatabase
import com.progresspal.app.data.database.entities.BloodPressureEntity
import com.progresspal.app.data.repository.BloodPressureRepository
import com.progresspal.app.data.repository.UserRepository
import com.progresspal.app.databinding.ActivityBloodPressureHistoryBinding
import com.progresspal.app.presentation.bloodpressure.adapters.BloodPressureHistoryAdapter
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Activity for viewing blood pressure reading history
 * Displays a comprehensive list of all blood pressure measurements with filtering options
 */
class BloodPressureHistoryActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityBloodPressureHistoryBinding
    private lateinit var bloodPressureRepository: BloodPressureRepository
    private lateinit var userRepository: UserRepository
    private lateinit var adapter: BloodPressureHistoryAdapter
    
    private var currentUserId: Long? = null
    private var allReadings = listOf<BloodPressureEntity>()
    private var filteredReadings = listOf<BloodPressureEntity>()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBloodPressureHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupDatabase()
        setupToolbar()
        setupRecyclerView()
        setupViews()
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
    
    private fun setupRecyclerView() {
        adapter = BloodPressureHistoryAdapter(
            onItemClick = { reading ->
                // TODO: Navigate to edit reading
            },
            onOptionsClick = { reading ->
                // TODO: Show options menu (edit, delete)
            }
        )
        
        binding.rvBloodPressureHistory.layoutManager = LinearLayoutManager(this)
        binding.rvBloodPressureHistory.adapter = adapter
    }
    
    private fun setupViews() {
        // Swipe refresh
        binding.swipeRefresh.setOnRefreshListener {
            loadBloodPressureData()
        }
        
        // FAB for adding new reading
        binding.fabAddReading.setOnClickListener {
            navigateToAddBloodPressure()
        }
        
        // Empty state button
        binding.btnAddFirstReading.setOnClickListener {
            navigateToAddBloodPressure()
        }
        
        // Filter chips
        setupFilterChips()
    }
    
    private fun setupFilterChips() {
        binding.chipGroupFilter.setOnCheckedStateChangeListener { group, checkedIds ->
            when (checkedIds.firstOrNull()) {
                binding.chipAll.id -> filterReadings(FilterType.ALL)
                binding.chipWeek.id -> filterReadings(FilterType.WEEK)
                binding.chipMonth.id -> filterReadings(FilterType.MONTH)
                else -> filterReadings(FilterType.ALL)
            }
        }
        
        // Set default selection
        binding.chipAll.isChecked = true
    }
    
    private fun loadCurrentUser() {
        lifecycleScope.launch {
            try {
                val user = userRepository.getUser()
                if (user != null) {
                    currentUserId = user.id
                    loadBloodPressureData()
                } else {
                    // Handle no user case
                    finish()
                }
            } catch (e: Exception) {
                // Handle error
                finish()
            }
        }
    }
    
    private fun loadBloodPressureData() {
        val userId = currentUserId ?: return
        
        lifecycleScope.launch {
            try {
                binding.swipeRefresh.isRefreshing = true
                
                allReadings = bloodPressureRepository.getAllForUserSync(userId)
                filterReadings(getCurrentFilterType())
                updateSummaryStats()
                updateEmptyState()
                
            } catch (e: Exception) {
                // Handle error
            } finally {
                binding.swipeRefresh.isRefreshing = false
            }
        }
    }
    
    private fun filterReadings(filterType: FilterType) {
        val now = System.currentTimeMillis()
        
        filteredReadings = when (filterType) {
            FilterType.ALL -> allReadings
            FilterType.WEEK -> {
                val weekAgo = now - TimeUnit.DAYS.toMillis(7)
                allReadings.filter { it.timestamp >= weekAgo }
            }
            FilterType.MONTH -> {
                val monthAgo = now - TimeUnit.DAYS.toMillis(30)
                allReadings.filter { it.timestamp >= monthAgo }
            }
        }
        
        adapter.submitList(filteredReadings)
        updateSummaryStats()
        updateEmptyState()
    }
    
    private fun getCurrentFilterType(): FilterType {
        return when (binding.chipGroupFilter.checkedChipId) {
            binding.chipWeek.id -> FilterType.WEEK
            binding.chipMonth.id -> FilterType.MONTH
            else -> FilterType.ALL
        }
    }
    
    private fun updateSummaryStats() {
        if (filteredReadings.isEmpty()) {
            binding.tvTotalReadings.text = "0"
            binding.tvAvgReading.text = "--/--"
            binding.tvTrend.text = "--"
            return
        }
        
        // Total readings
        binding.tvTotalReadings.text = filteredReadings.size.toString()
        
        // Average reading
        val avgSystolic = filteredReadings.map { it.systolic }.average().toInt()
        val avgDiastolic = filteredReadings.map { it.diastolic }.average().toInt()
        binding.tvAvgReading.text = "$avgSystolic/$avgDiastolic"
        
        // Trend (simplified)
        lifecycleScope.launch {
            try {
                val userId = currentUserId ?: return@launch
                val trend = bloodPressureRepository.getBloodPressureTrend(userId)
                binding.tvTrend.text = trend
            } catch (e: Exception) {
                binding.tvTrend.text = "--"
            }
        }
    }
    
    private fun updateEmptyState() {
        val isEmpty = filteredReadings.isEmpty()
        binding.layoutEmptyState.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.rvBloodPressureHistory.visibility = if (isEmpty) View.GONE else View.VISIBLE
        binding.fabAddReading.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }
    
    private fun navigateToAddBloodPressure() {
        val intent = Intent(this, AddBloodPressureActivity::class.java)
        startActivityForResult(intent, REQUEST_CODE_ADD_BP)
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_ADD_BP && resultCode == RESULT_OK) {
            // Refresh data when returning from add activity
            loadBloodPressureData()
        }
    }
    
    enum class FilterType {
        ALL, WEEK, MONTH
    }
    
    companion object {
        private const val REQUEST_CODE_ADD_BP = 1001
    }
}