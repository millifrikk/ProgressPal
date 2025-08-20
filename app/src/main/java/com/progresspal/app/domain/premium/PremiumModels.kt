package com.progresspal.app.domain.premium

/**
 * Data classes and enums for the premium feature system
 */

/**
 * Represents the current premium status of a user
 */
data class PremiumStatus(
    val isPremium: Boolean,
    val isInTrial: Boolean,
    val subscriptionType: SubscriptionType?,
    val subscriptionEndDate: Long?, // timestamp
    val trialEndDate: Long?, // timestamp
    val canStartTrial: Boolean
) {
    /**
     * Check if user has any form of premium access
     */
    fun hasAnyPremiumAccess(): Boolean = isPremium || isInTrial
    
    /**
     * Get user-friendly status description
     */
    fun getStatusDescription(): String {
        return when {
            isPremium -> when (subscriptionType) {
                SubscriptionType.LIFETIME -> "Premium (Lifetime)"
                SubscriptionType.YEARLY -> "Premium (Annual)"
                SubscriptionType.MONTHLY -> "Premium (Monthly)"
                null -> "Premium"
            }
            isInTrial -> "Free Trial Active"
            canStartTrial -> "Free Trial Available"
            else -> "Free Plan"
        }
    }
    
    /**
     * Get days remaining for current premium access
     */
    fun getDaysRemaining(): Int? {
        val endDate = when {
            isPremium -> subscriptionEndDate
            isInTrial -> trialEndDate
            else -> null
        } ?: return null
        
        val now = System.currentTimeMillis()
        val daysRemaining = ((endDate - now) / (24 * 60 * 60 * 1000)).toInt()
        return maxOf(0, daysRemaining)
    }
}

/**
 * Available premium features in the app
 */
enum class PremiumFeature {
    ADVANCED_ANALYTICS,    // Detailed insights and analytics
    UNLIMITED_PHOTOS,      // Store unlimited progress photos
    EXPORT_DATA,          // Export weight data to CSV/PDF
    CUSTOM_GOALS,         // Set custom weight and timeline goals
    PLATEAU_ALERTS,       // Get notified about plateaus
    PROGRESS_PREDICTIONS, // AI-powered progress predictions
    PHOTO_COMPARISON,     // Before/after photo comparisons (free)
    BASIC_INSIGHTS;       // Basic progress insights (free)
    
    /**
     * Get user-friendly name for the feature
     */
    fun getDisplayName(): String {
        return when (this) {
            ADVANCED_ANALYTICS -> "Advanced Analytics"
            UNLIMITED_PHOTOS -> "Unlimited Photos"
            EXPORT_DATA -> "Data Export"
            CUSTOM_GOALS -> "Custom Goals"
            PLATEAU_ALERTS -> "Plateau Alerts"
            PROGRESS_PREDICTIONS -> "Progress Predictions"
            PHOTO_COMPARISON -> "Photo Comparison"
            BASIC_INSIGHTS -> "Basic Insights"
        }
    }
    
    /**
     * Get description of what the feature provides
     */
    fun getDescription(): String {
        return when (this) {
            ADVANCED_ANALYTICS -> "Detailed pattern analysis, trend detection, and plateau identification"
            UNLIMITED_PHOTOS -> "Store unlimited progress photos with advanced comparison tools"
            EXPORT_DATA -> "Export your weight data in CSV, PDF, and other formats"
            CUSTOM_GOALS -> "Set personalized weight targets and timeline goals"
            PLATEAU_ALERTS -> "Get notified when progress stalls with actionable breakthrough strategies"
            PROGRESS_PREDICTIONS -> "AI-powered predictions of your future weight loss progress"
            PHOTO_COMPARISON -> "Compare before and after photos to visualize your transformation"
            BASIC_INSIGHTS -> "Basic progress tracking with simple analytics"
        }
    }
    
    /**
     * Check if this is a core/free feature
     */
    fun isFreeFeature(): Boolean {
        return this == PHOTO_COMPARISON || this == BASIC_INSIGHTS
    }
}

/**
 * Subscription types available
 */
enum class SubscriptionType {
    MONTHLY,
    YEARLY,
    LIFETIME;
    
    fun getDisplayName(): String {
        return when (this) {
            MONTHLY -> "Monthly"
            YEARLY -> "Annual"
            LIFETIME -> "Lifetime"
        }
    }
}

/**
 * Represents a premium benefit for display in upgrade screens
 */
data class PremiumBenefit(
    val icon: String,
    val title: String,
    val description: String
)

/**
 * Represents a subscription pricing tier
 */
data class SubscriptionTier(
    val type: SubscriptionType,
    val price: String,
    val billingPeriod: String,
    val savings: String?, // e.g., "Save 33%"
    val isPopular: Boolean = false
) {
    /**
     * Get formatted price description
     */
    fun getPriceDescription(): String {
        return "$price / $billingPeriod"
    }
    
    /**
     * Get monthly equivalent price for comparison
     */
    fun getMonthlyEquivalent(): String {
        return when (type) {
            SubscriptionType.MONTHLY -> price
            SubscriptionType.YEARLY -> {
                // Calculate monthly equivalent for yearly subscription
                val yearlyPrice = price.replace("$", "").toFloatOrNull() ?: 0f
                val monthlyEquivalent = yearlyPrice / 12
                "$%.2f".format(monthlyEquivalent)
            }
            SubscriptionType.LIFETIME -> "One-time"
        }
    }
}

/**
 * Reasons why a premium feature is blocked
 */
enum class FeatureBlockReason {
    NOT_PREMIUM,          // User is not premium
    TRIAL_EXPIRED,        // Free trial has expired
    USAGE_LIMIT_REACHED,  // Hit free tier usage limit
    SUBSCRIPTION_EXPIRED; // Premium subscription has expired
    
    fun getMessage(feature: PremiumFeature): String {
        return when (this) {
            NOT_PREMIUM -> "Upgrade to Premium to unlock ${feature.getDisplayName()}"
            TRIAL_EXPIRED -> "Your free trial has expired. Upgrade to continue using ${feature.getDisplayName()}"
            USAGE_LIMIT_REACHED -> "You've reached the free limit for ${feature.getDisplayName()}. Upgrade for unlimited access"
            SUBSCRIPTION_EXPIRED -> "Your Premium subscription has expired. Renew to continue using ${feature.getDisplayName()}"
        }
    }
}

/**
 * Result of attempting to access a premium feature
 */
sealed class FeatureAccessResult {
    object Granted : FeatureAccessResult()
    
    data class Blocked(
        val reason: FeatureBlockReason,
        val remainingUsage: Int = 0,
        val canStartTrial: Boolean = false
    ) : FeatureAccessResult()
    
    data class LimitWarning(
        val remainingUsage: Int,
        val totalLimit: Int
    ) : FeatureAccessResult()
}