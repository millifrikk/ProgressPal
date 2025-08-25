package com.progresspal.app.data.api.models

import com.google.gson.annotations.SerializedName

data class GeminiRequest(
    @SerializedName("contents")
    val contents: List<Content>,
    @SerializedName("generationConfig")
    val generationConfig: GenerationConfig? = null,
    @SerializedName("safetySettings")
    val safetySettings: List<SafetySetting>? = null
)

data class Content(
    @SerializedName("parts")
    val parts: List<Part>
)

data class Part(
    @SerializedName("text")
    val text: String
)

data class GenerationConfig(
    @SerializedName("temperature")
    val temperature: Float = 0.7f,
    @SerializedName("topK")
    val topK: Int = 40,
    @SerializedName("topP")
    val topP: Float = 0.95f,
    @SerializedName("maxOutputTokens")
    val maxOutputTokens: Int = 2048,
    @SerializedName("candidateCount")
    val candidateCount: Int = 1
)

data class SafetySetting(
    @SerializedName("category")
    val category: String,
    @SerializedName("threshold")
    val threshold: String
)