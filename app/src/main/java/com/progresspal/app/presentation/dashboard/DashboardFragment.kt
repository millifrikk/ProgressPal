package com.progresspal.app.presentation.dashboard

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.progresspal.app.data.database.entities.BloodPressureEntity
import com.progresspal.app.databinding.FragmentDashboardBinding
import com.progresspal.app.domain.contracts.DashboardContract
import com.progresspal.app.domain.models.User
import com.progresspal.app.domain.models.Weight
import com.progresspal.app.MainActivity
import com.progresspal.app.presentation.bloodpressure.AddBloodPressureActivity
import com.progresspal.app.presentation.entry.AddEntryActivity
import com.progresspal.app.presentation.dialogs.WaistMeasurementDialog
import com.progresspal.app.domain.models.ActivityLevel
import com.progresspal.app.domain.models.MeasurementSystem

class DashboardFragment : Fragment(), DashboardContract.View {
    
    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var presenter: DashboardContract.Presenter
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        setupPresenter()
        presenter.loadDashboardData()
    }
    
    private fun setupViews() {
        binding.fabAddEntry.setOnClickListener {
            presenter.onAddEntryClicked()
        }
        
        binding.swipeRefresh.setOnRefreshListener {
            presenter.onRefresh()
        }
        
        // Set up blood pressure card click listeners
        binding.bloodPressureCard.setOnAddMeasurementClickListener {
            presenter.onAddBloodPressureClicked()
        }
        
        binding.bloodPressureCard.setOnViewHistoryClickListener {
            presenter.onViewBloodPressureHistoryClicked()
        }
        
        binding.bloodPressureCard.setOnViewTrendsClickListener {
            presenter.onViewBloodPressureTrendsClicked()
        }
        
        // Set up body composition card click listener
        binding.bodyCompositionCard.setOnAddWaistClickListener {
            showWaistMeasurementDialog()
        }
    }
    
    private fun setupPresenter() {
        val database = com.progresspal.app.data.database.ProgressPalDatabase.getDatabase(requireContext())
        val userRepository = com.progresspal.app.data.repository.UserRepository(database.userDao())
        val weightRepository = com.progresspal.app.data.repository.WeightRepository(database.weightDao())
        val bloodPressureRepository = com.progresspal.app.data.repository.BloodPressureRepository(database.bloodPressureDao())
        presenter = DashboardPresenter(userRepository, weightRepository, bloodPressureRepository)
        presenter.attachView(this)
    }
    
    override fun showCurrentWeight(weight: Float) {
        binding.tvCurrentWeight.text = "${weight} kg"
    }
    
    override fun showGoalWeight(weight: Float?) {
        if (weight != null) {
            binding.tvGoalWeight.text = "${weight} kg"
            binding.layoutGoal.visibility = View.VISIBLE
        } else {
            binding.layoutGoal.visibility = View.GONE
        }
    }
    
    override fun showProgress(progress: Float) {
        binding.tvProgress.text = "${String.format("%.1f", progress)}%"
        binding.progressBar.progress = progress.toInt()
    }
    
    override fun showBMI(bmi: Float, category: String) {
        // Legacy method - now handled by showBodyComposition
    }
    
    override fun showBodyComposition(user: User, latestWeight: Weight) {
        // Get user settings for personalized assessment
        val activityLevel = when (user.activityLevel) {
            "SEDENTARY" -> ActivityLevel.SEDENTARY
            "ACTIVE" -> ActivityLevel.ACTIVE
            "ATHLETIC" -> ActivityLevel.ATHLETIC
            "ENDURANCE_ATHLETE" -> ActivityLevel.ENDURANCE_ATHLETE
            else -> ActivityLevel.ACTIVE
        }
        
        val measurementSystem = when (user.measurementSystem) {
            "IMPERIAL" -> MeasurementSystem.IMPERIAL
            else -> MeasurementSystem.METRIC
        }
        
        // Calculate age from birth date
        val age = user.birthDate?.let { birthDate ->
            val ageInMillis = System.currentTimeMillis() - birthDate.time
            (ageInMillis / (365.25 * 24 * 60 * 60 * 1000)).toInt()
        }
        
        // Update body composition card with comprehensive assessment
        binding.bodyCompositionCard.updateBodyComposition(
            weightKg = latestWeight.weight,
            heightCm = user.height,
            waistCm = user.waistCircumference,
            activityLevel = activityLevel,
            age = age,
            gender = user.gender,
            measurementSystem = measurementSystem
        )
    }
    
    private fun showWaistMeasurementDialog() {
        // Get user's measurement system preference
        val measurementSystem = MeasurementSystem.METRIC // TODO: Get from user settings
        
        val dialog = WaistMeasurementDialog.newInstance(measurementSystem)
        dialog.setOnMeasurementAddedListener { waistCm ->
            presenter.onWaistMeasurementAdded(waistCm)
        }
        dialog.show(parentFragmentManager, "WaistMeasurementDialog")
    }
    
    override fun showQuickStats(weightEntries: List<Weight>) {
        if (weightEntries.size >= 2) {
            val recent = weightEntries.first().weight
            val previous = weightEntries[1].weight
            val change = recent - previous
            val changeText = if (change > 0) "+${String.format("%.1f", change)} kg" 
                            else "${String.format("%.1f", change)} kg"
            binding.tvWeeklyChange.text = changeText
        } else {
            binding.tvWeeklyChange.text = "No previous data"
        }
    }
    
    override fun showWeightChart(data: List<Weight>) {
        if (data.isNotEmpty() && data.size >= 2) {
            binding.weightChart.visibility = View.VISIBLE
            binding.tvChartEmpty.visibility = View.GONE
            binding.weightChart.updateChart(data)
            binding.chartContainer.visibility = View.VISIBLE
        } else if (data.size == 1) {
            binding.weightChart.visibility = View.GONE
            binding.tvChartEmpty.visibility = View.VISIBLE
            binding.tvChartEmpty.text = "Add more entries to see your progress chart"
            binding.chartContainer.visibility = View.VISIBLE
        } else {
            binding.chartContainer.visibility = View.GONE
        }
    }
    
    override fun navigateToAddEntry() {
        val intent = Intent(requireContext(), AddEntryActivity::class.java)
        startActivity(intent)
    }
    
    override fun navigateToAddBloodPressure() {
        val intent = Intent(requireContext(), AddBloodPressureActivity::class.java)
        startActivity(intent)
    }
    
    override fun navigateToBloodPressureHistory() {
        val intent = Intent(requireContext(), com.progresspal.app.presentation.bloodpressure.BloodPressureHistoryActivity::class.java)
        startActivity(intent)
    }
    
    override fun navigateToBloodPressureTrends() {
        // Navigate to Statistics tab which contains blood pressure analytics
        (activity as? MainActivity)?.navigateToStatistics()
    }
    
    override fun showBloodPressureData(reading: BloodPressureEntity?, trend: String?) {
        binding.bloodPressureCard.updateWithTrend(reading, trend)
    }
    
    override fun showEmptyState() {
        binding.layoutContent.visibility = View.GONE
        binding.layoutEmptyState.visibility = View.VISIBLE
    }
    
    override fun showUser(user: User?) {
        if (user != null && !user.name.isNullOrBlank()) {
            binding.tvGreeting.text = "Hello, ${user.name}!"
        } else {
            binding.tvGreeting.text = "Hello!"
        }
    }
    
    override fun showLoading() {
        binding.swipeRefresh.isRefreshing = true
    }
    
    override fun hideLoading() {
        binding.swipeRefresh.isRefreshing = false
    }
    
    override fun showError(message: String) {
        // TODO: Show proper error message with Snackbar
        binding.tvError.text = message
        binding.tvError.visibility = View.VISIBLE
    }
    
    override fun showMessage(message: String) {
        // TODO: Show proper message with Snackbar or Toast
        android.widget.Toast.makeText(requireContext(), message, android.widget.Toast.LENGTH_SHORT).show()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        presenter.detachView()
        _binding = null
    }
}