package com.progresspal.app.data.api.models

import com.google.gson.annotations.SerializedName

data class GeminiResponse(
    @SerializedName("candidates")
    val candidates: List<Candidate>?,
    @SerializedName("promptFeedback")
    val promptFeedback: PromptFeedback?,
    @SerializedName("usageMetadata")
    val usageMetadata: UsageMetadata?
)

data class Candidate(
    @SerializedName("content")
    val content: ResponseContent?,
    @SerializedName("finishReason")
    val finishReason: String?,
    @SerializedName("index")
    val index: Int?,
    @SerializedName("safetyRatings")
    val safetyRatings: List<SafetyRating>?
)

data class ResponseContent(
    @SerializedName("parts")
    val parts: List<ResponsePart>?,
    @SerializedName("role")
    val role: String?
)

data class ResponsePart(
    @SerializedName("text")
    val text: String?
)

data class SafetyRating(
    @SerializedName("category")
    val category: String?,
    @SerializedName("probability")
    val probability: String?
)

data class PromptFeedback(
    @SerializedName("safetyRatings")
    val safetyRatings: List<SafetyRating>?
)

data class UsageMetadata(
    @SerializedName("promptTokenCount")
    val promptTokenCount: Int?,
    @SerializedName("candidatesTokenCount")
    val candidatesTokenCount: Int?,
    @SerializedName("totalTokenCount")
    val totalTokenCount: Int?
)