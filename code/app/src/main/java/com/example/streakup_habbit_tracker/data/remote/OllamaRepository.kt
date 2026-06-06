package com.example.streakup_habbit_tracker.data.remote

import com.example.streakup_habbit_tracker.data.HabitRepository
import com.example.streakup_habbit_tracker.data.Note
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.OkHttpClient
import org.json.JSONObject
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

    suspend fun getNoteHelp(note: Note): String = withContext(Dispatchers.IO) {
        val apiService = getApiService()
            ?: return@withContext "Error: Ngrok URL is not set."

        val prompt = """
            I wrote this note:
            Title: "${note.title}"
            Body: "${note.body}"

            Help me improve or act on it. Keep the answer short: give 2-3 useful suggestions, a clearer version, or next steps depending on what the note needs.
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

    fun sendChatStream(messages: List<OllamaMessage>): Flow<String> = flow {
        val apiService = getApiService()
        if (apiService == null) {
            emit("Error: Ngrok URL is not set.")
            return@flow
        }
        try {
            val response = apiService.chatStream(OllamaChatRequest(messages = messages, stream = true))
            if (!response.isSuccessful) {
                emit("Error communicating with AI: ${response.code()} ${response.message()}")
                return@flow
            }
            response.body()?.byteStream()?.bufferedReader()?.use { reader ->
                var line: String? = reader.readLine()
                while (line != null) {
                    if (line.isNotBlank()) {
                        try {
                            val json = org.json.JSONObject(line)
                            var content = json.optString("content", "")
                            if (content.isEmpty() && json.has("message")) {
                                content = json.getJSONObject("message").optString("content", "")
                            }
                            if (content.isNotEmpty()) {
                                emit(content)
                            }
                        } catch (e: Exception) {
                            // ignore parse error for incomplete chunks
                        }
                    }
                    line = reader.readLine()
                }
            }
        } catch (e: Exception) {
            emit("\n[Network error: ${e.message}]")
        }
    }.flowOn(Dispatchers.IO)

    suspend fun createDraftFromVoice(transcript: String): VoiceCreateDraft = withContext(Dispatchers.IO) {
        val apiService = getApiService()
            ?: throw IllegalStateException("Ngrok URL is not set.")

        val prompt = """
            Convert this spoken request into one StreakUp create action.

            Spoken request:
            "$transcript"

            Return only valid JSON with exactly these keys:
            {
              "type": "habit" or "note",
              "title": "short title",
              "details": "short note/body text, empty string if none",
              "isFlexible": true or false,
              "targetValue": integer daily target, default 1,
              "unit": "unit for flexible habits, empty string if none"
            }

            Use type "habit" when the user wants to track a recurring action.
            Use type "note" when the user wants to save an idea, reminder, journal entry, or one-time note.
            For flexible habits like water, pages, steps, prayers, minutes, or workouts with a number, set isFlexible true and fill targetValue and unit.
        """.trimIndent()

        val response = apiService.generateInsights(OllamaRequest(prompt = prompt))
        if (!response.isSuccessful || response.body() == null) {
            throw IllegalStateException("Error communicating with AI: ${response.code()} ${response.message()}")
        }

        val rawResponse = response.body()?.response.orEmpty()
        val json = JSONObject(extractJsonObject(rawResponse))
        val type = json.optString("type", "habit").lowercase().let {
            if (it == "note") "note" else "habit"
        }
        val title = json.optString("title", "").trim()
        if (title.isBlank()) {
            throw IllegalStateException("AI could not find a title in the voice request.")
        }

        VoiceCreateDraft(
            type = type,
            title = title,
            details = json.optString("details", "").trim(),
            isFlexible = type == "habit" && json.optBoolean("isFlexible", false),
            targetValue = json.optInt("targetValue", 1).coerceAtLeast(1),
            unit = json.optString("unit", "").trim()
        )
    }

    private fun extractJsonObject(text: String): String {
        val cleaned = text
            .replace("```json", "", ignoreCase = true)
            .replace("```", "")
            .trim()
        val start = cleaned.indexOf('{')
        val end = cleaned.lastIndexOf('}')
        if (start == -1 || end == -1 || end <= start) {
            throw IllegalStateException("AI did not return a valid create draft.")
        }
        return cleaned.substring(start, end + 1)
    }

    /**
     * Sends a broad goal to the AI and gets back 3-5 specific, measurable micro-habits with descriptions.
     * Returns a list of HabitBreakdown, or throws on failure.
     */
    suspend fun breakdownHabit(goal: String): List<HabitBreakdown> = withContext(Dispatchers.IO) {
        val apiService = getApiService()
            ?: throw IllegalStateException("Ngrok URL is not set. Go to Profile → Settings to set the server URL.")

        val response = apiService.generateInsights(OllamaRequest(prompt = goal, promptType = "breakdown"))
        if (!response.isSuccessful || response.body() == null) {
            throw IllegalStateException("Error from AI: ${response.code()} ${response.message()}")
        }

        val raw = response.body()?.response.orEmpty()
        // Extract JSON array from the response
        val cleaned = raw.replace("```json", "", ignoreCase = true).replace("```", "").trim()
        val start = cleaned.indexOf('[')
        val end = cleaned.lastIndexOf(']')
        if (start == -1 || end == -1 || end <= start) {
            throw IllegalStateException("AI returned an unexpected format. Please try again.")
        }
        val jsonArrayStr = cleaned.substring(start, end + 1)
        val arr = org.json.JSONArray(jsonArrayStr)
        (0 until arr.length()).map { 
            val obj = arr.getJSONObject(it)
            HabitBreakdown(
                title = obj.optString("title", "").trim(),
                description = obj.optString("description", "").trim()
            )
        }.filter { it.title.isNotBlank() }
    }
}

