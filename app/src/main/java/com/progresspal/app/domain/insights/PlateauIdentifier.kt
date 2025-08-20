package com.progresspal.app.domain.insights

import com.progresspal.app.data.database.entities.WeightEntity
import com.progresspal.app.domain.insights.models.PlateauResult
import com.progresspal.app.domain.insights.models.PlateauSeverity
import java.util.*
import kotlin.math.*

/**
 * Advanced algorithm for detecting and analyzing weight plateaus
 * Provides detailed plateau analysis and breakthrough recommendations
 */
class PlateauIdentifier {
    
    companion object {
        private const val MIN_PLATEAU_DAYS = 14           // Minimum days to consider a plateau
        private const val STRICT_PLATEAU_DAYS = 21        // Days for strict plateau detection
        private const val MAX_PLATEAU_VARIATION = 1.0f    // Max weight variation in kg for plateau
        private const val TREND_ANALYSIS_WINDOW = 7       // Days to analyze trend changes
        private const val MOVING_AVERAGE_WINDOW = 5       // Window for smoothing data
    }
    
    /**
     * Analyze weight data for plateau patterns and provide detailed results
     */
    suspend fun analyzePlateaus(weightEntries: List<WeightEntity>): PlateauResult {
        if (weightEntries.size < MIN_PLATEAU_DAYS) {
            return PlateauResult.insufficientData()
        }
        
        val sortedEntries = weightEntries.sortedBy { it.date }
        val smoothedData = applySmoothingFilter(sortedEntries)
        
        val currentPlateau = detectCurrentPlateau(smoothedData)
        val historicalPlateaus = detectHistoricalPlateaus(smoothedData)
        val plateauProbability = calculatePlateauProbability(smoothedData)
        val breakoutStrategies = generateBreakoutStrategies(currentPlateau, sortedEntries)
        
        return PlateauResult(
            isInPlateau = currentPlateau != null,
            currentPlateau = currentPlateau,
            historicalPlateaus = historicalPlateaus,
            plateauProbability = plateauProbability,
            daysSincePlateauStart = currentPlateau?.duration ?: 0,
            averagePlateauWeight = currentPlateau?.averageWeight ?: 0f,
            plateauSeverity = determinePlateauSeverity(currentPlateau, sortedEntries),
            breakoutStrategies = breakoutStrategies,
            plateauTriggers = identifyPossibleTriggers(sortedEntries),
            expectedBreakoutTimeframe = estimateBreakoutTimeframe(currentPlateau, historicalPlateaus)
        )
    }
    
    /**
     * Detect if user is currently in a plateau
     */
    private fun detectCurrentPlateau(entries: List<WeightEntity>): PlateauPeriod? {
        if (entries.size < MIN_PLATEAU_DAYS) return null
        
        // Analyze recent data for plateau characteristics
        val recentEntries = getRecentEntries(entries, STRICT_PLATEAU_DAYS * 2)
        if (recentEntries.size < MIN_PLATEAU_DAYS) return null
        
        // Look for longest recent plateau period
        for (windowSize in STRICT_PLATEAU_DAYS..recentEntries.size) {
            val window = recentEntries.takeLast(windowSize)
            
            if (isPlateauPeriod(window)) {
                return PlateauPeriod(
                    startDate = window.first().date,
                    endDate = window.last().date,
                    duration = windowSize,
                    averageWeight = window.map { it.weight }.average().toFloat(),
                    weightVariation = calculateWeightVariation(window),
                    minWeight = window.minOf { it.weight },
                    maxWeight = window.maxOf { it.weight }
                )
            }
        }
        
        return null
    }
    
    /**
     * Detect historical plateau periods
     */
    private fun detectHistoricalPlateaus(entries: List<WeightEntity>): List<PlateauPeriod> {
        val plateaus = mutableListOf<PlateauPeriod>()
        var currentStart = 0
        
        while (currentStart < entries.size - MIN_PLATEAU_DAYS) {
            var bestPlateauEnd = -1
            var bestPlateauLength = 0
            
            // Look for plateaus starting from currentStart
            for (windowSize in MIN_PLATEAU_DAYS..(entries.size - currentStart)) {
                val window = entries.subList(currentStart, currentStart + windowSize)
                
                if (isPlateauPeriod(window) && windowSize > bestPlateauLength) {
                    bestPlateauEnd = currentStart + windowSize - 1
                    bestPlateauLength = windowSize
                }
            }
            
            if (bestPlateauEnd > -1) {
                val plateauWindow = entries.subList(currentStart, bestPlateauEnd + 1)
                plateaus.add(
                    PlateauPeriod(
                        startDate = plateauWindow.first().date,
                        endDate = plateauWindow.last().date,
                        duration = bestPlateauLength,
                        averageWeight = plateauWindow.map { it.weight }.average().toFloat(),
                        weightVariation = calculateWeightVariation(plateauWindow),
                        minWeight = plateauWindow.minOf { it.weight },
                        maxWeight = plateauWindow.maxOf { it.weight }
                    )
                )
                currentStart = bestPlateauEnd + 1
            } else {
                currentStart++
            }
        }
        
        return plateaus
    }
    
    /**
     * Calculate probability that user is entering or in a plateau
     */
    private fun calculatePlateauProbability(entries: List<WeightEntity>): Float {
        if (entries.size < TREND_ANALYSIS_WINDOW) return 0f
        
        val recentEntries = getRecentEntries(entries, TREND_ANALYSIS_WINDOW * 2)
        val recentVariation = calculateWeightVariation(recentEntries)
        val recentTrendSlope = calculateTrendSlope(recentEntries)
        
        // Factors that increase plateau probability
        val lowVariationScore = (MAX_PLATEAU_VARIATION - recentVariation).coerceAtLeast(0f) / MAX_PLATEAU_VARIATION
        val flatTrendScore = (0.1f - abs(recentTrendSlope)).coerceAtLeast(0f) / 0.1f
        val durationScore = (recentEntries.size.toFloat() / STRICT_PLATEAU_DAYS).coerceAtMost(1f)
        
        return ((lowVariationScore * 0.4f) + (flatTrendScore * 0.4f) + (durationScore * 0.2f))
            .coerceIn(0f, 1f)
    }
    
    /**
     * Determine severity of current plateau
     */
    private fun determinePlateauSeverity(
        plateau: PlateauPeriod?,
        allEntries: List<WeightEntity>
    ): PlateauSeverity {
        if (plateau == null) return PlateauSeverity.NONE
        
        return when {
            plateau.duration >= 60 -> PlateauSeverity.SEVERE      // 2+ months
            plateau.duration >= 35 -> PlateauSeverity.MODERATE    // 5+ weeks  
            plateau.duration >= 21 -> PlateauSeverity.MILD        // 3+ weeks
            else -> PlateauSeverity.NONE
        }
    }
    
    /**
     * Generate personalized strategies to break out of plateau
     */
    private fun generateBreakoutStrategies(
        plateau: PlateauPeriod?,
        entries: List<WeightEntity>
    ): List<String> {
        val strategies = mutableListOf<String>()
        
        if (plateau == null) {
            strategies.add("🎯 Keep up your current routine to maintain progress!")
            return strategies
        }
        
        when (determinePlateauSeverity(plateau, entries)) {
            PlateauSeverity.MILD -> {
                strategies.addAll(listOf(
                    "💪 Try adding 10-15 minutes to your workout routine",
                    "🥗 Consider tracking your food intake more carefully",
                    "💧 Increase your daily water intake",
                    "😴 Ensure you're getting 7-8 hours of quality sleep"
                ))
            }
            PlateauSeverity.MODERATE -> {
                strategies.addAll(listOf(
                    "🔄 Change your exercise routine - try new activities",
                    "🍎 Reassess your calorie needs - they may have changed",
                    "⏱️ Consider intermittent fasting (consult your doctor first)",
                    "🏃 Add interval training to boost metabolism",
                    "📊 Track measurements and photos - you may be gaining muscle"
                ))
            }
            PlateauSeverity.SEVERE -> {
                strategies.addAll(listOf(
                    "👥 Consider consulting a nutritionist or trainer",
                    "🔬 Get blood work to check for metabolic issues",
                    "📱 Try a completely new approach to diet and exercise",
                    "🧘 Focus on stress management and mental health",
                    "⚖️ Take a planned diet break for 1-2 weeks",
                    "🎯 Set non-scale victory goals (strength, endurance, etc.)"
                ))
            }
            PlateauSeverity.NONE -> {
                strategies.add("✨ You're not in a plateau - keep going!")
            }
        }
        
        return strategies
    }
    
    /**
     * Identify possible triggers for plateau
     */
    private fun identifyPossibleTriggers(entries: List<WeightEntity>): List<String> {
        val triggers = mutableListOf<String>()
        
        // Analyze timing patterns
        val recentEntries = getRecentEntries(entries, 30)
        if (recentEntries.isNotEmpty()) {
            val timespan = daysBetween(recentEntries.first().date, recentEntries.last().date)
            
            when {
                timespan >= 90 -> triggers.add("⏰ Long-term adaptation - your body may have adjusted to your routine")
                timespan >= 60 -> triggers.add("🔄 Metabolic adaptation - consider changing your approach")
                timespan >= 30 -> triggers.add("📈 Progress slowing - this is normal, stay consistent")
            }
        }
        
        // Add general possible triggers
        triggers.addAll(listOf(
            "🍽️ Calorie creep - portion sizes may have gradually increased",
            "💪 Muscle gain - you might be building muscle while losing fat",
            "💧 Water retention - hormones, sodium, or stress can affect weight",
            "😴 Sleep quality - poor sleep affects metabolism and hunger hormones"
        ))
        
        return triggers.take(3) // Return top 3 most likely triggers
    }
    
    /**
     * Estimate when plateau might break based on historical data
     */
    private fun estimateBreakoutTimeframe(
        currentPlateau: PlateauPeriod?,
        historicalPlateaus: List<PlateauPeriod>
    ): String {
        if (currentPlateau == null) return "Not applicable"
        
        val averageHistoricalDuration = if (historicalPlateaus.isNotEmpty()) {
            historicalPlateaus.map { it.duration }.average()
        } else {
            25.0 // Default expectation
        }
        
        val daysRemaining = (averageHistoricalDuration - currentPlateau.duration).toInt()
        
        return when {
            daysRemaining <= 0 -> "Plateau should break soon with consistent effort"
            daysRemaining <= 7 -> "Expected to break within 1 week"
            daysRemaining <= 14 -> "Expected to break within 2 weeks"
            daysRemaining <= 28 -> "Expected to break within 1 month"
            else -> "May take several weeks - consider strategy changes"
        }
    }
    
    // Helper functions
    
    private fun isPlateauPeriod(entries: List<WeightEntity>): Boolean {
        if (entries.size < MIN_PLATEAU_DAYS) return false
        
        val variation = calculateWeightVariation(entries)
        val trendSlope = abs(calculateTrendSlope(entries))
        
        return variation <= MAX_PLATEAU_VARIATION && trendSlope <= 0.05f
    }
    
    private fun applySmoothingFilter(entries: List<WeightEntity>): List<WeightEntity> {
        if (entries.size <= MOVING_AVERAGE_WINDOW) return entries
        
        return entries.mapIndexed { index, entry ->
            val start = maxOf(0, index - MOVING_AVERAGE_WINDOW / 2)
            val end = minOf(entries.size - 1, index + MOVING_AVERAGE_WINDOW / 2)
            val window = entries.subList(start, end + 1)
            val smoothedWeight = window.map { it.weight }.average().toFloat()
            
            entry.copy(weight = smoothedWeight)
        }
    }
    
    private fun calculateWeightVariation(entries: List<WeightEntity>): Float {
        if (entries.isEmpty()) return 0f
        
        val weights = entries.map { it.weight }
        val mean = weights.average().toFloat()
        val variance = weights.map { (it - mean).pow(2) }.average().toFloat()
        
        return sqrt(variance)
    }
    
    private fun calculateTrendSlope(entries: List<WeightEntity>): Float {
        if (entries.size < 2) return 0f
        
        val n = entries.size
        val xValues = entries.indices.map { it.toFloat() }
        val yValues = entries.map { it.weight }
        
        val xMean = xValues.average().toFloat()
        val yMean = yValues.average().toFloat()
        
        val numerator = xValues.zip(yValues) { x, y -> (x - xMean) * (y - yMean) }.sum()
        val denominator = xValues.map { (it - xMean).pow(2) }.sum()
        
        return if (denominator != 0f) numerator / denominator else 0f
    }
    
    private fun getRecentEntries(entries: List<WeightEntity>, days: Int): List<WeightEntity> {
        val cutoffDate = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -days)
        }.time
        
        return entries.filter { it.date.after(cutoffDate) }
    }
    
    private fun daysBetween(startDate: Date, endDate: Date): Int {
        val diffInMillis = endDate.time - startDate.time
        return (diffInMillis / (24 * 60 * 60 * 1000)).toInt()
    }
}

/**
 * Represents a detected plateau period
 */
data class PlateauPeriod(
    val startDate: Date,
    val endDate: Date,
    val duration: Int, // days
    val averageWeight: Float,
    val weightVariation: Float,
    val minWeight: Float,
    val maxWeight: Float
)