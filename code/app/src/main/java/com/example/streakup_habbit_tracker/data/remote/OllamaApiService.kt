package com.example.streakup_habbit_tracker.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface OllamaApiService {
    @POST("api/generate")
    suspend fun generateInsights(@Body request: OllamaRequest): Response<OllamaResponse>
}
