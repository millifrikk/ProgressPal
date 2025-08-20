package com.progresspal.app.utils

object BMICalculator {
    
    fun calculate(weightKg: Float, heightCm: Float): Float {
        val heightM = heightCm / 100f
        return weightKg / (heightM * heightM)
    }
    
    fun getCategory(bmi: Float): String {
        return when {
            bmi < 18.5f -> "Underweight"
            bmi < 25f -> "Normal"
            bmi < 30f -> "Overweight"
            else -> "Obese"
        }
    }
    
    fun getCategoryColor(bmi: Float): String {
        return when {
            bmi < 18.5f -> "#FFC107" // Warning (yellow)
            bmi < 25f -> "#4CAF50" // Success (green)
            bmi < 30f -> "#FF9800" // Warning (orange)
            else -> "#F44336" // Error (red)
        }
    }
}