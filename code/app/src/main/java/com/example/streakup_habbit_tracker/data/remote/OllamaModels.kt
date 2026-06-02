package com.example.streakup_habbit_tracker.data.remote

import com.google.gson.annotations.SerializedName

data class OllamaRequest(
    @SerializedName("model") val model: String = "qwen-coder:7b",
    @SerializedName("prompt") val prompt: String,
    @SerializedName("stream") val stream: Boolean = false
)

data class OllamaResponse(
    @SerializedName("model") val model: String?,
    @SerializedName("response") val response: String?
)
