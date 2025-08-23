package com.progresspal.app.utils

import com.progresspal.app.domain.models.MeasurementSystem

/**
 * Utility class for converting between metric and imperial measurement units
 * Supports weight, height, and body measurement conversions with proper formatting
 */
object UnitConverter {
    
    // Weight conversions
    fun kgToLbs(kg: Float): Float = kg * 2.20462f
    fun lbsToKg(lbs: Float): Float = lbs / 2.20462f
    
    // Height conversions
    fun cmToInches(cm: Float): Float = cm / 2.54f
    fun inchesToCm(inches: Float): Float = inches * 2.54f
    
    /**
     * Convert cm to feet and inches
     * @return Pair of (feet, inches)
     */
    fun cmToFeetInches(cm: Float): Pair<Int, Float> {
        val totalInches = cmToInches(cm)
        val feet = (totalInches / 12).toInt()
        val inches = totalInches % 12
        return Pair(feet, inches)
    }
    
    /**
     * Convert feet and inches to cm
     */
    fun feetInchesToCm(feet: Int, inches: Float): Float {
        val totalInches = feet * 12 + inches
        return inchesToCm(totalInches)
    }
    
    // Body measurement conversions (waist, chest, hips)
    fun cmToInchesWaist(cm: Float): Float = cm / 2.54f
    fun inchesToCmWaist(inches: Float): Float = inches * 2.54f
    
    // Display formatting methods
    
    /**
     * Format weight value according to measurement system
     */
    fun formatWeight(value: Float, system: MeasurementSystem): String {
        return when (system) {
            MeasurementSystem.METRIC -> "%.1f kg".format(value)
            MeasurementSystem.IMPERIAL -> "%.1f lbs".format(kgToLbs(value))
        }
    }
    
    /**
     * Format height value according to measurement system
     */
    fun formatHeight(value: Float, system: MeasurementSystem): String {
        return when (system) {
            MeasurementSystem.METRIC -> "%.0f cm".format(value)
            MeasurementSystem.IMPERIAL -> {
                val (feet, inches) = cmToFeetInches(value)
                "$feet'%.1f\"".format(inches)
            }
        }
    }
    
    /**
     * Format body measurement (waist, chest, hips) according to measurement system
     */
    fun formatBodyMeasurement(value: Float, system: MeasurementSystem): String {
        return when (system) {
            MeasurementSystem.METRIC -> "%.1f cm".format(value)
            MeasurementSystem.IMPERIAL -> "%.1f in".format(cmToInchesWaist(value))
        }
    }
    
    /**
     * Get weight unit label for the measurement system
     */
    fun getWeightUnit(system: MeasurementSystem): String {
        return when (system) {
            MeasurementSystem.METRIC -> "kg"
            MeasurementSystem.IMPERIAL -> "lbs"
        }
    }
    
    /**
     * Get height unit label for the measurement system
     */
    fun getHeightUnit(system: MeasurementSystem): String {
        return when (system) {
            MeasurementSystem.METRIC -> "cm"
            MeasurementSystem.IMPERIAL -> "ft/in"
        }
    }
    
    /**
     * Get body measurement unit label for the measurement system
     */
    fun getBodyMeasurementUnit(system: MeasurementSystem): String {
        return when (system) {
            MeasurementSystem.METRIC -> "cm"
            MeasurementSystem.IMPERIAL -> "in"
        }
    }
    
    /**
     * Convert weight value to the specified measurement system
     * Input is always in kg (internal storage format)
     */
    fun convertWeight(valueKg: Float, toSystem: MeasurementSystem): Float {
        return when (toSystem) {
            MeasurementSystem.METRIC -> valueKg
            MeasurementSystem.IMPERIAL -> kgToLbs(valueKg)
        }
    }
    
    /**
     * Convert height value to the specified measurement system
     * Input is always in cm (internal storage format)
     */
    fun convertHeight(valueCm: Float, toSystem: MeasurementSystem): Float {
        return when (toSystem) {
            MeasurementSystem.METRIC -> valueCm
            MeasurementSystem.IMPERIAL -> cmToInches(valueCm)
        }
    }
    
    /**
     * Convert body measurement to the specified measurement system
     * Input is always in cm (internal storage format)
     */
    fun convertBodyMeasurement(valueCm: Float, toSystem: MeasurementSystem): Float {
        return when (toSystem) {
            MeasurementSystem.METRIC -> valueCm
            MeasurementSystem.IMPERIAL -> cmToInchesWaist(valueCm)
        }
    }
    
    /**
     * Convert user input back to metric for database storage
     * Weight conversion from display format to internal kg
     */
    fun weightToKg(value: Float, fromSystem: MeasurementSystem): Float {
        return when (fromSystem) {
            MeasurementSystem.METRIC -> value
            MeasurementSystem.IMPERIAL -> lbsToKg(value)
        }
    }
    
    /**
     * Convert user input back to metric for database storage
     * Height conversion from display format to internal cm
     */
    fun heightToCm(value: Float, fromSystem: MeasurementSystem): Float {
        return when (fromSystem) {
            MeasurementSystem.METRIC -> value
            MeasurementSystem.IMPERIAL -> inchesToCm(value)
        }
    }
    
    /**
     * Convert user input back to metric for database storage
     * Body measurement conversion from display format to internal cm
     */
    fun bodyMeasurementToCm(value: Float, fromSystem: MeasurementSystem): Float {
        return when (fromSystem) {
            MeasurementSystem.METRIC -> value
            MeasurementSystem.IMPERIAL -> inchesToCmWaist(value)
        }
    }
}