package com.progresspal.app.data.api

import com.progresspal.app.data.api.models.GeminiRequest
import com.progresspal.app.data.api.models.GeminiResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

interface GeminiGoalService {
    
    @POST("v1beta/models/gemini-1.5-flash:generateContent")
    suspend fun generateGoalSuggestions(
        @Body request: GeminiRequest,
        @Query("key") apiKey: String
    ): Response<GeminiResponse>
    
    @POST("v1beta/models/gemini-1.5-flash:generateContent")
    suspend fun generateGoalMilestones(
        @Body request: GeminiRequest,
        @Query("key") apiKey: String
    ): Response<GeminiResponse>
    
    @POST("v1beta/models/gemini-1.5-flash:generateContent")
    suspend fun analyzeProgressAndAdjustGoal(
        @Body request: GeminiRequest,
        @Query("key") apiKey: String
    ): Response<GeminiResponse>
}