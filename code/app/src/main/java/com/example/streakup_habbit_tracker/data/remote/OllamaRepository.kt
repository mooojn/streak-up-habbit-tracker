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

    suspend fun getInsights(): String = withContext(Dispatchers.IO) {
        val apiService = getApiService()
            ?: return@withContext "Error: Ngrok URL is not set. Please set it in the Profile settings."

        val habits = HabitRepository.getHabits()
        if (habits.isEmpty()) {
            return@withContext "You don't have any habits yet. Add some habits and start building your streak!"
        }

        val habitsInfo = habits.joinToString(separator = "\n") { habit ->
            "- ${habit.title}: Streak of ${habit.streakCount} days. Completed today? ${if (HabitRepository.hasCompletedToday(habit)) "Yes" else "No"}"
        }

        val prompt = """
            You are a helpful and encouraging habit coach.
            Here are my current habits and streaks:
            ${habitsInfo}
            
            Please analyze my habits and give me a short, encouraging insight and self-improvement advice in 2-3 sentences.
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
}
