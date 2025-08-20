package com.progresspal.app.domain.insights.models

/**
 * Represents the severity level of a weight plateau
 */
enum class PlateauSeverity {
    NONE,      // No plateau detected
    MILD,      // 3-5 weeks plateau
    MODERATE,  // 5-8 weeks plateau  
    SEVERE;    // 8+ weeks plateau
    
    /**
     * Get user-friendly description of severity
     */
    fun getDescription(): String {
        return when (this) {
            NONE -> "No Plateau"
            MILD -> "Mild Plateau"
            MODERATE -> "Moderate Plateau"
            SEVERE -> "Extended Plateau"
        }
    }
    
    /**
     * Get emoji representation
     */
    fun getEmoji(): String {
        return when (this) {
            NONE -> "✅"
            MILD -> "⚠️"
            MODERATE -> "🟡"
            SEVERE -> "🔴"
        }
    }
    
    /**
     * Get color resource for UI
     */
    fun getColorRes(): String {
        return when (this) {
            NONE -> "pal_success"
            MILD -> "pal_warning"
            MODERATE -> "pal_warning"
            SEVERE -> "pal_error"
        }
    }
    
    /**
     * Get urgency level (0-3)
     */
    fun getUrgencyLevel(): Int {
        return when (this) {
            NONE -> 0
            MILD -> 1
            MODERATE -> 2
            SEVERE -> 3
        }
    }
    
    /**
     * Get recommended action frequency
     */
    fun getActionFrequency(): String {
        return when (this) {
            NONE -> "Continue routine"
            MILD -> "Weekly adjustments"
            MODERATE -> "Bi-weekly reviews"
            SEVERE -> "Immediate changes needed"
        }
    }
    
    /**
     * Check if plateau requires immediate attention
     */
    fun requiresImmediateAction(): Boolean {
        return this == SEVERE
    }
    
    /**
     * Get expected resolution timeframe
     */
    fun getExpectedResolutionTime(): String {
        return when (this) {
            NONE -> "N/A"
            MILD -> "1-2 weeks with adjustments"
            MODERATE -> "2-4 weeks with strategy changes"
            SEVERE -> "4+ weeks with significant changes"
        }
    }
}