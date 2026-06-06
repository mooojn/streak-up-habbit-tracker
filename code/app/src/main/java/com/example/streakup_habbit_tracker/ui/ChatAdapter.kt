package com.example.streakup_habbit_tracker.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.streakup_habbit_tracker.R
import com.example.streakup_habbit_tracker.data.remote.OllamaMessage

class ChatAdapter : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    companion object {
        const val TYPE_USER = 0
        const val TYPE_AI = 1
        const val TYPE_TYPING = 2
        const val TYPING_INDICATOR_ROLE = "typing_indicator"
    }

    private val messages = mutableListOf<OllamaMessage>()
    private var showTyping = false

    fun setMessages(newMessages: List<OllamaMessage>) {
        messages.clear()
        messages.addAll(newMessages.filter { it.role != "system" })
        if (showTyping) {
            messages.add(OllamaMessage(role = TYPING_INDICATOR_ROLE, content = ""))
        }
        notifyDataSetChanged()
    }

    fun setTyping(isTyping: Boolean) {
        showTyping = isTyping
        // Remove old typing indicator if present
        messages.removeAll { it.role == TYPING_INDICATOR_ROLE }
        if (isTyping) {
            messages.add(OllamaMessage(role = TYPING_INDICATOR_ROLE, content = ""))
        }
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return when (messages[position].role) {
            "user" -> TYPE_USER
            TYPING_INDICATOR_ROLE -> TYPE_TYPING
            else -> TYPE_AI
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat_message, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        holder.bind(messages[position])
    }

    override fun getItemCount(): Int = messages.size

    class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val aiRow: LinearLayout = itemView.findViewById(R.id.aiMessageRow)
        private val userRow: LinearLayout = itemView.findViewById(R.id.userMessageRow)
        private val typingRow: LinearLayout = itemView.findViewById(R.id.typingIndicatorRow)
        private val aiText: TextView = itemView.findViewById(R.id.aiMessageText)
        private val userText: TextView = itemView.findViewById(R.id.userMessageText)

        fun bind(message: OllamaMessage) {
            aiRow.visibility = View.GONE
            userRow.visibility = View.GONE
            typingRow.visibility = View.GONE

            when (message.role) {
                "user" -> {
                    userRow.visibility = View.VISIBLE
                    userText.text = message.content
                }
                TYPING_INDICATOR_ROLE -> {
                    typingRow.visibility = View.VISIBLE
                }
                else -> {
                    aiRow.visibility = View.VISIBLE
                    aiText.text = message.content
                }
            }
        }
    }
}
