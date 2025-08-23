package com.progresspal.app.utils

import com.progresspal.app.data.database.entities.BloodPressureCategory
import com.progresspal.app.domain.models.MedicalGuidelines
import com.progresspal.app.domain.models.ActivityLevel
import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for enhanced BloodPressureUtils
 * Tests multi-standard support and age-adjusted assessments
 */
class BloodPressureUtilsTest {
    
    @Test
    fun testAHAGuidelines_StandardThresholds() {
        // Test US (AHA) guidelines - same thresholds for all ages
        
        // Young adult (25)
        val youngAdult = BloodPressureUtils.getAgeAdjustedCategory(
            systolic = 140,
            diastolic = 90,
            age = 25,
            guidelines = MedicalGuidelines.US_AHA
        )
        assertEquals(BloodPressureCategory.STAGE_2, youngAdult.category)
        assertFalse(youngAdult.isAgeAdjusted)
        
        // Elderly (75) - same thresholds
        val elderly = BloodPressureUtils.getAgeAdjustedCategory(
            systolic = 140,
            diastolic = 90,
            age = 75,
            guidelines = MedicalGuidelines.US_AHA
        )
        assertEquals(BloodPressureCategory.STAGE_2, elderly.category)
        assertFalse(elderly.isAgeAdjusted)
    }
    
    @Test
    fun testESCGuidelines_AgeAdjustedThresholds() {
        // Test EU (ESC) guidelines - age-adjusted thresholds
        
        // Young adult (30): 140/90 = Stage 1
        val youngAdult = BloodPressureUtils.getAgeAdjustedCategory(
            systolic = 140,
            diastolic = 90,
            age = 30,
            guidelines = MedicalGuidelines.EU_ESC
        )
        assertEquals(BloodPressureCategory.STAGE_1, youngAdult.category)
        assertFalse(youngAdult.isAgeAdjusted) // Not age-adjusted for <65
        
        // Elderly (70): 140/90 = Stage 1 (more lenient)
        val elderly = BloodPressureUtils.getAgeAdjustedCategory(
            systolic = 140,
            diastolic = 90,
            age = 70,
            guidelines = MedicalGuidelines.EU_ESC
        )
        assertEquals(BloodPressureCategory.STAGE_1, elderly.category)
        assertTrue(elderly.isAgeAdjusted) // Age-adjusted for 65+
        
        // Very elderly (85): 150/95 = Stage 1 (very lenient)
        val veryElderly = BloodPressureUtils.getAgeAdjustedCategory(
            systolic = 150,
            diastolic = 95,
            age = 85,
            guidelines = MedicalGuidelines.EU_ESC
        )
        assertEquals(BloodPressureCategory.STAGE_1, veryElderly.category)
        assertTrue(veryElderly.isAgeAdjusted)
    }
    
    @Test
    fun testOptimalReadings() {
        // Test optimal readings across both guidelines
        
        val ahaOptimal = BloodPressureUtils.getAgeAdjustedCategory(
            systolic = 110,
            diastolic = 70,
            age = 45,
            guidelines = MedicalGuidelines.US_AHA
        )
        assertEquals(BloodPressureCategory.OPTIMAL, ahaOptimal.category)
        
        val escOptimal = BloodPressureUtils.getAgeAdjustedCategory(
            systolic = 110,
            diastolic = 65,
            age = 45,
            guidelines = MedicalGuidelines.EU_ESC
        )
        assertEquals(BloodPressureCategory.OPTIMAL, escOptimal.category)
    }
    
    @Test
    fun testCrisisReadings() {
        // Crisis readings should be same across guidelines
        
        val ahaCrisis = BloodPressureUtils.getAgeAdjustedCategory(
            systolic = 190,
            diastolic = 125,
            age = 50,
            guidelines = MedicalGuidelines.US_AHA
        )
        assertEquals(BloodPressureCategory.CRISIS, ahaCrisis.category)
        
        val escCrisis = BloodPressureUtils.getAgeAdjustedCategory(
            systolic = 190,
            diastolic = 115,
            age = 50,
            guidelines = MedicalGuidelines.EU_ESC
        )
        assertEquals(BloodPressureCategory.CRISIS, escCrisis.category)
    }
    
    @Test
    fun testAgeCalculation() {
        // Test age calculation from birth date
        val calendar = java.util.Calendar.getInstance()
        
        // Birth date 30 years ago
        calendar.add(java.util.Calendar.YEAR, -30)
        val birthDate30 = calendar.time
        
        val age30 = BloodPressureUtils.calculateAge(birthDate30)
        assertEquals(30, age30)
        
        // Birth date 65 years ago
        calendar.time = java.util.Date()
        calendar.add(java.util.Calendar.YEAR, -65)
        val birthDate65 = calendar.time
        
        val age65 = BloodPressureUtils.calculateAge(birthDate65)
        assertEquals(65, age65)
    }
    
    @Test
    fun testGuidelinesDisplayString() {
        // Test guidelines display formatting
        
        val ahaDisplay = BloodPressureUtils.getGuidelinesDisplayString(
            MedicalGuidelines.US_AHA, 
            45
        )
        assertEquals("US (AHA)", ahaDisplay)
        
        val escYoungDisplay = BloodPressureUtils.getGuidelinesDisplayString(
            MedicalGuidelines.EU_ESC, 
            45
        )
        assertEquals("EU (ESC)", escYoungDisplay)
        
        val escElderlyDisplay = BloodPressureUtils.getGuidelinesDisplayString(
            MedicalGuidelines.EU_ESC, 
            70
        )
        assertEquals("EU (ESC) • 65+ adjusted", escElderlyDisplay)
        
        val escVeryElderlyDisplay = BloodPressureUtils.getGuidelinesDisplayString(
            MedicalGuidelines.EU_ESC, 
            85
        )
        assertEquals("EU (ESC) • 80+ adjusted", escVeryElderlyDisplay)
    }
    
    @Test
    fun testImmediateAttentionLogic() {
        // Test immediate attention detection
        
        // Crisis BP should require immediate attention
        assertTrue(
            BloodPressureUtils.requiresImmediateAttention(
                systolic = 190,
                diastolic = 125,
                pulse = 80,
                age = 50,
                guidelines = MedicalGuidelines.US_AHA
            )
        )
        
        // Very low pulse should require attention
        assertTrue(
            BloodPressureUtils.requiresImmediateAttention(
                systolic = 120,
                diastolic = 80,
                pulse = 45,
                age = 50,
                guidelines = MedicalGuidelines.US_AHA
            )
        )
        
        // Very high pulse should require attention
        assertTrue(
            BloodPressureUtils.requiresImmediateAttention(
                systolic = 120,
                diastolic = 80,
                pulse = 130,
                age = 50,
                guidelines = MedicalGuidelines.US_AHA
            )
        )
        
        // Normal readings should not require immediate attention
        assertFalse(
            BloodPressureUtils.requiresImmediateAttention(
                systolic = 120,
                diastolic = 80,
                pulse = 75,
                age = 50,
                guidelines = MedicalGuidelines.US_AHA
            )
        )
    }
}