package com.progresspal.app.domain.premium

import android.content.Context
import android.content.SharedPreferences
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.TimeUnit

/**
 * Manager for premium features and subscription status
 * Handles feature flags, subscription validation, and premium content access
 */
class PremiumManager(context: Context) {
    
    private val preferences: SharedPreferences = context.getSharedPreferences(
        "premium_preferences", 
        Context.MODE_PRIVATE
    )
    
    // Premium status flow for reactive UI updates
    private val _premiumStatus = MutableStateFlow(loadPremiumStatus())
    val premiumStatus: StateFlow<PremiumStatus> = _premiumStatus.asStateFlow()
    
    // Feature availability flow
    private val _featureFlags = MutableStateFlow(loadFeatureFlags())
    val featureFlags: StateFlow<Map<PremiumFeature, Boolean>> = _featureFlags.asStateFlow()
    
    companion object {
        private const val KEY_IS_PREMIUM = "is_premium"
        private const val KEY_SUBSCRIPTION_TYPE = "subscription_type"
        private const val KEY_SUBSCRIPTION_END_DATE = "subscription_end_date"
        private const val KEY_TRIAL_USED = "trial_used"
        private const val KEY_TRIAL_END_DATE = "trial_end_date"
        
        // Feature flag keys
        private const val KEY_ADVANCED_ANALYTICS = "feature_advanced_analytics"
        private const val KEY_UNLIMITED_PHOTOS = "feature_unlimited_photos"
        private const val KEY_EXPORT_DATA = "feature_export_data"
        private const val KEY_CUSTOM_GOALS = "feature_custom_goals"
        private const val KEY_PLATEAU_ALERTS = "feature_plateau_alerts"
        private const val KEY_PROGRESS_PREDICTIONS = "feature_progress_predictions"
        
        // Free tier limits
        const val FREE_PHOTO_LIMIT = 10
        const val FREE_EXPORT_LIMIT = 1 // per month
        const val FREE_INSIGHTS_LIMIT = 3 // basic insights only
    }
    
    /**
     * Check if user has access to a specific premium feature
     */
    fun hasFeatureAccess(feature: PremiumFeature): Boolean {
        val currentStatus = _premiumStatus.value
        
        return when (feature) {
            PremiumFeature.ADVANCED_ANALYTICS -> {
                currentStatus.isPremium || currentStatus.isInTrial
            }
            PremiumFeature.UNLIMITED_PHOTOS -> {
                currentStatus.isPremium || currentStatus.isInTrial
            }
            PremiumFeature.EXPORT_DATA -> {
                currentStatus.isPremium || currentStatus.isInTrial
            }
            PremiumFeature.CUSTOM_GOALS -> {
                currentStatus.isPremium || currentStatus.isInTrial
            }
            PremiumFeature.PLATEAU_ALERTS -> {
                currentStatus.isPremium || currentStatus.isInTrial
            }
            PremiumFeature.PROGRESS_PREDICTIONS -> {
                currentStatus.isPremium || currentStatus.isInTrial
            }
            PremiumFeature.PHOTO_COMPARISON -> {
                // Always available as it's part of core functionality
                true
            }
            PremiumFeature.BASIC_INSIGHTS -> {
                // Always available
                true
            }
        }
    }
    
    /**
     * Get remaining usage for limited features
     */
    fun getRemainingUsage(feature: PremiumFeature): Int {
        if (hasFeatureAccess(feature)) {
            return Int.MAX_VALUE // Unlimited for premium users
        }
        
        return when (feature) {
            PremiumFeature.UNLIMITED_PHOTOS -> {
                val usedPhotos = preferences.getInt("used_photos_count", 0)
                maxOf(0, FREE_PHOTO_LIMIT - usedPhotos)
            }
            PremiumFeature.EXPORT_DATA -> {
                val usedExports = preferences.getInt("used_exports_month", 0)
                maxOf(0, FREE_EXPORT_LIMIT - usedExports)
            }
            else -> 0
        }
    }
    
    /**
     * Track feature usage for free tier limits
     * Fixed: Added overdue reset check and validation
     */
    fun trackFeatureUsage(feature: PremiumFeature) {
        // Check for overdue monthly reset first
        checkAndResetIfOverdue()
        
        if (hasFeatureAccess(feature)) {
            return // No tracking needed for premium users
        }
        
        when (feature) {
            PremiumFeature.UNLIMITED_PHOTOS -> {
                val currentCount = preferences.getInt("used_photos_count", 0)
                val newCount = minOf(currentCount + 1, FREE_PHOTO_LIMIT + 10) // Prevent overflow
                preferences.edit().putInt("used_photos_count", newCount).apply()
            }
            PremiumFeature.EXPORT_DATA -> {
                val currentCount = preferences.getInt("used_exports_month", 0)
                val newCount = minOf(currentCount + 1, FREE_EXPORT_LIMIT + 5) // Prevent overflow
                preferences.edit().putInt("used_exports_month", newCount).apply()
            }
            else -> { /* No tracking needed */ }
        }
    }
    
    /**
     * Start free trial for user
     */
    fun startFreeTrial(): Boolean {
        if (preferences.getBoolean(KEY_TRIAL_USED, false)) {
            return false // Trial already used
        }
        
        val trialEndDate = System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000L) // 7 days
        
        preferences.edit()
            .putBoolean(KEY_TRIAL_USED, true)
            .putLong(KEY_TRIAL_END_DATE, trialEndDate)
            .apply()
        
        updatePremiumStatus()
        return true
    }
    
    /**
     * Simulate premium subscription purchase
     * In a real app, this would integrate with Google Play Billing
     */
    fun activatePremiumSubscription(subscriptionType: SubscriptionType) {
        val endDate = when (subscriptionType) {
            SubscriptionType.MONTHLY -> System.currentTimeMillis() + (30 * 24 * 60 * 60 * 1000L)
            SubscriptionType.YEARLY -> System.currentTimeMillis() + (365 * 24 * 60 * 60 * 1000L)
            SubscriptionType.LIFETIME -> Long.MAX_VALUE
        }
        
        preferences.edit()
            .putBoolean(KEY_IS_PREMIUM, true)
            .putString(KEY_SUBSCRIPTION_TYPE, subscriptionType.name)
            .putLong(KEY_SUBSCRIPTION_END_DATE, endDate)
            .apply()
        
        updatePremiumStatus()
        updateFeatureFlags()
    }
    
    /**
     * Check if user should see premium prompts
     */
    fun shouldShowPremiumPrompt(feature: PremiumFeature): Boolean {
        if (hasFeatureAccess(feature)) {
            return false
        }
        
        // Show prompt when user hits limits or tries to access premium features
        return when (feature) {
            PremiumFeature.UNLIMITED_PHOTOS -> {
                getRemainingUsage(feature) <= 2 // Show when close to limit
            }
            PremiumFeature.EXPORT_DATA -> {
                getRemainingUsage(feature) == 0
            }
            else -> true
        }
    }
    
    /**
     * Get premium benefits for display in upgrade screen
     */
    fun getPremiumBenefits(): List<PremiumBenefit> {
        return listOf(
            PremiumBenefit(
                icon = "📊",
                title = "Advanced Analytics",
                description = "Detailed progress patterns and plateau analysis"
            ),
            PremiumBenefit(
                icon = "📷",
                title = "Unlimited Photos",
                description = "Store unlimited progress photos and comparisons"
            ),
            PremiumBenefit(
                icon = "📤",
                title = "Data Export",
                description = "Export your data in multiple formats"
            ),
            PremiumBenefit(
                icon = "🎯",
                title = "Custom Goals",
                description = "Set personalized weight and timeline goals"
            ),
            PremiumBenefit(
                icon = "⚠️",
                title = "Plateau Alerts",
                description = "Get notified when progress stalls with actionable tips"
            ),
            PremiumBenefit(
                icon = "🔮",
                title = "Progress Predictions",
                description = "AI-powered predictions of your future progress"
            )
        )
    }
    
    /**
     * Get subscription pricing tiers
     */
    fun getSubscriptionTiers(): List<SubscriptionTier> {
        return listOf(
            SubscriptionTier(
                type = SubscriptionType.MONTHLY,
                price = "$4.99",
                billingPeriod = "month",
                savings = null,
                isPopular = false
            ),
            SubscriptionTier(
                type = SubscriptionType.YEARLY,
                price = "$39.99",
                billingPeriod = "year",
                savings = "Save 33%",
                isPopular = true
            ),
            SubscriptionTier(
                type = SubscriptionType.LIFETIME,
                price = "$99.99",
                billingPeriod = "one-time",
                savings = "Best Value",
                isPopular = false
            )
        )
    }
    
    private fun loadPremiumStatus(): PremiumStatus {
        val isPremium = preferences.getBoolean(KEY_IS_PREMIUM, false)
        val subscriptionType = try {
            preferences.getString(KEY_SUBSCRIPTION_TYPE, null)
                ?.let { SubscriptionType.valueOf(it) }
        } catch (e: IllegalArgumentException) {
            // Handle invalid subscription type gracefully
            null
        }
        val subscriptionEndDate = preferences.getLong(KEY_SUBSCRIPTION_END_DATE, 0)
        val isTrialUsed = preferences.getBoolean(KEY_TRIAL_USED, false)
        val trialEndDate = preferences.getLong(KEY_TRIAL_END_DATE, 0)
        
        // Fix: Handle lifetime subscriptions separately to avoid overflow issues
        val isLifetime = subscriptionType == SubscriptionType.LIFETIME
        val isInTrial = isTrialUsed && 
            trialEndDate > 0 && 
            System.currentTimeMillis() < trialEndDate
        
        val isSubscriptionActive = when {
            !isPremium -> false
            isLifetime -> true // Lifetime subscriptions never expire
            subscriptionEndDate <= 0 -> false // Invalid end date
            subscriptionEndDate == Long.MAX_VALUE -> true // Handle edge case
            else -> System.currentTimeMillis() < subscriptionEndDate
        }
        
        return PremiumStatus(
            isPremium = isSubscriptionActive,
            isInTrial = isInTrial,
            subscriptionType = subscriptionType,
            subscriptionEndDate = if (subscriptionEndDate > 0 && !isLifetime) subscriptionEndDate else null,
            trialEndDate = if (trialEndDate > 0) trialEndDate else null,
            canStartTrial = !isTrialUsed
        )
    }
    
    private fun loadFeatureFlags(): Map<PremiumFeature, Boolean> {
        // Fix: Always get fresh status to avoid race conditions
        val status = loadPremiumStatus()
        
        return mapOf(
            PremiumFeature.ADVANCED_ANALYTICS to (status.isPremium || status.isInTrial),
            PremiumFeature.UNLIMITED_PHOTOS to (status.isPremium || status.isInTrial),
            PremiumFeature.EXPORT_DATA to (status.isPremium || status.isInTrial),
            PremiumFeature.CUSTOM_GOALS to (status.isPremium || status.isInTrial),
            PremiumFeature.PLATEAU_ALERTS to (status.isPremium || status.isInTrial),
            PremiumFeature.PROGRESS_PREDICTIONS to (status.isPremium || status.isInTrial),
            PremiumFeature.PHOTO_COMPARISON to true,
            PremiumFeature.BASIC_INSIGHTS to true
        )
    }
    
    private fun updatePremiumStatus() {
        _premiumStatus.value = loadPremiumStatus()
    }
    
    private fun updateFeatureFlags() {
        _featureFlags.value = loadFeatureFlags()
    }
    
    /**
     * Validate subscription integrity and fix inconsistencies
     */
    fun validateSubscriptionIntegrity(): Boolean {
        val currentStatus = loadPremiumStatus()
        val storedIsPremium = preferences.getBoolean(KEY_IS_PREMIUM, false)
        
        // Check for inconsistencies and log them
        if (storedIsPremium && !currentStatus.isPremium && !currentStatus.isInTrial) {
            // Premium flag set but no active subscription - possible corruption
            preferences.edit().putBoolean(KEY_IS_PREMIUM, false).apply()
            updatePremiumStatus()
            updateFeatureFlags()
            return false
        }
        
        return true
    }
    
    /**
     * Reset monthly usage counters (automatically scheduled)
     */
    fun resetMonthlyUsage() {
        preferences.edit()
            .putInt("used_exports_month", 0)
            .putLong("last_monthly_reset", System.currentTimeMillis())
            .apply()
    }
    
    /**
     * Initialize automatic monthly usage reset with WorkManager
     */
    fun initializeAutomaticReset(context: Context) {
        val workRequest = PeriodicWorkRequestBuilder<MonthlyUsageResetWorker>(30, TimeUnit.DAYS)
            .build()
        
        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                "monthly_usage_reset",
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
    }
    
    /**
     * Check if monthly reset is overdue and reset if needed
     */
    private fun checkAndResetIfOverdue() {
        val lastReset = preferences.getLong("last_monthly_reset", 0)
        val currentTime = System.currentTimeMillis()
        val thirtyDaysInMillis = 30L * 24 * 60 * 60 * 1000
        
        if (lastReset == 0L || currentTime - lastReset > thirtyDaysInMillis) {
            resetMonthlyUsage()
        }
    }
    
}

/**
 * WorkManager worker for automatic monthly usage reset
 */
class MonthlyUsageResetWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    override fun doWork(): Result {
        return try {
            val premiumManager = PremiumManager(applicationContext)
            premiumManager.resetMonthlyUsage()
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}