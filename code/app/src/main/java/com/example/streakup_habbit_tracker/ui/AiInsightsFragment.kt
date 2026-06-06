package com.example.streakup_habbit_tracker.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.streakup_habbit_tracker.R
import com.example.streakup_habbit_tracker.data.HabitRepository
import com.example.streakup_habbit_tracker.data.remote.OllamaMessage
import com.example.streakup_habbit_tracker.data.remote.OllamaRepository
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

class AiInsightsFragment : Fragment() {

    private lateinit var chatRecyclerView: RecyclerView
    private lateinit var chatInputEditText: EditText
    private lateinit var chatSendButton: MaterialButton
    private lateinit var clearChatButton: MaterialButton
    private lateinit var quickPromptsContainer: LinearLayout
    private lateinit var chatAdapter: ChatAdapter

    private val conversationHistory = mutableListOf<OllamaMessage>()

    companion object {
        private const val PREFS_NAME = "coach_prefs"
        private const val KEY_HISTORY = "chat_history"
        private const val MAX_HISTORY = 40

        private val QUICK_PROMPTS = listOf(
            "💪 How am I doing?",
            "😓 I missed a habit today",
            "🚀 Motivate me!",
            "📋 Give me tips",
            "🔥 Build a new habit"
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_ai_insights, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        chatRecyclerView = view.findViewById(R.id.chatRecyclerView)
        chatInputEditText = view.findViewById(R.id.chatInputEditText)
        chatSendButton = view.findViewById(R.id.chatSendButton)
        clearChatButton = view.findViewById(R.id.clearChatButton)
        quickPromptsContainer = view.findViewById(R.id.quickPromptsContainer)

        chatAdapter = ChatAdapter()
        val layoutManager = LinearLayoutManager(requireContext())
        layoutManager.stackFromEnd = true
        chatRecyclerView.layoutManager = layoutManager
        chatRecyclerView.adapter = chatAdapter

        setupQuickPrompts()
        loadConversationHistory()
        setupListeners()
    }

    private fun setupQuickPrompts() {
        QUICK_PROMPTS.forEach { prompt ->
            val chip = TextView(requireContext()).apply {
                text = prompt
                textSize = 13f
                setTextColor(resources.getColor(R.color.brand_primary, null))
                setBackgroundResource(R.drawable.bg_quick_prompt_chip)
                setPadding(32, 16, 32, 16)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { setMargins(0, 0, 12, 0) }
                setOnClickListener {
                    val cleanPrompt = prompt.substring(2).trim()  // strip emoji
                    chatInputEditText.setText(cleanPrompt)
                    sendMessage(cleanPrompt)
                }
            }
            quickPromptsContainer.addView(chip)
        }
    }

    private fun setupListeners() {
        chatSendButton.setOnClickListener {
            val text = chatInputEditText.text.toString().trim()
            if (text.isNotEmpty()) sendMessage(text)
        }

        chatInputEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                val text = chatInputEditText.text.toString().trim()
                if (text.isNotEmpty()) sendMessage(text)
                true
            } else false
        }

        clearChatButton.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Clear conversation?")
                .setMessage("This will reset the chat and start fresh with the coach.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Clear") { _, _ ->
                    clearHistory()
                }
                .show()
        }
    }

    private fun buildSystemPrompt(): String {
        val habits = HabitRepository.getHabits()
        val userName = HabitRepository.userName.ifBlank { "friend" }

        val habitsInfo = if (habits.isEmpty()) {
            "The user hasn't created any habits yet."
        } else {
            habits.joinToString(separator = "\n") { habit ->
                val status = if (HabitRepository.hasCompletedToday(habit)) "✅ completed today" else "❌ not done today"
                "- ${habit.title}: ${habit.streakCount}-day streak, $status"
            }
        }

        return """
You are an expert AI Accountability Coach inside the StreakUp habit tracking app. You act as a warm, motivating, and science-backed accountability partner.

The user's name is: $userName

Their current habit data:
$habitsInfo

Your coaching principles:
1. Be concise — keep replies to 2-4 sentences unless giving a detailed plan.
2. Be warm and encouraging, never preachy or judgmental.
3. Use the user's habit data when relevant to personalize your answers.
4. If they've missed a habit, help them get back on track gently.
5. Celebrate their streaks genuinely.
6. Suggest practical, small action steps based on behavioral science.

Always respond in plain text without markdown symbols.
        """.trimIndent()
    }

    private fun loadConversationHistory() {
        val prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_HISTORY, null)

        conversationHistory.clear()
        conversationHistory.add(OllamaMessage(role = "system", content = buildSystemPrompt()))

        if (json != null) {
            try {
                val arr = JSONArray(json)
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    conversationHistory.add(
                        OllamaMessage(role = obj.getString("role"), content = obj.getString("content"))
                    )
                }
            } catch (_: Exception) { }
        }

        if (conversationHistory.size <= 1) {
            // First launch — add welcome message
            conversationHistory.add(
                OllamaMessage(
                    role = "assistant",
                    content = "Hey${if (HabitRepository.userName.isNotBlank()) ", ${HabitRepository.userName}" else ""}! 👋 I'm your Accountability Coach. I know your habits and I'm here to help you stay consistent, beat procrastination, and build a life you're proud of. What's on your mind today?"
                )
            )
            saveHistory()
        }

        chatAdapter.setMessages(conversationHistory)
        scrollToBottom()
    }

    private fun saveHistory() {
        val prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val toSave = conversationHistory.filter { it.role != "system" }
            .takeLast(MAX_HISTORY)
        val arr = JSONArray()
        toSave.forEach { msg ->
            arr.put(JSONObject().apply {
                put("role", msg.role)
                put("content", msg.content)
            })
        }
        prefs.edit().putString(KEY_HISTORY, arr.toString()).apply()
    }

    private fun clearHistory() {
        val prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove(KEY_HISTORY).apply()
        loadConversationHistory()
    }

    private fun sendMessage(text: String) {
        chatInputEditText.text.clear()
        chatSendButton.isEnabled = false

        conversationHistory.add(OllamaMessage(role = "user", content = text))
        chatAdapter.setMessages(conversationHistory)
        chatAdapter.setTyping(true)
        scrollToBottom()

        viewLifecycleOwner.lifecycleScope.launch {
            // Update system prompt with latest habit data on each message
            val sysIdx = conversationHistory.indexOfFirst { it.role == "system" }
            if (sysIdx >= 0) {
                conversationHistory[sysIdx] = OllamaMessage(role = "system", content = buildSystemPrompt())
            }

            val response = OllamaRepository.sendChatMessage(conversationHistory)

            chatAdapter.setTyping(false)

            val reply = response ?: OllamaMessage(
                role = "assistant",
                content = "I'm having trouble connecting to the local AI right now. Make sure server.py is running, then try again!"
            )
            conversationHistory.add(reply)
            saveHistory()

            chatAdapter.setMessages(conversationHistory)
            scrollToBottom()
            chatSendButton.isEnabled = true
        }
    }

    private fun scrollToBottom() {
        if (chatAdapter.itemCount > 0) {
            chatRecyclerView.post {
                chatRecyclerView.smoothScrollToPosition(chatAdapter.itemCount - 1)
            }
        }
    }
}
