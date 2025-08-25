package com.progresspal.app.data.repository

import androidx.lifecycle.LiveData
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.progresspal.app.data.api.ApiKeyManager
import com.progresspal.app.data.api.GeminiGoalService
import com.progresspal.app.data.api.models.*
import com.progresspal.app.data.database.dao.GoalDao
import com.progresspal.app.data.database.dao.UserDao
import com.progresspal.app.data.database.dao.WeightDao
import com.progresspal.app.data.database.entities.GoalEntity
import com.progresspal.app.data.database.entities.UserEntity
import com.progresspal.app.data.database.entities.WeightEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

/**
 * Repository for AI-assisted goal management
 * Combines Gemini API calls with local database operations
 */
class AiGoalRepository(
    private val goalDao: GoalDao,
    private val userDao: UserDao,
    private val weightDao: WeightDao,
    private val geminiService: GeminiGoalService,
    private val apiKeyManager: ApiKeyManager,
    private val gson: Gson = Gson()
) {
    
    // LiveData for reactive UI updates
    fun getAiSuggestedGoals(userId: Long): LiveData<List<GoalEntity>> = 
        goalDao.getAiSuggestedGoals(userId)
    
    fun getActiveAiSuggestedGoals(userId: Long): LiveData<List<GoalEntity>> = 
        goalDao.getActiveAiSuggestedGoals(userId)
    
    /**
     * Generate AI-powered goal suggestions based on user profile and history
     */
    suspend fun generateGoalSuggestions(userId: Long, goalType: String): Result<List<GoalEntity>> = 
        withContext(Dispatchers.IO) {
            try {
                // Check API availability and rate limits
                if (!apiKeyManager.canMakeApiRequest()) {
                    return@withContext Result.failure(
                        Exception("AI features disabled or daily limit reached. Remaining: ${apiKeyManager.getRemainingDailyRequests()}")
                    )
                }
                
                val apiKey = apiKeyManager.getGeminiApiKey()
                    ?: return@withContext Result.failure(Exception("Gemini API key not configured"))
                
                // Gather user context
                val user = userDao.getUserByIdSync(userId)
                    ?: return@withContext Result.failure(Exception("User not found"))
                
                val recentWeights = weightDao.getRecentWeightsSync(userId, 10)
                val existingGoals = goalDao.getActiveGoals(userId).value ?: emptyList()
                
                // Build context prompt
                val prompt = buildGoalSuggestionPrompt(user, recentWeights, existingGoals, goalType)
                
                // Create Gemini request
                val request = GeminiRequest(
                    contents = listOf(
                        Content(parts = listOf(Part(text = prompt)))
                    ),
                    generationConfig = GenerationConfig(
                        temperature = 0.7f,
                        maxOutputTokens = 1024,
                        candidateCount = 1
                    )
                )
                
                // Make API call
                val response = geminiService.generateGoalSuggestions(request, apiKey)
                apiKeyManager.incrementApiUsage()
                
                if (response.isSuccessful && response.body() != null) {
                    val suggestions = parseGoalSuggestions(response.body()!!, userId, goalType)
                    Result.success(suggestions)
                } else {
                    Result.failure(Exception("API request failed: ${response.errorBody()?.string()}"))
                }
                
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    
    /**
     * Generate milestones for an existing goal using AI
     */
    suspend fun generateGoalMilestones(goalId: Long): Result<List<String>> = 
        withContext(Dispatchers.IO) {
            try {
                if (!apiKeyManager.canMakeApiRequest()) {
                    return@withContext Result.failure(Exception("Daily API limit reached"))
                }
                
                val apiKey = apiKeyManager.getGeminiApiKey()
                    ?: return@withContext Result.failure(Exception("API key not configured"))
                
                val goal = goalDao.getGoalById(goalId)
                    ?: return@withContext Result.failure(Exception("Goal not found"))
                
                val user = userDao.getUserByIdSync(goal.userId)
                    ?: return@withContext Result.failure(Exception("User not found"))
                
                val prompt = buildMilestonePrompt(goal, user)
                
                val request = GeminiRequest(
                    contents = listOf(Content(parts = listOf(Part(text = prompt))))
                )
                
                val response = geminiService.generateGoalMilestones(request, apiKey)
                apiKeyManager.incrementApiUsage()
                
                if (response.isSuccessful && response.body() != null) {
                    val milestones = parseMilestones(response.body()!!)
                    
                    // Update goal with milestones
                    val updatedGoal = goal.copy(
                        milestones = gson.toJson(milestones),
                        updatedAt = Date()
                    )
                    goalDao.updateGoal(updatedGoal)
                    
                    Result.success(milestones)
                } else {
                    Result.failure(Exception("Milestone generation failed"))
                }
                
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    
    /**
     * Analyze progress and suggest goal adjustments
     */
    suspend fun analyzeProgressAndAdjustGoal(goalId: Long): Result<String> = 
        withContext(Dispatchers.IO) {
            try {
                if (!apiKeyManager.canMakeApiRequest()) {
                    return@withContext Result.failure(Exception("Daily API limit reached"))
                }
                
                val apiKey = apiKeyManager.getGeminiApiKey()
                    ?: return@withContext Result.failure(Exception("API key not configured"))
                
                val goal = goalDao.getGoalById(goalId)
                    ?: return@withContext Result.failure(Exception("Goal not found"))
                
                val user = userDao.getUserByIdSync(goal.userId)
                    ?: return@withContext Result.failure(Exception("User not found"))
                
                val recentWeights = weightDao.getRecentWeightsSync(goal.userId, 30)
                
                val prompt = buildProgressAnalysisPrompt(goal, user, recentWeights)
                
                val request = GeminiRequest(
                    contents = listOf(Content(parts = listOf(Part(text = prompt))))
                )
                
                val response = geminiService.analyzeProgressAndAdjustGoal(request, apiKey)
                apiKeyManager.incrementApiUsage()
                
                if (response.isSuccessful && response.body() != null) {
                    val analysis = parseProgressAnalysis(response.body()!!)
                    Result.success(analysis)
                } else {
                    Result.failure(Exception("Progress analysis failed"))
                }
                
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    
    /**
     * Get AI goal statistics for user dashboard
     */
    suspend fun getAiGoalStats(userId: Long): AiGoalStats = withContext(Dispatchers.IO) {
        val achievedCount = goalDao.getAchievedAiGoalCount(userId)
        val averageDifficulty = goalDao.getAverageAchievedGoalDifficulty(userId) ?: 0f
        val remainingRequests = apiKeyManager.getRemainingDailyRequests()
        val todayUsage = apiKeyManager.getTodayApiUsageCount()
        
        AiGoalStats(
            achievedAiGoals = achievedCount,
            averageDifficulty = averageDifficulty,
            remainingDailyRequests = remainingRequests,
            todayApiUsage = todayUsage,
            aiEnabled = apiKeyManager.isAiEnabled()
        )
    }
    
    // Helper functions for prompt building and response parsing
    
    private fun buildGoalSuggestionPrompt(
        user: UserEntity,
        weights: List<WeightEntity>,
        existingGoals: List<GoalEntity>,
        goalType: String
    ): String {
        return """
        Generate personalized fitness goals for a user based on their profile and history.
        
        User Profile:
        - Age: ${calculateAge(user.birthDate)}
        - Gender: ${user.gender}
        - Height: ${user.height} cm
        - Current Weight: ${weights.firstOrNull()?.weight ?: user.currentWeight} kg
        - Activity Level: ${user.activityLevel ?: "moderate"}
        - Goal Type: $goalType
        
        Recent Weight History (last 10 entries):
        ${weights.joinToString("\n") { "- ${it.weight} kg on ${it.date}" }}
        
        Existing Goals:
        ${existingGoals.joinToString("\n") { "- ${it.goalType}: ${it.currentValue} → ${it.targetValue}" }}
        
        Please suggest 2-3 SMART goals for $goalType with:
        1. Specific target values
        2. Realistic timeframes (2-16 weeks)
        3. Difficulty assessment (1-10 scale)
        4. Brief reasoning for each suggestion
        5. Key milestones to track progress
        
        Return response in JSON format:
        {
          "suggestions": [
            {
              "targetValue": number,
              "timeframeWeeks": number,
              "difficulty": number,
              "reasoning": "string",
              "milestones": ["string"],
              "confidence": number (0.0-1.0)
            }
          ]
        }
        """.trimIndent()
    }
    
    private fun buildMilestonePrompt(goal: GoalEntity, user: UserEntity): String {
        return """
        Generate progressive milestones for this fitness goal:
        
        Goal: ${goal.goalType}
        Current: ${goal.currentValue}
        Target: ${goal.targetValue}
        Target Date: ${goal.targetDate}
        
        User: ${user.gender}, Age ${calculateAge(user.birthDate)}
        
        Create 4-6 intermediate milestones with specific targets and approximate dates.
        Return as JSON array of strings: ["Week 2: Milestone 1", "Week 4: Milestone 2", ...]
        """.trimIndent()
    }
    
    private fun buildProgressAnalysisPrompt(
        goal: GoalEntity,
        user: UserEntity,
        weights: List<WeightEntity>
    ): String {
        return """
        Analyze progress toward this goal and provide recommendations:
        
        Goal: ${goal.goalType}
        Current: ${goal.currentValue} → Target: ${goal.targetValue}
        Created: ${goal.createdAt}
        Target Date: ${goal.targetDate}
        
        Recent Progress:
        ${weights.take(10).joinToString("\n") { "- ${it.weight} kg on ${it.date}" }}
        
        Provide:
        1. Progress assessment (on track, ahead, behind)
        2. Recommendations for adjustments
        3. Motivation and encouragement
        4. Suggested timeline modifications if needed
        
        Keep response under 200 words.
        """.trimIndent()
    }
    
    private fun parseGoalSuggestions(response: GeminiResponse, userId: Long, goalType: String): List<GoalEntity> {
        try {
            val text = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: return emptyList()
            
            // Extract JSON from response text (handle markdown formatting)
            val jsonStart = text.indexOf("{")
            val jsonEnd = text.lastIndexOf("}") + 1
            if (jsonStart == -1 || jsonEnd <= jsonStart) return emptyList()
            
            val jsonText = text.substring(jsonStart, jsonEnd)
            val suggestionsResponse = gson.fromJson(jsonText, GoalSuggestionsResponse::class.java)
            
            return suggestionsResponse.suggestions.map { suggestion ->
                GoalEntity(
                    userId = userId,
                    goalType = goalType,
                    currentValue = 0f, // To be set by caller
                    targetValue = suggestion.targetValue,
                    targetDate = calculateTargetDate(suggestion.timeframeWeeks),
                    aiSuggested = true,
                    aiReasoning = suggestion.reasoning,
                    difficultyScore = suggestion.difficulty,
                    aiConfidenceScore = suggestion.confidence,
                    milestones = gson.toJson(suggestion.milestones)
                )
            }
        } catch (e: JsonSyntaxException) {
            return emptyList()
        }
    }
    
    private fun parseMilestones(response: GeminiResponse): List<String> {
        try {
            val text = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: return emptyList()
            
            return gson.fromJson(text, Array<String>::class.java).toList()
        } catch (e: JsonSyntaxException) {
            return emptyList()
        }
    }
    
    private fun parseProgressAnalysis(response: GeminiResponse): String {
        return response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            ?: "Unable to generate progress analysis"
    }
    
    private fun calculateAge(birthDate: Date?): Int {
        if (birthDate == null) return 30 // Default age
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        calendar.time = birthDate
        val birthYear = calendar.get(Calendar.YEAR)
        return currentYear - birthYear
    }
    
    private fun calculateTargetDate(weeksFromNow: Int): Date {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.WEEK_OF_YEAR, weeksFromNow)
        return calendar.time
    }
}

// Data classes for API responses
data class GoalSuggestionsResponse(
    val suggestions: List<GoalSuggestion>
)

data class GoalSuggestion(
    val targetValue: Float,
    val timeframeWeeks: Int,
    val difficulty: Int,
    val reasoning: String,
    val milestones: List<String>,
    val confidence: Float
)

data class AiGoalStats(
    val achievedAiGoals: Int,
    val averageDifficulty: Float,
    val remainingDailyRequests: Int,
    val todayApiUsage: Int,
    val aiEnabled: Boolean
)