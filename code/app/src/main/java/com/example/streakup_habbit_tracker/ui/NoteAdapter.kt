package com.example.streakup_habbit_tracker.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.streakup_habbit_tracker.R
import com.example.streakup_habbit_tracker.data.Note
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NoteAdapter(
    private val listener: NoteActionListener
) : RecyclerView.Adapter<NoteAdapter.NoteViewHolder>() {

    interface NoteActionListener {
        fun onAiHelp(note: Note)
        fun onEdit(note: Note)
        fun onDelete(note: Note)
    }

    private val notes = mutableListOf<Note>()
    private val dateFormatter = SimpleDateFormat("MMM d, h:mm a", Locale.US)

    fun setNotes(newNotes: List<Note>) {
        notes.clear()
        notes.addAll(newNotes)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_note, parent, false)
        return NoteViewHolder(view)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        holder.bind(notes[position])
    }

    override fun getItemCount(): Int = notes.size

    inner class NoteViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val titleText: TextView = view.findViewById(R.id.noteTitleText)
        private val bodyText: TextView = view.findViewById(R.id.noteBodyText)
        private val dateText: TextView = view.findViewById(R.id.noteDateText)
        private val aiHelpButton: ImageButton = view.findViewById(R.id.aiHelpNoteButton)
        private val editButton: ImageButton = view.findViewById(R.id.editNoteButton)
        private val deleteButton: ImageButton = view.findViewById(R.id.deleteNoteButton)

        fun bind(note: Note) {
            titleText.text = note.title
            bodyText.text = note.body
            bodyText.visibility = if (note.body.isBlank()) View.GONE else View.VISIBLE
            dateText.text = dateFormatter.format(Date(note.updatedAt))
            aiHelpButton.setOnClickListener { listener.onAiHelp(note) }
            editButton.setOnClickListener { listener.onEdit(note) }
            deleteButton.setOnClickListener { listener.onDelete(note) }
        }
    }
}
