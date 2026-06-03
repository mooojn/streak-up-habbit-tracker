package com.example.streakup_habbit_tracker.ui

import android.content.res.ColorStateList
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.streakup_habbit_tracker.R
import com.example.streakup_habbit_tracker.data.remote.OllamaMessage

class ChatAdapter : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    private val messages = mutableListOf<OllamaMessage>()

    fun setMessages(newMessages: List<OllamaMessage>) {
        messages.clear()
        // Filter out the system prompt from the UI
        messages.addAll(newMessages.filter { it.role != "system" })
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat_message, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val message = messages[position]
        holder.bind(message)
    }

    override fun getItemCount(): Int = messages.size

    class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val container: LinearLayout = itemView.findViewById(R.id.chatMessageContainer)
        private val messageText: TextView = itemView.findViewById(R.id.chatMessageText)

        fun bind(message: OllamaMessage) {
            messageText.text = message.content

            val context = itemView.context
            if (message.role == "user") {
                container.gravity = Gravity.END
                messageText.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.brand_primary))
                messageText.setTextColor(ContextCompat.getColor(context, android.R.color.white))
            } else {
                container.gravity = Gravity.START
                messageText.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.surface_card))
                messageText.setTextColor(ContextCompat.getColor(context, R.color.text_primary))
            }
        }
    }
}
