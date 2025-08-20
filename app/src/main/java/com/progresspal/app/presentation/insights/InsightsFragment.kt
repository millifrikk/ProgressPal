package com.progresspal.app.presentation.insights

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.progresspal.app.data.database.ProgressPalDatabase
import com.progresspal.app.data.repository.UserRepository
import com.progresspal.app.data.repository.WeightRepository
import com.progresspal.app.databinding.FragmentInsightsBinding
import com.progresspal.app.domain.insights.InsightsCalculator
import com.progresspal.app.domain.insights.PlateauIdentifier
import com.progresspal.app.domain.insights.models.InsightResult
import com.progresspal.app.domain.insights.models.PlateauResult
import com.progresspal.app.presentation.insights.adapters.InsightCardsAdapter
import com.progresspal.app.presentation.insights.models.InsightCard
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Fragment that displays comprehensive insights and analytics
 * Shows patterns, trends, plateaus, and personalized recommendations
 * FIXED: Replaced sync database calls with proper suspend functions
 */
class InsightsFragment : Fragment() {
    
    private var _binding: FragmentInsightsBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var userRepository: UserRepository
    private lateinit var weightRepository: WeightRepository
    private lateinit var insightsCalculator: InsightsCalculator
    private lateinit var plateauIdentifier: PlateauIdentifier
    private lateinit var insightCardsAdapter: InsightCardsAdapter
    
    // Job for cancelling long-running operations
    private var insightLoadingJob: Job? = null
    
    // Cache for insights to prevent unnecessary recalculations
    data class CachedInsights(
        val cards: List<InsightCard>,
        val timestamp: Long,
        val userHash: Int
    ) {
        fun isExpired(maxAge: Long = 2 * 60 * 1000): Boolean { // 2 minutes
            return System.currentTimeMillis() - timestamp > maxAge
        }
    }
    
    private var cachedInsights: CachedInsights? = null
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInsightsBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRepositories()
        setupRecyclerView()
        setupRefreshListener()
        loadInsights()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        // Cancel any ongoing insight loading to prevent memory leaks
        insightLoadingJob?.cancel()
        insightLoadingJob = null
        _binding = null
    }
    
    private fun setupRepositories() {
        val database = ProgressPalDatabase.getDatabase(requireContext())
        userRepository = UserRepository(database.userDao())
        weightRepository = WeightRepository(database.weightDao())
        insightsCalculator = InsightsCalculator()
        plateauIdentifier = PlateauIdentifier()
    }
    
    private fun setupRecyclerView() {
        insightCardsAdapter = InsightCardsAdapter { card ->
            // Handle insight card clicks
            handleInsightCardClick(card)
        }
        
        binding.recyclerViewInsights.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = insightCardsAdapter
            setHasFixedSize(false)
        }
    }
    
    private fun setupRefreshListener() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            loadInsights()
        }
    }
    
    private fun loadInsights() {
        // Cancel any existing loading job
        insightLoadingJob?.cancel()
        
        showLoading()
        
        insightLoadingJob = viewLifecycleOwner.lifecycleScope.launch {
            try {
                // Get user data using proper suspend function
                val user = withContext(Dispatchers.IO) {
                    userRepository.getUser() // Fixed: Use suspend function instead of sync
                }
                
                if (user == null) {
                    withContext(Dispatchers.Main) {
                        showError("User not found. Please complete onboarding first.")
                    }
                    return@launch
                }
                
                // Check cache first to prevent unnecessary recalculation
                val userHash = user.hashCode()
                cachedInsights?.let { cached ->
                    if (!cached.isExpired() && cached.userHash == userHash) {
                        withContext(Dispatchers.Main) {
                            showInsights(cached.cards)
                        }
                        return@launch
                    }
                }
                
                // Get weight data using proper suspend function
                val weightEntries = withContext(Dispatchers.IO) {
                    weightRepository.getAllWeights(user.id) // Fixed: Use suspend function
                }
                
                if (weightEntries.isEmpty()) {
                    withContext(Dispatchers.Main) {
                        showEmptyState()
                    }
                    return@launch
                }
                
                // Check if coroutine is still active before heavy computation
                ensureActive()
                
                // Generate insights on background thread with progress checks
                val (insights, plateauAnalysis) = withContext(Dispatchers.Default) {
                    ensureActive() // Check cancellation before starting
                    
                    val insights = insightsCalculator.generateInsights(weightEntries)
                    ensureActive() // Check cancellation between operations
                    
                    val plateauAnalysis = plateauIdentifier.analyzePlateaus(weightEntries)
                    ensureActive() // Final check before returning
                    
                    Pair(insights, plateauAnalysis)
                }
                
                // Create insight cards
                val insightCards = createInsightCards(insights, plateauAnalysis)
                
                // Cache the results
                cachedInsights = CachedInsights(
                    cards = insightCards,
                    timestamp = System.currentTimeMillis(),
                    userHash = userHash
                )
                
                // Update UI on main thread
                withContext(Dispatchers.Main) {
                    if (isActive) { // Final check before UI update
                        showInsights(insightCards)
                    }
                }
                
            } catch (e: CancellationException) {
                // Handle cancellation gracefully - don't show error
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showError("Failed to load insights: ${e.message}")
                }
            }
        }
    }
    
    private fun createInsightCards(
        insights: InsightResult,
        plateauAnalysis: PlateauResult
    ): List<InsightCard> {
        val cards = mutableListOf<InsightCard>()
        
        // Progress Summary Card
        cards.add(
            InsightCard.ProgressSummary(
                title = "Progress Overview",
                summary = insights.getProgressSummary(),
                trend = insights.recentTrend,
                totalChange = insights.totalWeightChange,
                motivationMessage = insights.getMotivationMessage()
            )
        )
        
        // Current Streak Card
        if (insights.currentStreak > 0) {
            cards.add(
                InsightCard.Streak(
                    title = "Current Streak",
                    streakDays = insights.currentStreak,
                    streakType = if (insights.recentTrend.isPositive()) "weight loss" else "tracking",
                    encouragement = "Keep the momentum going!"
                )
            )
        }
        
        // Plateau Analysis Card
        if (!plateauAnalysis.hasInsufficientData) {
            cards.add(
                InsightCard.PlateauAnalysis(
                    title = "Plateau Analysis",
                    status = plateauAnalysis.getPlateauStatusMessage(),
                    severity = plateauAnalysis.plateauSeverity,
                    primaryAction = plateauAnalysis.getPriorityAction(),
                    encouragement = plateauAnalysis.getEncouragementMessage()
                )
            )
        }
        
        // Best Progress Card
        if (insights.bestWeekProgress > 0) {
            cards.add(
                InsightCard.BestProgress(
                    title = "Best Week",
                    progressAmount = insights.bestWeekProgress,
                    description = "Your best weekly progress so far!",
                    tip = "Try to identify what made this week successful"
                )
            )
        }
        
        // Patterns Card
        if (insights.patterns.isNotEmpty()) {
            cards.add(
                InsightCard.Patterns(
                    title = "Detected Patterns",
                    patterns = insights.patterns,
                    description = "Patterns in your weight data"
                )
            )
        }
        
        // Predictions Card
        if (insights.predictions.isNotEmpty()) {
            cards.add(
                InsightCard.Predictions(
                    title = "Progress Predictions",
                    predictions = insights.predictions,
                    disclaimer = "Predictions based on current trends"
                )
            )
        }
        
        // Tips Card
        if (insights.tips.isNotEmpty()) {
            cards.add(
                InsightCard.Tips(
                    title = "Personalized Tips",
                    tips = insights.tips,
                    category = "General"
                )
            )
        }
        
        // Plateau Strategies Card
        if (plateauAnalysis.isInPlateau && plateauAnalysis.breakoutStrategies.isNotEmpty()) {
            cards.add(
                InsightCard.PlateauStrategies(
                    title = "Plateau Breakthrough",
                    strategies = plateauAnalysis.breakoutStrategies,
                    timeframe = plateauAnalysis.expectedBreakoutTimeframe
                )
            )
        }
        
        // Milestones Card
        if (insights.milestones.isNotEmpty()) {
            cards.add(
                InsightCard.Milestones(
                    title = "Achievements",
                    milestones = insights.milestones,
                    celebration = "Celebrate your progress!"
                )
            )
        }
        
        return cards
    }
    
    private fun handleInsightCardClick(card: InsightCard) {
        // Handle different card types
        when (card) {
            is InsightCard.ProgressSummary -> {
                // Navigate to detailed progress view
            }
            is InsightCard.PlateauAnalysis -> {
                // Show plateau details or strategies
            }
            is InsightCard.Tips -> {
                // Show expanded tips or educational content
            }
            // Add more card type handlers as needed
            else -> {
                // Default action
            }
        }
    }
    
    private fun showLoading() {
        binding.apply {
            recyclerViewInsights.visibility = View.GONE
            layoutEmpty.visibility = View.GONE
            layoutError.visibility = View.GONE
            swipeRefreshLayout.isRefreshing = true
        }
    }
    
    private fun showInsights(cards: List<InsightCard>) {
        binding.apply {
            recyclerViewInsights.visibility = View.VISIBLE
            layoutEmpty.visibility = View.GONE
            layoutError.visibility = View.GONE
            swipeRefreshLayout.isRefreshing = false
        }
        
        insightCardsAdapter.submitList(cards)
    }
    
    private fun showEmptyState() {
        binding.apply {
            recyclerViewInsights.visibility = View.GONE
            layoutEmpty.visibility = View.VISIBLE
            layoutError.visibility = View.GONE
            swipeRefreshLayout.isRefreshing = false
        }
    }
    
    private fun showError(message: String) {
        binding.apply {
            recyclerViewInsights.visibility = View.GONE
            layoutEmpty.visibility = View.GONE
            layoutError.visibility = View.VISIBLE
            tvErrorMessage.text = message
            swipeRefreshLayout.isRefreshing = false
        }
    }
    
    companion object {
        fun newInstance(): InsightsFragment {
            return InsightsFragment()
        }
    }
}