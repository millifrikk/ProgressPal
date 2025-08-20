package com.progresspal.app.domain.premium

import android.content.Context

/**
 * Helper class for premium feature access checks and user experience flows
 * Provides convenient methods for checking features and handling upgrade flows
 */
class PremiumHelper(private val premiumManager: PremiumManager) {
    
    /**
     * Check feature access and return detailed result
     */
    fun checkFeatureAccess(feature: PremiumFeature): FeatureAccessResult {
        val hasAccess = premiumManager.hasFeatureAccess(feature)
        
        if (hasAccess) {
            return FeatureAccessResult.Granted
        }
        
        val remainingUsage = premiumManager.getRemainingUsage(feature)
        val status = premiumManager.premiumStatus.value
        
        // Determine why access is blocked
        val reason = when {
            !status.canStartTrial && !status.isPremium && !status.isInTrial -> {
                FeatureBlockReason.NOT_PREMIUM
            }
            status.isInTrial && status.getDaysRemaining() == 0 -> {
                FeatureBlockReason.TRIAL_EXPIRED
            }
            status.isPremium && status.getDaysRemaining() == 0 -> {
                FeatureBlockReason.SUBSCRIPTION_EXPIRED
            }
            remainingUsage == 0 -> {
                FeatureBlockReason.USAGE_LIMIT_REACHED
            }
            else -> FeatureBlockReason.NOT_PREMIUM
        }
        
        // Show warning when approaching limits
        if (remainingUsage in 1..2) {
            val totalLimit = when (feature) {
                PremiumFeature.UNLIMITED_PHOTOS -> PremiumManager.FREE_PHOTO_LIMIT
                PremiumFeature.EXPORT_DATA -> PremiumManager.FREE_EXPORT_LIMIT
                else -> 0
            }
            return FeatureAccessResult.LimitWarning(remainingUsage, totalLimit)
        }
        
        return FeatureAccessResult.Blocked(
            reason = reason,
            remainingUsage = remainingUsage,
            canStartTrial = status.canStartTrial
        )
    }
    
    /**
     * Attempt to use a feature and track usage
     */
    fun useFeature(feature: PremiumFeature): FeatureAccessResult {
        val accessResult = checkFeatureAccess(feature)
        
        if (accessResult is FeatureAccessResult.Granted) {
            premiumManager.trackFeatureUsage(feature)
        }
        
        return accessResult
    }
    
    /**
     * Get user-friendly message for feature access result
     */
    fun getAccessMessage(result: FeatureAccessResult, feature: PremiumFeature): String {
        return when (result) {
            is FeatureAccessResult.Granted -> "Feature unlocked!"
            is FeatureAccessResult.Blocked -> result.reason.getMessage(feature)
            is FeatureAccessResult.LimitWarning -> {
                "You have ${result.remainingUsage} ${feature.getDisplayName().lowercase()} remaining out of ${result.totalLimit}"
            }
        }
    }
    
    /**
     * Get suggested action for blocked features
     */
    fun getSuggestedAction(result: FeatureAccessResult): PremiumAction {
        return when (result) {
            is FeatureAccessResult.Granted -> PremiumAction.NONE
            is FeatureAccessResult.Blocked -> {
                when {
                    result.canStartTrial -> PremiumAction.START_TRIAL
                    result.reason == FeatureBlockReason.TRIAL_EXPIRED -> PremiumAction.UPGRADE_PREMIUM
                    result.reason == FeatureBlockReason.SUBSCRIPTION_EXPIRED -> PremiumAction.RENEW_SUBSCRIPTION
                    else -> PremiumAction.UPGRADE_PREMIUM
                }
            }
            is FeatureAccessResult.LimitWarning -> PremiumAction.SHOW_UPGRADE_HINT
        }
    }
    
    /**
     * Get premium features that would be unlocked for user
     */
    fun getUnlockedFeatures(): List<PremiumFeature> {
        return PremiumFeature.values().filter { feature ->
            !feature.isFreeFeature() && !premiumManager.hasFeatureAccess(feature)
        }
    }
    
    /**
     * Get features user is currently using in free tier
     */
    fun getUsedFreeFeatures(): Map<PremiumFeature, Int> {
        return mapOf(
            PremiumFeature.UNLIMITED_PHOTOS to (PremiumManager.FREE_PHOTO_LIMIT - premiumManager.getRemainingUsage(PremiumFeature.UNLIMITED_PHOTOS)),
            PremiumFeature.EXPORT_DATA to (PremiumManager.FREE_EXPORT_LIMIT - premiumManager.getRemainingUsage(PremiumFeature.EXPORT_DATA))
        ).filterValues { it > 0 }
    }
    
    /**
     * Get premium value proposition based on user's current usage
     */
    fun getPersonalizedValueProposition(): List<String> {
        val propositions = mutableListOf<String>()
        val usedFeatures = getUsedFreeFeatures()
        val status = premiumManager.premiumStatus.value
        
        if (usedFeatures.containsKey(PremiumFeature.UNLIMITED_PHOTOS)) {
            propositions.add("📷 Store unlimited progress photos to track your entire journey")
        }
        
        if (usedFeatures.containsKey(PremiumFeature.EXPORT_DATA)) {
            propositions.add("📊 Export your data anytime to share with healthcare providers")
        }
        
        if (!status.hasAnyPremiumAccess()) {
            propositions.addAll(listOf(
                "🔍 Get advanced insights to break through plateaus faster",
                "🎯 Set custom goals tailored to your lifestyle",
                "⚡ Receive smart alerts when your progress stalls"
            ))
        }
        
        return propositions
    }
    
    /**
     * Check if user should see premium onboarding
     */
    fun shouldShowPremiumOnboarding(): Boolean {
        val status = premiumManager.premiumStatus.value
        val hasUsedAnyFeature = getUsedFreeFeatures().isNotEmpty()
        
        return !status.hasAnyPremiumAccess() && hasUsedAnyFeature
    }
    
    /**
     * Get recommended subscription tier for user
     */
    fun getRecommendedTier(): SubscriptionType {
        val usageLevel = getUsedFreeFeatures().values.sum()
        
        return when {
            usageLevel >= 5 -> SubscriptionType.YEARLY // Heavy user
            usageLevel >= 2 -> SubscriptionType.MONTHLY // Regular user  
            else -> SubscriptionType.MONTHLY // Light user
        }
    }
}

/**
 * Actions that can be suggested to users for premium features
 */
enum class PremiumAction {
    NONE,
    START_TRIAL,
    UPGRADE_PREMIUM,
    RENEW_SUBSCRIPTION,
    SHOW_UPGRADE_HINT;
    
    fun getActionText(): String {
        return when (this) {
            NONE -> ""
            START_TRIAL -> "Start Free Trial"
            UPGRADE_PREMIUM -> "Upgrade to Premium"
            RENEW_SUBSCRIPTION -> "Renew Subscription"
            SHOW_UPGRADE_HINT -> "Learn More"
        }
    }
}