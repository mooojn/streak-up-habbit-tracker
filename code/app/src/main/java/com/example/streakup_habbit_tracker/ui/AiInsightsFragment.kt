package com.example.streakup_habbit_tracker.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.streakup_habbit_tracker.R
import com.example.streakup_habbit_tracker.data.HabitRepository
import com.example.streakup_habbit_tracker.data.remote.OllamaMessage
import com.example.streakup_habbit_tracker.data.remote.OllamaRepository
import kotlinx.coroutines.launch

class AiInsightsFragment : Fragment() {

    private lateinit var chatRecyclerView: RecyclerView
    private lateinit var chatInputEditText: EditText
    private lateinit var chatSendButton: ImageButton
    private lateinit var chatAdapter: ChatAdapter

    private val messages = mutableListOf<OllamaMessage>()

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

        chatAdapter = ChatAdapter()
        val layoutManager = LinearLayoutManager(requireContext())
        layoutManager.stackFromEnd = true
        chatRecyclerView.layoutManager = layoutManager
        chatRecyclerView.adapter = chatAdapter

        setupInitialContext()

        chatSendButton.setOnClickListener {
            val userText = chatInputEditText.text.toString().trim()
            if (userText.isNotEmpty()) {
                sendMessage(userText)
            }
        }
    }

    private fun setupInitialContext() {
        val habits = HabitRepository.getHabits()
        val habitsInfo = if (habits.isEmpty()) {
            "The user doesn't have any habits yet."
        } else {
            habits.joinToString(separator = "\n") { habit ->
                "- ${habit.title}: Streak of ${habit.streakCount} days. Completed today? ${if (HabitRepository.hasCompletedToday(habit)) "Yes" else "No"}"
            }
        }

        val systemPrompt = """
            You are a helpful, encouraging habit coach chatbot.
            You help the user improve their habits.
            Here is the user's current habit data:
            $habitsInfo
            
            Always keep your answers concise, encouraging, and directly related to the user's habits if asked.
        """.trimIndent()

        messages.add(OllamaMessage(role = "system", content = systemPrompt))
        messages.add(OllamaMessage(role = "assistant", content = "Hi! I'm your AI Habit Coach. Ask me anything about your habits or how to improve your streak!"))
        chatAdapter.setMessages(messages)
    }

    private fun sendMessage(text: String) {
        chatInputEditText.text.clear()
        
        messages.add(OllamaMessage(role = "user", content = text))
        chatAdapter.setMessages(messages)
        chatRecyclerView.scrollToPosition(chatAdapter.itemCount - 1)

        chatSendButton.isEnabled = false

        viewLifecycleOwner.lifecycleScope.launch {
            val responseMsg = OllamaRepository.sendChatMessage(messages)
            
            if (responseMsg != null) {
                messages.add(responseMsg)
            } else {
                messages.add(OllamaMessage(role = "assistant", content = "Sorry, I couldn't get a response. Please try again."))
            }
            
            chatAdapter.setMessages(messages)
            chatRecyclerView.scrollToPosition(chatAdapter.itemCount - 1)
            chatSendButton.isEnabled = true
        }
    }
}
