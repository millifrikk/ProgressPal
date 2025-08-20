package com.progresspal.app.presentation.premium

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.progresspal.app.databinding.ActivityPremiumUpgradeBinding
import com.progresspal.app.domain.premium.*
import com.progresspal.app.presentation.premium.adapters.BenefitsAdapter
import com.progresspal.app.presentation.premium.adapters.SubscriptionTiersAdapter
import kotlinx.coroutines.launch

/**
 * Activity for premium upgrade flow
 * Shows benefits, pricing tiers, and handles subscription purchase
 */
class PremiumUpgradeActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityPremiumUpgradeBinding
    private lateinit var premiumManager: PremiumManager
    private lateinit var premiumHelper: PremiumHelper
    private lateinit var benefitsAdapter: BenefitsAdapter
    private lateinit var subscriptionTiersAdapter: SubscriptionTiersAdapter
    
    private var selectedTier: SubscriptionTier? = null
    private var triggerFeature: PremiumFeature? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPremiumUpgradeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupPremiumManager()
        getTriggerFeature()
        setupToolbar()
        setupRecyclerViews()
        setupButtons()
        loadContent()
    }
    
    private fun setupPremiumManager() {
        premiumManager = PremiumManager(this)
        premiumHelper = PremiumHelper(premiumManager)
    }
    
    private fun getTriggerFeature() {
        val featureName = intent.getStringExtra(EXTRA_TRIGGER_FEATURE)
        triggerFeature = featureName?.let { 
            try {
                PremiumFeature.valueOf(it)
            } catch (e: IllegalArgumentException) {
                null
            }
        }
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "Upgrade to Premium"
        }
    }
    
    private fun setupRecyclerViews() {
        // Benefits RecyclerView
        benefitsAdapter = BenefitsAdapter()
        binding.recyclerViewBenefits.apply {
            layoutManager = LinearLayoutManager(this@PremiumUpgradeActivity)
            adapter = benefitsAdapter
            setHasFixedSize(true)
        }
        
        // Subscription Tiers RecyclerView
        subscriptionTiersAdapter = SubscriptionTiersAdapter { tier ->
            selectSubscriptionTier(tier)
        }
        binding.recyclerViewTiers.apply {
            layoutManager = LinearLayoutManager(this@PremiumUpgradeActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = subscriptionTiersAdapter
            setHasFixedSize(true)
        }
    }
    
    private fun setupButtons() {
        binding.btnStartTrial.setOnClickListener {
            startFreeTrial()
        }
        
        binding.btnUpgrade.setOnClickListener {
            purchasePremium()
        }
        
        binding.btnRestorePurchases.setOnClickListener {
            restorePurchases()
        }
    }
    
    private fun loadContent() {
        lifecycleScope.launch {
            val status = premiumManager.premiumStatus.value
            
            // Load personalized content
            setupHeader(status)
            loadBenefits()
            loadSubscriptionTiers()
            updateUI(status)
        }
    }
    
    private fun setupHeader(status: PremiumStatus) {
        binding.apply {
            // Customize header based on trigger feature
            if (triggerFeature != null) {
                tvHeaderTitle.text = "Unlock ${triggerFeature!!.getDisplayName()}"
                tvHeaderSubtitle.text = triggerFeature!!.getDescription()
            } else {
                tvHeaderTitle.text = "Unlock Your Full Potential"
                tvHeaderSubtitle.text = "Get advanced insights and unlimited features to accelerate your progress"
            }
            
            // Show personalized value proposition
            val valueProps = premiumHelper.getPersonalizedValueProposition()
            if (valueProps.isNotEmpty()) {
                tvValueProposition.text = valueProps.joinToString("\n")
            }
            
            // Show trial availability
            btnStartTrial.isEnabled = status.canStartTrial
            if (!status.canStartTrial) {
                btnStartTrial.text = "Trial Already Used"
            }
        }
    }
    
    private fun loadBenefits() {
        val benefits = premiumManager.getPremiumBenefits()
        benefitsAdapter.submitList(benefits)
    }
    
    private fun loadSubscriptionTiers() {
        val tiers = premiumManager.getSubscriptionTiers()
        subscriptionTiersAdapter.submitList(tiers)
        
        // Pre-select recommended tier
        val recommendedTier = premiumHelper.getRecommendedTier()
        val recommended = tiers.find { it.type == recommendedTier }
        recommended?.let { selectSubscriptionTier(it) }
    }
    
    private fun selectSubscriptionTier(tier: SubscriptionTier) {
        selectedTier = tier
        subscriptionTiersAdapter.setSelectedTier(tier)
        
        binding.apply {
            btnUpgrade.isEnabled = true
            btnUpgrade.text = "Upgrade for ${tier.price}"
            
            // Show savings for annual/lifetime
            if (tier.savings != null) {
                tvSavings.text = tier.savings
                tvSavings.visibility = android.view.View.VISIBLE
            } else {
                tvSavings.visibility = android.view.View.GONE
            }
        }
    }
    
    private fun updateUI(status: PremiumStatus) {
        binding.apply {
            when {
                status.isPremium -> {
                    // User already has premium
                    tvHeaderTitle.text = "You're Premium!"
                    tvHeaderSubtitle.text = "Thanks for supporting ProgressPal"
                    btnUpgrade.text = "Manage Subscription"
                    btnStartTrial.visibility = android.view.View.GONE
                }
                status.isInTrial -> {
                    // User is in trial
                    val daysRemaining = status.getDaysRemaining() ?: 0
                    tvHeaderTitle.text = "Trial Active"
                    tvHeaderSubtitle.text = "$daysRemaining days remaining in your free trial"
                    btnStartTrial.visibility = android.view.View.GONE
                }
                else -> {
                    // Regular upgrade flow
                    btnUpgrade.isEnabled = selectedTier != null
                }
            }
        }
    }
    
    private fun startFreeTrial() {
        val success = premiumManager.startFreeTrial()
        
        if (success) {
            showSuccess("Free trial started! Enjoy 7 days of premium features.")
            finishWithResult(RESULT_TRIAL_STARTED)
        } else {
            showError("Unable to start trial. You may have already used your free trial.")
        }
    }
    
    private fun purchasePremium() {
        val tier = selectedTier
        if (tier == null) {
            showError("Please select a subscription plan")
            return
        }
        
        // In a real app, this would integrate with Google Play Billing
        // For demo purposes, we'll simulate the purchase
        simulatePurchase(tier)
    }
    
    private fun simulatePurchase(tier: SubscriptionTier) {
        binding.apply {
            btnUpgrade.isEnabled = false
            btnUpgrade.text = "Processing..."
        }
        
        // Simulate network delay
        lifecycleScope.launch {
            kotlinx.coroutines.delay(2000)
            
            // Simulate successful purchase
            premiumManager.activatePremiumSubscription(tier.type)
            
            showSuccess("Welcome to Premium! Your subscription is now active.")
            finishWithResult(RESULT_PREMIUM_PURCHASED)
        }
    }
    
    private fun restorePurchases() {
        // In a real app, this would query Google Play Billing for existing purchases
        showInfo("No previous purchases found")
    }
    
    private fun showSuccess(message: String) {
        // Show success message (could use Snackbar or Dialog)
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_LONG).show()
    }
    
    private fun showError(message: String) {
        // Show error message
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_LONG).show()
    }
    
    private fun showInfo(message: String) {
        // Show info message
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show()
    }
    
    private fun finishWithResult(resultCode: Int) {
        setResult(resultCode)
        finish()
    }
    
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
    
    companion object {
        private const val EXTRA_TRIGGER_FEATURE = "trigger_feature"
        
        const val RESULT_TRIAL_STARTED = 100
        const val RESULT_PREMIUM_PURCHASED = 101
        
        fun createIntent(context: Context, triggerFeature: PremiumFeature? = null): Intent {
            return Intent(context, PremiumUpgradeActivity::class.java).apply {
                triggerFeature?.let { 
                    putExtra(EXTRA_TRIGGER_FEATURE, it.name)
                }
            }
        }
    }
}