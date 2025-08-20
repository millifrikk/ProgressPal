package com.progresspal.app.presentation.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.progresspal.app.data.database.ProgressPalDatabase
import com.progresspal.app.data.repository.UserRepository
import com.progresspal.app.data.repository.WeightRepository
import com.progresspal.app.databinding.FragmentHistoryBinding
import com.progresspal.app.domain.contracts.HistoryContract
import com.progresspal.app.domain.models.Weight
import com.progresspal.app.presentation.history.adapters.HistoryAdapter

class HistoryFragment : Fragment(), HistoryContract.View {
    
    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var presenter: HistoryPresenter
    private lateinit var historyAdapter: HistoryAdapter
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupPresenter()
        setupViews()
        presenter.loadWeightHistory()
    }
    
    private fun setupPresenter() {
        val database = ProgressPalDatabase.getDatabase(requireContext())
        val userRepository = UserRepository(database.userDao())
        val weightRepository = WeightRepository(database.weightDao())
        presenter = HistoryPresenter(userRepository, weightRepository)
        presenter.attachView(this)
    }
    
    private fun setupViews() {
        historyAdapter = HistoryAdapter(
            onItemClick = { weight -> presenter.onWeightClicked(weight) },
            onDeleteClick = { weight -> presenter.onDeleteClicked(weight) }
        )
        
        binding.recyclerView.apply {
            adapter = historyAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
        
        binding.swipeRefresh.setOnRefreshListener {
            presenter.onRefresh()
        }
    }
    
    // HistoryContract.View implementation
    override fun showWeightHistory(weights: List<Weight>) {
        binding.recyclerView.visibility = View.VISIBLE
        binding.layoutEmptyState.visibility = View.GONE
        historyAdapter.submitList(weights)
    }
    
    override fun showEmptyState() {
        binding.recyclerView.visibility = View.GONE
        binding.layoutEmptyState.visibility = View.VISIBLE
    }
    
    override fun showDeleteConfirmation(weight: Weight) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Entry")
            .setMessage("Are you sure you want to delete this weight entry from ${com.progresspal.app.utils.DateUtils.formatDisplayDate(weight.date)}?")
            .setPositiveButton("Delete") { _, _ ->
                presenter.onDeleteConfirmed(weight)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    override fun showEditDialog(weight: Weight) {
        // TODO: Implement edit dialog
        showMessage("Edit functionality coming soon!")
    }
    
    override fun navigateToEditEntry(weightId: Long) {
        // TODO: Navigate to edit entry screen
        showMessage("Edit functionality coming soon!")
    }
    
    override fun showLoading() {
        binding.swipeRefresh.isRefreshing = true
    }
    
    override fun hideLoading() {
        binding.swipeRefresh.isRefreshing = false
    }
    
    override fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
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