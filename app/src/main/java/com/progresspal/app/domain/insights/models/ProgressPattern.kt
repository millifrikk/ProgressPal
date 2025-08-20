package com.progresspal.app.domain.insights.models

/**
 * Represents different patterns detected in weight progress data
 */
sealed class ProgressPattern {
    
    /**
     * Weight plateau detected - minimal change over extended period
     */
    data class Plateau(
        val duration: Int, // days
        val averageWeight: Float
    ) : ProgressPattern() {
        
        fun getDescription(): String {
            return "⚖️ Weight plateau detected for $duration days"
        }
        
        fun getAdvice(): String {
            return "Consider changing your routine to break through the plateau. " +
                   "Try adjusting your calorie intake or adding new exercises."
        }
    }
    
    /**
     * Rapid weight change detected - significant change in short time
     */
    data class RapidChange(
        val change: Float, // positive = gain, negative = loss
        val days: Int
    ) : ProgressPattern() {
        
        fun getDescription(): String {
            val direction = if (change > 0) "gain" else "loss"
            val amount = kotlin.math.abs(change)
            return "⚡ Rapid weight $direction: ${amount.format(1)}kg in $days days"
        }
        
        fun getAdvice(): String {
            return if (change > 0) {
                "Rapid weight gain detected. Consider reviewing your recent diet and exercise habits."
            } else {
                "Rapid weight loss detected. Ensure you're losing weight in a healthy, sustainable way."
            }
        }
    }
    
    /**
     * Weekly cycle pattern - weight fluctuates in weekly patterns
     */
    data class WeeklyCycle(
        val averageFluctuation: Float,
        val peakDay: String, // day of week with highest weight
        val lowDay: String   // day of week with lowest weight
    ) : ProgressPattern() {
        
        fun getDescription(): String {
            return "📅 Weekly pattern: Weight peaks on $peakDay, lowest on $lowDay"
        }
        
        fun getAdvice(): String {
            return "You have a weekly weight pattern. This is often related to weekend habits or water retention. " +
                   "Focus on consistency throughout the week."
        }
    }
    
    /**
     * Consistent progress - steady, healthy rate of change
     */
    data class ConsistentProgress(
        val weeklyRate: Float, // kg per week
        val consistency: Float // 0-1, how consistent the rate is
    ) : ProgressPattern() {
        
        fun getDescription(): String {
            val rate = kotlin.math.abs(weeklyRate)
            val direction = if (weeklyRate < 0) "losing" else "gaining"
            return "📈 Consistent progress: $direction ${rate.format(1)}kg per week"
        }
        
        fun getAdvice(): String {
            return "Excellent! You're maintaining a consistent, healthy rate of progress. Keep doing what you're doing!"
        }
    }
    
    /**
     * Irregular pattern - inconsistent weight changes
     */
    data class IrregularPattern(
        val variability: Float, // how much weight varies
        val frequency: Int      // how often weight changes significantly
    ) : ProgressPattern() {
        
        fun getDescription(): String {
            return "🎢 Irregular pattern: Weight varies significantly (±${variability.format(1)}kg)"
        }
        
        fun getAdvice(): String {
            return "Your weight shows irregular patterns. Try to identify triggers like stress, sleep, or meal timing. " +
                   "Consider weighing yourself at the same time each day."
        }
    }
}

/**
 * Extension function to format float to specified decimal places
 */
private fun Float.format(decimals: Int): String {
    return "%.${decimals}f".format(this)
}