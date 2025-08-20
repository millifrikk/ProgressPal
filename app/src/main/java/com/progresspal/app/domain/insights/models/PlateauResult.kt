package com.progresspal.app.domain.insights.models

import com.progresspal.app.domain.insights.PlateauPeriod
import java.util.*

/**
 * Comprehensive result of plateau analysis
 */
data class PlateauResult(
    val isInPlateau: Boolean,
    val currentPlateau: PlateauPeriod?,
    val historicalPlateaus: List<PlateauPeriod>,
    val plateauProbability: Float, // 0.0 to 1.0
    val daysSincePlateauStart: Int,
    val averagePlateauWeight: Float,
    val plateauSeverity: PlateauSeverity,
    val breakoutStrategies: List<String>,
    val plateauTriggers: List<String>,
    val expectedBreakoutTimeframe: String,
    val hasInsufficientData: Boolean = false
) {
    
    companion object {
        fun insufficientData(): PlateauResult {
            return PlateauResult(
                isInPlateau = false,
                currentPlateau = null,
                historicalPlateaus = emptyList(),
                plateauProbability = 0f,
                daysSincePlateauStart = 0,
                averagePlateauWeight = 0f,
                plateauSeverity = PlateauSeverity.NONE,
                breakoutStrategies = listOf(
                    "📊 Keep tracking your weight consistently",
                    "🎯 Stay focused on your routine",
                    "⏱️ Give it time - results take patience"
                ),
                plateauTriggers = emptyList(),
                expectedBreakoutTimeframe = "Continue tracking to unlock plateau analysis",
                hasInsufficientData = true
            )
        }
    }
    
    /**
     * Get user-friendly plateau status message
     */
    fun getPlateauStatusMessage(): String {
        return when {
            hasInsufficientData -> "📊 Track more data to analyze plateaus"
            isInPlateau -> {
                when (plateauSeverity) {
                    PlateauSeverity.MILD -> "⚖️ Minor plateau detected ($daysSincePlateauStart days)"
                    PlateauSeverity.MODERATE -> "⚠️ Plateau in progress ($daysSincePlateauStart days)"
                    PlateauSeverity.SEVERE -> "🚨 Extended plateau ($daysSincePlateauStart days)"
                    PlateauSeverity.NONE -> "📈 Progress continues"
                }
            }
            plateauProbability > 0.7f -> "⚠️ Possible plateau forming"
            plateauProbability > 0.5f -> "👀 Watch for plateau signs"
            else -> "🎯 Progress on track"
        }
    }
    
    /**
     * Get priority action message based on plateau status
     */
    fun getPriorityAction(): String {
        return when {
            hasInsufficientData -> "Keep tracking daily to unlock insights"
            plateauSeverity == PlateauSeverity.SEVERE -> "Time for significant strategy changes"
            plateauSeverity == PlateauSeverity.MODERATE -> "Consider adjusting your approach"
            plateauSeverity == PlateauSeverity.MILD -> "Small tweaks may help break through"
            plateauProbability > 0.7f -> "Be proactive - make small changes now"
            else -> "Continue your current successful approach"
        }
    }
    
    /**
     * Get encouragement message based on plateau context
     */
    fun getEncouragementMessage(): String {
        return when {
            hasInsufficientData -> "🚀 Every expert was once a beginner"
            historicalPlateaus.isNotEmpty() && !isInPlateau -> "💪 You've broken through plateaus before!"
            isInPlateau && daysSincePlateauStart < 30 -> "🎯 Plateaus are normal - stay consistent"
            isInPlateau -> "🌟 This is when mental strength matters most"
            else -> "🔥 You're crushing your goals!"
        }
    }
    
    /**
     * Get plateau probability as percentage string
     */
    fun getPlateauProbabilityPercent(): String {
        return "${(plateauProbability * 100).toInt()}%"
    }
    
    /**
     * Get the most effective breakout strategy
     */
    fun getTopStrategy(): String {
        return breakoutStrategies.firstOrNull() 
            ?: "🎯 Stay consistent with your current routine"
    }
    
    /**
     * Get summary of historical plateau patterns
     */
    fun getHistoricalSummary(): String {
        return when {
            historicalPlateaus.isEmpty() -> "No previous plateaus detected"
            historicalPlateaus.size == 1 -> "1 previous plateau overcome"
            else -> "${historicalPlateaus.size} previous plateaus overcome"
        }
    }
}