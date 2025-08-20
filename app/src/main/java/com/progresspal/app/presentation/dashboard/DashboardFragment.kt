package com.progresspal.app.presentation.dashboard

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.progresspal.app.databinding.FragmentDashboardBinding
import com.progresspal.app.domain.contracts.DashboardContract
import com.progresspal.app.domain.models.User
import com.progresspal.app.domain.models.Weight
import com.progresspal.app.presentation.entry.AddEntryActivity

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
    }
    
    private fun setupPresenter() {
        val database = com.progresspal.app.data.database.ProgressPalDatabase.getDatabase(requireContext())
        val userRepository = com.progresspal.app.data.repository.UserRepository(database.userDao())
        val weightRepository = com.progresspal.app.data.repository.WeightRepository(database.weightDao())
        presenter = DashboardPresenter(userRepository, weightRepository)
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
        binding.tvBmi.text = "${String.format("%.1f", bmi)} ($category)"
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