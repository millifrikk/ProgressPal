package com.progresspal.app.utils

import com.progresspal.app.domain.models.ActivityLevel
import org.junit.Test
import org.junit.Assert.*

/**
 * Comprehensive tests for BodyCompositionUtils
 * Verifies that the "athletic build incorrectly labeled overweight" problem is solved
 */
class BodyCompositionUtilsTest {
    
    @Test
    fun testAthleticBuildProblemSolved() {
        // Original problem: 178cm/80kg athlete labeled as "Overweight" (BMI 25.2)
        val height = 178f
        val weight = 80f
        val waist = 82f  // Athletic waist measurement
        
        // Traditional BMI assessment (problematic)
        val bmi = BodyCompositionUtils.calculateBMI(weight, height)
        assertEquals(25.2f, bmi, 0.1f)  // BMI 25.2 = "Overweight" traditionally
        
        // Enhanced assessment for athletic individual
        val athleticAssessment = BodyCompositionUtils.getBodyCompositionScore(
            weight = weight,
            height = height,
            waist = waist,
            activityLevel = ActivityLevel.ATHLETIC
        )
        
        // Should show "Athletic Build" or similar, not "Overweight"
        assertTrue(
            "Athletic individual should not be labeled overweight",
            athleticAssessment.category.displayName.contains("Athletic") ||
            athleticAssessment.category.displayName == "Healthy"
        )
        
        // WHtR should be healthy for this individual
        val whtr = athleticAssessment.whtr!!
        assertEquals(0.46f, whtr, 0.01f)  // 82cm/178cm = 0.46
        assertTrue("WHtR should indicate healthy range", whtr < 0.5f)
    }
    
    @Test
    fun testWHtRCalculations() {
        // Test Waist-to-Height Ratio calculations
        
        // Healthy example: 70cm waist, 175cm height
        val healthyWHtR = BodyCompositionUtils.calculateWHtR(70f, 175f)
        assertEquals(0.4f, healthyWHtR, 0.01f)
        assertTrue("Should be in healthy range", healthyWHtR < 0.5f)
        
        // Elevated risk: 90cm waist, 170cm height
        val elevatedWHtR = BodyCompositionUtils.calculateWHtR(90f, 170f)
        assertEquals(0.53f, elevatedWHtR, 0.01f)
        assertTrue("Should indicate elevated risk", elevatedWHtR > 0.5f)
        
        // High risk: 110cm waist, 175cm height
        val highRiskWHtR = BodyCompositionUtils.calculateWHtR(110f, 175f)
        assertEquals(0.63f, highRiskWHtR, 0.01f)
        assertTrue("Should indicate high risk", highRiskWHtR > 0.6f)
    }
    
    @Test
    fun testBRICalculations() {
        // Test Body Roundness Index for different body shapes
        
        // Athletic build: relatively low waist for height
        val athleticBRI = BodyCompositionUtils.calculateBRI(82f, 178f)
        assertTrue("Athletic BRI should be low", athleticBRI < 4f)
        
        // Average build
        val averageBRI = BodyCompositionUtils.calculateBRI(90f, 175f)
        assertTrue("Average BRI should be moderate", averageBRI in 2f..6f)
        
        // High risk build
        val highRiskBRI = BodyCompositionUtils.calculateBRI(110f, 170f)
        assertTrue("High risk BRI should be elevated", highRiskBRI > 6f)
    }
    
    @Test
    fun testActivityLevelAdjustments() {
        val height = 180f
        val weight = 85f  // BMI 26.2 - traditionally "overweight"
        
        // Sedentary individual
        val sedentaryAssessment = BodyCompositionUtils.getBodyCompositionScore(
            weight = weight,
            height = height,
            waist = null,
            activityLevel = ActivityLevel.SEDENTARY
        )
        
        // Athletic individual (same weight/height)
        val athleticAssessment = BodyCompositionUtils.getBodyCompositionScore(
            weight = weight,
            height = height,
            waist = null,
            activityLevel = ActivityLevel.ATHLETIC
        )
        
        // Athletic individual should get better category due to activity adjustment
        assertTrue(
            "Athletic individuals should get better assessment than sedentary",
            athleticAssessment.category.ordinal < sedentaryAssessment.category.ordinal
        )
    }
    
    @Test
    fun testComprehensiveAssessment() {
        // Test the main assessment method with all parameters
        
        val assessment = BodyCompositionUtils.getBodyCompositionScore(
            weight = 75f,
            height = 170f,
            waist = 80f,
            hip = 95f,
            activityLevel = ActivityLevel.ACTIVE,
            age = 30,
            gender = "male"
        )
        
        // Should have all metrics calculated
        assertNotNull("BMI should be calculated", assessment.bmi)
        assertNotNull("WHtR should be calculated", assessment.whtr)
        assertNotNull("BRI should be calculated", assessment.bri)
        assertNotNull("ABSI should be calculated", assessment.absi)
        assertNotNull("WHR should be calculated", assessment.whr)
        
        // Should prioritize WHtR as primary metric when available
        assertEquals("WHtR should be primary metric", PrimaryMetric.WHTR, assessment.primaryMetric)
        
        // Should provide recommendation
        assertTrue("Should provide recommendation", assessment.recommendation.isNotEmpty())
    }
    
    @Test
    fun testHealthRiskCalculation() {
        // Low risk profile
        val lowRiskAssessment = BodyCompositionUtils.getBodyCompositionScore(
            weight = 70f,
            height = 175f,
            waist = 75f,
            activityLevel = ActivityLevel.ACTIVE
        )
        
        assertTrue(
            "Should be low risk",
            lowRiskAssessment.healthRisk in listOf(HealthRisk.VERY_LOW, HealthRisk.LOW)
        )
        
        // High risk profile
        val highRiskAssessment = BodyCompositionUtils.getBodyCompositionScore(
            weight = 100f,
            height = 170f,
            waist = 110f,
            activityLevel = ActivityLevel.SEDENTARY
        )
        
        assertTrue(
            "Should be high risk",
            highRiskAssessment.healthRisk in listOf(HealthRisk.MODERATE, HealthRisk.HIGH)
        )
    }
    
    @Test
    fun testCategoryProgression() {
        // Test that categories make sense in progression
        val height = 175f
        val activityLevel = ActivityLevel.ACTIVE
        
        // Test different waist measurements with same height
        val waistMeasurements = listOf(70f, 80f, 90f, 100f, 110f)
        val categories = waistMeasurements.map { waist ->
            BodyCompositionUtils.getBodyCompositionScore(
                weight = 75f,
                height = height,
                waist = waist,
                activityLevel = activityLevel
            ).category
        }
        
        // Categories should generally get worse as waist increases
        // (allowing for some variation due to complex logic)
        val firstCategory = categories.first()
        val lastCategory = categories.last()
        
        assertTrue(
            "Category should worsen as waist increases",
            firstCategory.ordinal <= lastCategory.ordinal
        )
    }
    
    @Test
    fun testBMIUtilsEnhancements() {
        // Test the enhanced BMI utilities
        
        val height = 178f
        val weight = 80f
        val waist = 82f
        
        // Traditional category (problematic)
        val traditionalCategory = BMIUtils.getBMICategory(BMIUtils.calculateBMI(weight, height))
        assertEquals("Overweight", traditionalCategory)  // The problem!
        
        // Enhanced category (solution)
        val enhancedCategory = BMIUtils.getEnhancedCategory(
            weightKg = weight,
            heightCm = height,
            activityLevel = ActivityLevel.ATHLETIC,
            waistCm = waist
        )
        
        // Should not be "Overweight" for athletic individual
        assertNotEquals(
            "Enhanced category should not label athletic build as overweight",
            "Overweight",
            enhancedCategory
        )
        
        assertTrue(
            "Should show athletic or healthy category",
            enhancedCategory.contains("Athletic") || enhancedCategory.contains("Healthy")
        )
    }
    
    @Test
    fun testWaistMeasurementRecommendation() {
        // Test when waist measurement should be recommended
        
        // Athletic individual with BMI > 25 should be recommended waist measurement
        assertTrue(
            "Athletic individuals with BMI > 25 should add waist measurement",
            BMIUtils.shouldAddWaistMeasurement(80f, 178f, ActivityLevel.ATHLETIC)
        )
        
        // Low BMI individual shouldn't need waist measurement
        assertFalse(
            "Low BMI individuals don't need waist measurement",
            BMIUtils.shouldAddWaistMeasurement(65f, 175f, ActivityLevel.SEDENTARY)
        )
        
        // Borderline BMI should benefit from waist measurement
        assertTrue(
            "Borderline BMI should benefit from waist measurement",
            BMIUtils.shouldAddWaistMeasurement(75f, 170f, ActivityLevel.ACTIVE)
        )
    }
    
    @Test
    fun testMetricSummaryFormatting() {
        val assessment = BodyCompositionUtils.getBodyCompositionScore(
            weight = 75f,
            height = 170f,
            waist = 80f,
            activityLevel = ActivityLevel.ACTIVE
        )
        
        val summary = assessment.getMetricSummary()
        
        // Should include multiple metrics
        assertTrue("Should include WHtR", summary.contains("WHtR"))
        assertTrue("Should include BRI", summary.contains("BRI"))
        assertTrue("Should include BMI", summary.contains("BMI"))
        
        // Should be properly formatted
        assertTrue("Should use bullet separator", summary.contains("•"))
    }
}