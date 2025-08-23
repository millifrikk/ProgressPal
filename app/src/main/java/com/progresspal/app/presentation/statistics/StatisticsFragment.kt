package com.progresspal.app.presentation.statistics

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.progresspal.app.R
import com.progresspal.app.data.database.ProgressPalDatabase
import com.progresspal.app.data.repository.UserRepository
import com.progresspal.app.data.repository.WeightRepository
import com.progresspal.app.data.repository.BloodPressureRepository
import com.progresspal.app.databinding.FragmentStatisticsBinding
import com.progresspal.app.domain.contracts.StatisticsContract
import com.progresspal.app.domain.models.User
import com.progresspal.app.domain.models.Weight
import com.progresspal.app.presentation.dashboard.WeightChartView

class StatisticsFragment : Fragment(), StatisticsContract.View {
    
    private var _binding: FragmentStatisticsBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var presenter: StatisticsContract.Presenter
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatisticsBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupPresenter()
        setupViews()
        presenter.loadStatistics()
    }
    
    private fun setupPresenter() {
        val database = ProgressPalDatabase.getDatabase(requireContext())
        val userRepository = UserRepository(database.userDao())
        val weightRepository = WeightRepository(database.weightDao())
        val bloodPressureRepository = BloodPressureRepository(database.bloodPressureDao())
        presenter = StatisticsPresenter(userRepository, weightRepository, bloodPressureRepository)
        presenter.attachView(this)
    }
    
    private fun setupViews() {
        binding.swipeRefresh.setOnRefreshListener {
            presenter.onRefresh()
        }
        
        binding.btnExportData.setOnClickListener {
            presenter.exportData()
        }
        
        // Setup time range filter chips
        binding.chipGroupTimeRange.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                val selectedRange = when (checkedIds.first()) {
                    R.id.chip_week -> "Last 7 Days"
                    R.id.chip_month -> "Last 30 Days"
                    R.id.chip_3months -> "Last 3 Months"
                    R.id.chip_year -> "Last Year"
                    R.id.chip_all -> "All Time"
                    else -> "All Time"
                }
                presenter.onTimeRangeSelected(selectedRange)
            }
        }
    }
    
    override fun showUser(user: User) {
        binding.tvWelcomeUser.text = if (!user.name.isNullOrBlank()) {
            "Statistics for ${user.name}"
        } else {
            "Your Progress Statistics"
        }
        
        // Show UI elements when user data is available
        binding.layoutContent.visibility = View.VISIBLE
        binding.layoutEmptyState.visibility = View.GONE
        binding.chipGroupTimeRange.visibility = View.VISIBLE
        binding.btnExportData.visibility = View.VISIBLE
    }
    
    override fun showWeightStatistics(
        totalEntries: Int,
        averageWeight: Float,
        weightLoss: Float,
        weightGain: Float,
        averageWeeklyChange: Float
    ) {
        binding.tvTotalEntries.text = totalEntries.toString()
        binding.tvAverageWeight.text = String.format("%.1f kg", averageWeight)
        binding.tvTotalWeightLoss.text = String.format("%.1f kg", weightLoss)
        binding.tvTotalWeightGain.text = String.format("%.1f kg", weightGain)
        binding.tvWeeklyChange.text = String.format("%+.1f kg/week", averageWeeklyChange)
        
        // Set color for weekly change
        binding.tvWeeklyChange.setTextColor(
            requireContext().getColor(
                if (averageWeeklyChange < 0) R.color.pal_success 
                else if (averageWeeklyChange > 0) R.color.pal_error 
                else R.color.pal_text_primary
            )
        )
    }
    
    override fun showProgressStatistics(
        startWeight: Float,
        currentWeight: Float,
        goalWeight: Float?,
        progressPercentage: Float,
        daysActive: Int,
        estimatedDaysToGoal: Int?
    ) {
        binding.tvStartWeight.text = String.format("%.1f kg", startWeight)
        binding.tvCurrentWeightStats.text = String.format("%.1f kg", currentWeight)
        
        if (goalWeight != null) {
            binding.tvGoalWeightStats.text = String.format("%.1f kg", goalWeight)
            binding.tvProgressPercentage.text = String.format("%.1f%%", progressPercentage)
            binding.goalSection.visibility = View.VISIBLE
        } else {
            binding.goalSection.visibility = View.GONE
        }
        
        binding.tvDaysActive.text = "$daysActive days"
        
        if (estimatedDaysToGoal != null) {
            binding.tvEstimatedDays.text = "$estimatedDaysToGoal days"
            binding.estimatedSection.visibility = View.VISIBLE
        } else {
            binding.estimatedSection.visibility = View.GONE
        }
    }
    
    override fun showTrendAnalysis(
        currentTrend: String,
        weeklyTrend: String,
        monthlyTrend: String,
        longestStreak: Int,
        currentStreak: Int
    ) {
        binding.tvCurrentTrend.text = currentTrend
        binding.tvWeeklyTrend.text = weeklyTrend
        binding.tvMonthlyTrend.text = monthlyTrend
        binding.tvLongestStreak.text = "$longestStreak days"
        binding.tvCurrentStreak.text = "$currentStreak days"
        
        // Set colors for trends
        setTrendColor(binding.tvCurrentTrend, currentTrend)
        setTrendColor(binding.tvWeeklyTrend, weeklyTrend)
        setTrendColor(binding.tvMonthlyTrend, monthlyTrend)
    }
    
    private fun setTrendColor(textView: android.widget.TextView, trend: String) {
        val color = when (trend) {
            "Decreasing" -> R.color.pal_success
            "Increasing" -> R.color.pal_error
            "Stable" -> R.color.pal_warning
            else -> R.color.pal_text_secondary
        }
        textView.setTextColor(requireContext().getColor(color))
    }
    
    override fun showBMIAnalysis(
        currentBMI: Float,
        bmiCategory: String,
        bmiChange: Float,
        targetBMI: Float?
    ) {
        binding.tvCurrentBMI.text = String.format("%.1f", currentBMI)
        binding.tvBMICategory.text = bmiCategory
        binding.tvBMIChange.text = String.format("%+.1f", bmiChange)
        
        if (targetBMI != null) {
            binding.tvTargetBMI.text = String.format("%.1f", targetBMI)
            binding.targetBMISection.visibility = View.VISIBLE
        } else {
            binding.targetBMISection.visibility = View.GONE
        }
        
        // Set color for BMI category
        val bmiColor = when (bmiCategory.lowercase()) {
            "normal" -> R.color.pal_success
            "underweight" -> R.color.pal_warning
            "overweight", "obese" -> R.color.pal_error
            else -> R.color.pal_text_primary
        }
        binding.tvBMICategory.setTextColor(requireContext().getColor(bmiColor))
        
        // Set color for BMI change
        binding.tvBMIChange.setTextColor(
            requireContext().getColor(
                if (bmiChange < 0) R.color.pal_success 
                else if (bmiChange > 0) R.color.pal_error 
                else R.color.pal_text_primary
            )
        )
    }
    
    override fun showWeightChart(data: List<Weight>) {
        if (data.size >= 2) {
            binding.weightChartStats.visibility = View.VISIBLE
            binding.tvChartEmptyStats.visibility = View.GONE
            binding.weightChartStats.updateChart(data)
        } else {
            binding.weightChartStats.visibility = View.GONE
            binding.tvChartEmptyStats.visibility = View.VISIBLE
        }
    }
    
    override fun showBMIChart(data: List<Pair<Weight, Float>>) {
        // TODO: Implement BMI chart visualization
        // For now, just show a placeholder
        binding.bmiChartContainer.visibility = if (data.size >= 2) View.VISIBLE else View.GONE
    }
    
    override fun showBloodPressureStatistics(
        totalReadings: Int,
        averageSystolic: Float,
        averageDiastolic: Float,
        averagePulse: Float,
        trend: String,
        highReadings: Int,
        categoryBreakdown: String?
    ) {
        // Show the blood pressure statistics card
        binding.cardBloodPressureStats.visibility = View.VISIBLE
        
        // Update UI elements with blood pressure data
        binding.tvBpTotalReadings.text = totalReadings.toString()
        binding.tvBpAvgSystolic.text = String.format("%.0f mmHg", averageSystolic)
        binding.tvBpAvgDiastolic.text = String.format("%.0f mmHg", averageDiastolic)
        binding.tvBpAvgPulse.text = String.format("%.0f bpm", averagePulse)
        binding.tvBpTrend.text = trend
        binding.tvBpHighReadings.text = highReadings.toString()
        
        // Set trend color
        setTrendColor(binding.tvBpTrend, trend)
        
        // Show category breakdown if available
        if (!categoryBreakdown.isNullOrBlank()) {
            binding.tvBpCategories.text = categoryBreakdown
            binding.tvBpCategories.visibility = View.VISIBLE
        } else {
            binding.tvBpCategories.visibility = View.GONE
        }
        
        // Set high readings color
        binding.tvBpHighReadings.setTextColor(
            requireContext().getColor(
                if (highReadings > 0) R.color.pal_error else R.color.pal_success
            )
        )
    }
    
    override fun showEmptyState() {
        binding.layoutContent.visibility = View.GONE
        binding.layoutEmptyState.visibility = View.VISIBLE
        
        // Hide time range filters and export button when no user data exists
        binding.chipGroupTimeRange.visibility = View.GONE
        binding.btnExportData.visibility = View.GONE
        
        // Hide blood pressure card when no data
        binding.cardBloodPressureStats.visibility = View.GONE
    }
    
    override fun showLoading() {
        binding.swipeRefresh.isRefreshing = true
    }
    
    override fun hideLoading() {
        binding.swipeRefresh.isRefreshing = false
    }
    
    override fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }
    
    override fun showMessage(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        presenter.detachView()
        _binding = null
    }
}