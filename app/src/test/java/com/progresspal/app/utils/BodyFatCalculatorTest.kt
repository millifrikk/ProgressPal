package com.progresspal.app.utils

import com.progresspal.app.domain.models.User
import org.junit.Test
import org.junit.Assert.*
import kotlin.math.abs

/**
 * Unit tests for BodyFatCalculator
 * Tests the Navy Method calculations for accuracy and edge cases
 */
class BodyFatCalculatorTest {

    @Test
    fun testNavyMethodMaleCalculation() {
        // Test case: Male, 180cm height, 85cm waist, 38cm neck
        // Expected result: approximately 15-20% body fat
        val bodyFat = BodyFatCalculator.calculateNavyMethod(
            gender = User.Gender.MALE,
            waistCm = 85f,
            neckCm = 38f,
            heightCm = 180f
        )
        
        assertTrue("Body fat should be between 10-25%", bodyFat in 10f..25f)
    }
    
    @Test
    fun testNavyMethodFemaleCalculation() {
        // Test case: Female, 165cm height, 75cm waist, 33cm neck, 95cm hips
        // Expected result: approximately 20-30% body fat
        val bodyFat = BodyFatCalculator.calculateNavyMethod(
            gender = User.Gender.FEMALE,
            waistCm = 75f,
            neckCm = 33f,
            heightCm = 165f,
            hipsCm = 95f
        )
        
        assertTrue("Body fat should be between 15-35%", bodyFat in 15f..35f)
    }
    
    @Test
    fun testNavyMethodFemaleWithoutHips() {
        // Test female calculation without hips measurement
        val bodyFat = BodyFatCalculator.calculateNavyMethod(
            gender = User.Gender.FEMALE,
            waistCm = 75f,
            neckCm = 33f,
            heightCm = 165f,
            hipsCm = null
        )
        
        assertEquals("Should return default 25% when hips not provided", 25f, bodyFat)
    }
    
    @Test
    fun testInvalidMeasurementsHandling() {
        // Test with waist <= neck (invalid)
        val bodyFat = BodyFatCalculator.calculateNavyMethod(
            gender = User.Gender.MALE,
            waistCm = 35f,
            neckCm = 40f,
            heightCm = 180f
        )
        
        assertEquals("Should return 5% for invalid male measurements", 5f, bodyFat)
    }
    
    @Test
    fun testBodyFatCategoryMale() {
        val athleteCategory = BodyFatCalculator.getCategory(10f, User.Gender.MALE)
        assertEquals("Athletes", athleteCategory)
        
        val fitnessCategory = BodyFatCalculator.getCategory(15f, User.Gender.MALE)
        assertEquals("Fitness", fitnessCategory)
        
        val averageCategory = BodyFatCalculator.getCategory(20f, User.Gender.MALE)
        assertEquals("Average", averageCategory)
        
        val obeseCategory = BodyFatCalculator.getCategory(35f, User.Gender.MALE)
        assertEquals("Obese", obeseCategory)
    }
    
    @Test
    fun testBodyFatCategoryFemale() {
        val athleteCategory = BodyFatCalculator.getCategory(16f, User.Gender.FEMALE)
        assertEquals("Athletes", athleteCategory)
        
        val fitnessCategory = BodyFatCalculator.getCategory(22f, User.Gender.FEMALE)
        assertEquals("Fitness", fitnessCategory)
        
        val averageCategory = BodyFatCalculator.getCategory(28f, User.Gender.FEMALE)
        assertEquals("Average", averageCategory)
        
        val obeseCategory = BodyFatCalculator.getCategory(40f, User.Gender.FEMALE)
        assertEquals("Obese", obeseCategory)
    }
    
    @Test
    fun testHealthyRangeCheck() {
        // Test male healthy range
        assertTrue(BodyFatCalculator.isHealthyRange(15f, User.Gender.MALE))
        assertFalse(BodyFatCalculator.isHealthyRange(25f, User.Gender.MALE))
        assertFalse(BodyFatCalculator.isHealthyRange(5f, User.Gender.MALE))
        
        // Test female healthy range
        assertTrue(BodyFatCalculator.isHealthyRange(22f, User.Gender.FEMALE))
        assertFalse(BodyFatCalculator.isHealthyRange(35f, User.Gender.FEMALE))
        assertFalse(BodyFatCalculator.isHealthyRange(10f, User.Gender.FEMALE))
    }
    
    @Test
    fun testDetailedAssessment() {
        val assessment = BodyFatCalculator.getDetailedAssessment(15f, User.Gender.MALE)
        
        assertEquals(15f, assessment.percentage)
        assertEquals("Fitness", assessment.category)
        assertEquals("Low Risk", assessment.healthRisk)
        assertTrue(assessment.isHealthy)
        assertTrue(assessment.recommendations.isNotEmpty())
        assertTrue(assessment.description.isNotEmpty())
    }
    
    @Test
    fun testIdealRangeCalculation() {
        val maleRange = BodyFatCalculator.getIdealRange(User.Gender.MALE)
        assertEquals(12f, maleRange.first)
        assertEquals(18f, maleRange.second)
        
        val femaleRange = BodyFatCalculator.getIdealRange(User.Gender.FEMALE)
        assertEquals(18f, femaleRange.first)
        assertEquals(25f, femaleRange.second)
    }
    
    @Test
    fun testMeasurementValidation() {
        // Valid measurements
        val validResult = BodyFatCalculator.validateMeasurements(85f, 38f, 180f, 95f)
        assertTrue(validResult.isValid)
        assertNull(validResult.errorMessage)
        
        // Invalid waist (too small)
        val invalidWaist = BodyFatCalculator.validateMeasurements(-5f, 38f, 180f)
        assertFalse(invalidWaist.isValid)
        assertNotNull(invalidWaist.errorMessage)
        
        // Invalid neck (too large)
        val invalidNeck = BodyFatCalculator.validateMeasurements(85f, 150f, 180f)
        assertFalse(invalidNeck.isValid)
        assertNotNull(invalidNeck.errorMessage)
        
        // Waist <= neck (physiologically impossible)
        val waistLessNeck = BodyFatCalculator.validateMeasurements(35f, 40f, 180f)
        assertFalse(waistLessNeck.isValid)
        assertNotNull(waistLessNeck.errorMessage)
        
        // Invalid hips (smaller than waist)
        val invalidHips = BodyFatCalculator.validateMeasurements(95f, 38f, 180f, 85f)
        assertFalse(invalidHips.isValid)
        assertNotNull(invalidHips.errorMessage)
    }
    
    @Test
    fun testEdgeCases() {
        // Test minimum valid measurements
        val minBodyFat = BodyFatCalculator.calculateNavyMethod(
            gender = User.Gender.MALE,
            waistCm = 60f,
            neckCm = 30f,
            heightCm = 150f
        )
        assertTrue("Should handle minimum measurements", minBodyFat in 2f..50f)
        
        // Test maximum realistic measurements
        val maxBodyFat = BodyFatCalculator.calculateNavyMethod(
            gender = User.Gender.MALE,
            waistCm = 150f,
            neckCm = 45f,
            heightCm = 200f
        )
        assertTrue("Should handle maximum measurements", maxBodyFat in 2f..50f)
    }
    
    @Test
    fun testConsistentResults() {
        // Test that same inputs always produce same outputs
        val result1 = BodyFatCalculator.calculateNavyMethod(
            gender = User.Gender.MALE,
            waistCm = 85f,
            neckCm = 38f,
            heightCm = 180f
        )
        
        val result2 = BodyFatCalculator.calculateNavyMethod(
            gender = User.Gender.MALE,
            waistCm = 85f,
            neckCm = 38f,
            heightCm = 180f
        )
        
        assertEquals("Same inputs should produce same results", result1, result2)
    }
    
    @Test
    fun testReasonableResults() {
        // Test that results are within expected physiological ranges
        val testCases = listOf(
            // (gender, waist, neck, height, hips, expected_min, expected_max)
            Triple(User.Gender.MALE, Triple(85f, 38f, 180f), 5f, 35f),
            Triple(User.Gender.FEMALE, Triple(75f, 33f, 165f), 15f, 45f),
            Triple(User.Gender.MALE, Triple(100f, 40f, 175f), 15f, 45f),
            Triple(User.Gender.FEMALE, Triple(90f, 35f, 170f), 20f, 50f)
        )
        
        testCases.forEach { (gender, measurements, minExpected, maxExpected) ->
            val (waist, neck, height) = measurements
            val hips = if (gender == User.Gender.FEMALE) waist + 15f else null
            
            val result = BodyFatCalculator.calculateNavyMethod(gender, waist, neck, height, hips)
            
            assertTrue(
                "Result $result should be between $minExpected and $maxExpected for $gender",
                result in minExpected..maxExpected
            )
        }
    }
}