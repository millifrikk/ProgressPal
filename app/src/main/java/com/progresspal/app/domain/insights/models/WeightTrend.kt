package com.progresspal.app.domain.insights.models

/**
 * Represents the direction of weight change trend
 */
enum class WeightTrend {
    LOSING,    // User is losing weight consistently
    GAINING,   // User is gaining weight consistently  
    STABLE;    // Weight is relatively stable
    
    /**
     * Get user-friendly description of the trend
     */
    fun getDescription(): String {
        return when (this) {
            LOSING -> "Losing Weight"
            GAINING -> "Gaining Weight"
            STABLE -> "Weight Stable"
        }
    }
    
    /**
     * Get emoji representation of the trend
     */
    fun getEmoji(): String {
        return when (this) {
            LOSING -> "📉"
            GAINING -> "📈"
            STABLE -> "⚖️"
        }
    }
    
    /**
     * Get color resource for UI representation
     */
    fun getColorRes(): String {
        return when (this) {
            LOSING -> "pal_success"  // Green for weight loss
            GAINING -> "pal_warning" // Orange for weight gain
            STABLE -> "pal_info"     // Blue for stable
        }
    }
    
    /**
     * Check if trend is positive (based on typical weight loss goals)
     */
    fun isPositive(): Boolean {
        return this == LOSING
    }
}