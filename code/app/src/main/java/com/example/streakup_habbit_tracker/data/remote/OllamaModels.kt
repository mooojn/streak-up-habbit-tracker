package com.example.streakup_habbit_tracker.data.remote

import com.google.gson.annotations.SerializedName

data class OllamaRequest(
    @SerializedName("model") val model: String = "qwen2.5-coder:7b",
    @SerializedName("prompt") val prompt: String,
    @SerializedName("prompt_type") val promptType: String? = null,
    @SerializedName("stream") val stream: Boolean = false
)

data class OllamaResponse(
    @SerializedName("model") val model: String?,
    @SerializedName("response") val response: String?
)

data class OllamaMessage(
    @SerializedName("role") val role: String,
    @SerializedName("content") val content: String
)

data class OllamaChatRequest(
    @SerializedName("model") val model: String = "qwen2.5-coder:7b",
    @SerializedName("messages") val messages: List<OllamaMessage>,
    @SerializedName("stream") val stream: Boolean = false
)

data class OllamaChatResponse(
    @SerializedName("model") val model: String?,
    @SerializedName("message") val message: OllamaMessage?
)

data class VoiceCreateDraft(
    val type: String,
    val title: String,
    val details: String,
    val isFlexible: Boolean = false,
    val targetValue: Int = 1,
    val unit: String = ""
)

data class HabitBreakdown(
    val title: String,
    val description: String
)
