package com.progresspal.app.data.api

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

/**
 * Secure API key management for external service credentials
 * Uses Android's EncryptedSharedPreferences for secure storage
 */
class ApiKeyManager(private val context: Context) {
    
    companion object {
        private const val ENCRYPTED_PREFS_FILE = "secure_api_keys"
        private const val KEY_GEMINI_API_KEY = "gemini_api_key"
        private const val KEY_API_USAGE_COUNT = "api_usage_count"
        private const val KEY_LAST_RESET_DATE = "last_reset_date"
        private const val KEY_API_ENABLED = "ai_features_enabled"
        
        // Rate limiting constants
        private const val DAILY_REQUEST_LIMIT = 1000 // Free tier limit
        private const val MILLISECONDS_PER_DAY = 24 * 60 * 60 * 1000L
    }
    
    private val encryptedPreferences: SharedPreferences by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
            
        EncryptedSharedPreferences.create(
            context,
            ENCRYPTED_PREFS_FILE,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }
    
    /**
     * Stores the Gemini API key securely
     */
    fun setGeminiApiKey(apiKey: String) {
        encryptedPreferences.edit()
            .putString(KEY_GEMINI_API_KEY, apiKey)
            .putBoolean(KEY_API_ENABLED, apiKey.isNotBlank())
            .apply()
    }
    
    /**
     * Retrieves the Gemini API key
     */
    fun getGeminiApiKey(): String? {
        return encryptedPreferences.getString(KEY_GEMINI_API_KEY, null)
    }
    
    /**
     * Checks if AI features are enabled (API key is set)
     */
    fun isAiEnabled(): Boolean {
        return encryptedPreferences.getBoolean(KEY_API_ENABLED, false) && 
               !getGeminiApiKey().isNullOrBlank()
    }
    
    /**
     * Enables or disables AI features
     */
    fun setAiEnabled(enabled: Boolean) {
        encryptedPreferences.edit()
            .putBoolean(KEY_API_ENABLED, enabled)
            .apply()
    }
    
    /**
     * Clears the stored API key
     */
    fun clearApiKey() {
        encryptedPreferences.edit()
            .remove(KEY_GEMINI_API_KEY)
            .putBoolean(KEY_API_ENABLED, false)
            .apply()
    }
    
    /**
     * Rate limiting: Check if daily request limit has been reached
     */
    fun canMakeApiRequest(): Boolean {
        if (!isAiEnabled()) return false
        
        resetDailyCountIfNeeded()
        val currentCount = encryptedPreferences.getInt(KEY_API_USAGE_COUNT, 0)
        return currentCount < DAILY_REQUEST_LIMIT
    }
    
    /**
     * Increment API usage counter
     */
    fun incrementApiUsage() {
        resetDailyCountIfNeeded()
        val currentCount = encryptedPreferences.getInt(KEY_API_USAGE_COUNT, 0)
        encryptedPreferences.edit()
            .putInt(KEY_API_USAGE_COUNT, currentCount + 1)
            .apply()
    }
    
    /**
     * Get remaining API requests for today
     */
    fun getRemainingDailyRequests(): Int {
        if (!isAiEnabled()) return 0
        
        resetDailyCountIfNeeded()
        val currentCount = encryptedPreferences.getInt(KEY_API_USAGE_COUNT, 0)
        return maxOf(0, DAILY_REQUEST_LIMIT - currentCount)
    }
    
    /**
     * Get current API usage count for today
     */
    fun getTodayApiUsageCount(): Int {
        resetDailyCountIfNeeded()
        return encryptedPreferences.getInt(KEY_API_USAGE_COUNT, 0)
    }
    
    /**
     * Reset daily usage counter if a new day has started
     */
    private fun resetDailyCountIfNeeded() {
        val currentTime = System.currentTimeMillis()
        val lastResetTime = encryptedPreferences.getLong(KEY_LAST_RESET_DATE, 0)
        
        if (currentTime - lastResetTime >= MILLISECONDS_PER_DAY) {
            encryptedPreferences.edit()
                .putInt(KEY_API_USAGE_COUNT, 0)
                .putLong(KEY_LAST_RESET_DATE, currentTime)
                .apply()
        }
    }
    
    /**
     * Validates API key format (basic validation)
     */
    fun isValidApiKeyFormat(apiKey: String): Boolean {
        return apiKey.isNotBlank() && 
               apiKey.length >= 30 && 
               apiKey.matches(Regex("^[A-Za-z0-9_-]+$"))
    }
}