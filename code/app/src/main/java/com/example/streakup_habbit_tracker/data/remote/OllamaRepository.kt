package com.example.streakup_habbit_tracker.data.remote

import com.example.streakup_habbit_tracker.data.HabitRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object OllamaRepository {

    private fun getApiService(): OllamaApiService? {
        val baseUrl = HabitRepository.ngrokUrl.trim()
        if (baseUrl.isEmpty()) return null

        val safeUrl = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"

        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("ngrok-skip-browser-warning", "true")
                    .build()
                chain.proceed(request)
            }
            .readTimeout(60, TimeUnit.SECONDS)
            .connectTimeout(60, TimeUnit.SECONDS)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(safeUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return retrofit.create(OllamaApiService::class.java)
    }

    suspend fun getHabitSpecificInsight(habit: com.example.streakup_habbit_tracker.data.Habit): String = withContext(Dispatchers.IO) {
        val apiService = getApiService()
            ?: return@withContext "Error: Ngrok URL is not set."

        val completedToday = if (HabitRepository.hasCompletedToday(habit)) "Yes" else "No"
        val prompt = """
            I have a habit called "${habit.title}".
            My current streak is ${habit.streakCount} days.
            Did I complete it today? $completedToday
            
            Give me a short, 1-2 sentence personalized tip or word of encouragement to keep my streak going or improve my consistency.
        """.trimIndent()

        try {
            val response = apiService.generateInsights(OllamaRequest(prompt = prompt))
            if (response.isSuccessful && response.body() != null) {
                response.body()?.response ?: "Received empty response from AI."
            } else {
                "Error communicating with AI: ${response.code()} ${response.message()}"
            }
        } catch (e: Exception) {
            "Network error: ${e.message}"
        }
    }

    suspend fun sendChatMessage(messages: List<OllamaMessage>): OllamaMessage? = withContext(Dispatchers.IO) {
        val apiService = getApiService() ?: return@withContext OllamaMessage("assistant", "Error: Ngrok URL is not set.")
        try {
            val response = apiService.chat(OllamaChatRequest(messages = messages))
            if (response.isSuccessful && response.body() != null) {
                response.body()?.message
            } else {
                OllamaMessage("assistant", "Error communicating with AI: ${response.code()} ${response.message()}")
            }
        } catch (e: Exception) {
            OllamaMessage("assistant", "Network error: ${e.message}")
        }
    }
}
