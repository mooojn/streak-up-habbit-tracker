package com.example.streakup_habbit_tracker.ui

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.streakup_habbit_tracker.DashboardActivity
import com.example.streakup_habbit_tracker.R
import com.example.streakup_habbit_tracker.data.HabitRepository
import com.example.streakup_habbit_tracker.data.remote.OllamaRepository
import com.example.streakup_habbit_tracker.data.remote.VoiceCreateDraft
import com.google.android.material.card.MaterialCardView
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.switchmaterial.SwitchMaterial
import android.widget.LinearLayout
import kotlinx.coroutines.launch
import java.util.Locale

class AddHabitFragment : Fragment() {

    private var habitTitleInput: TextInputEditText? = null
    private var habitNoteInput: TextInputEditText? = null
    private var noteTitleInput: TextInputEditText? = null
    private var noteBodyInput: TextInputEditText? = null
    private var habitTargetInput: TextInputEditText? = null
    private var habitUnitInput: TextInputEditText? = null
    private var habitFlexibleSwitch: SwitchMaterial? = null
    private var flexibleOptionsLayout: LinearLayout? = null
    private var voiceStatusText: TextView? = null
    private var voiceProgressBar: ProgressBar? = null
    private var voiceDraftCard: MaterialCardView? = null
    private var voiceDraftTypeText: TextView? = null
    private var voiceDraftTitleText: TextView? = null
    private var voiceDraftDetailsText: TextView? = null
    private var voiceCreateButton: MaterialButton? = null
    private var voiceConfirmButton: MaterialButton? = null
    private var currentVoiceDraft: VoiceCreateDraft? = null

    private val microphonePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            startVoiceCapture()
        } else {
            Toast.makeText(requireContext(), R.string.voice_create_permission, Toast.LENGTH_SHORT).show()
        }
    }

    private val speechLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode != Activity.RESULT_OK) return@registerForActivityResult

        val transcript = result.data
            ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            ?.firstOrNull()
            ?.trim()
            .orEmpty()

        if (transcript.isBlank()) {
            Toast.makeText(requireContext(), R.string.voice_create_no_speech, Toast.LENGTH_SHORT).show()
            return@registerForActivityResult
        }

        createAiDraft(transcript)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_add_habit, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        habitTitleInput = view.findViewById(R.id.habitTitleInput)
        habitNoteInput = view.findViewById(R.id.habitNoteInput)
        noteTitleInput = view.findViewById(R.id.noteTitleInput)
        noteBodyInput = view.findViewById(R.id.noteBodyInput)
        habitTargetInput = view.findViewById(R.id.habitTargetInput)
        habitUnitInput = view.findViewById(R.id.habitUnitInput)
        habitFlexibleSwitch = view.findViewById(R.id.habitFlexibleSwitch)
        flexibleOptionsLayout = view.findViewById(R.id.flexibleOptionsLayout)
        voiceStatusText = view.findViewById(R.id.voiceStatusText)
        voiceProgressBar = view.findViewById(R.id.voiceProgressBar)
        voiceDraftCard = view.findViewById(R.id.voiceDraftCard)
        voiceDraftTypeText = view.findViewById(R.id.voiceDraftTypeText)
        voiceDraftTitleText = view.findViewById(R.id.voiceDraftTitleText)
        voiceDraftDetailsText = view.findViewById(R.id.voiceDraftDetailsText)
        voiceCreateButton = view.findViewById(R.id.voiceCreateButton)
        voiceConfirmButton = view.findViewById(R.id.voiceConfirmButton)
        val addHabitButton: MaterialButton = view.findViewById(R.id.addHabitButton)
        val addNoteButton: MaterialButton = view.findViewById(R.id.addNoteButton)

        habitFlexibleSwitch?.setOnCheckedChangeListener { _, isChecked ->
            flexibleOptionsLayout?.isVisible = isChecked
        }

        voiceCreateButton?.setOnClickListener { requestVoiceCapture() }
        voiceConfirmButton?.setOnClickListener { confirmVoiceDraft(view) }
        addHabitButton.setOnClickListener { addHabit(view) }
        addNoteButton.setOnClickListener { addNote(view) }
    }

    private fun requestVoiceCapture() {
        val permission = Manifest.permission.RECORD_AUDIO
        if (ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED) {
            startVoiceCapture()
        } else {
            microphonePermissionLauncher.launch(permission)
        }
    }

    private fun startVoiceCapture() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.voice_create_intro))
        }

        try {
            speechLauncher.launch(intent)
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(requireContext(), R.string.voice_create_unavailable, Toast.LENGTH_SHORT).show()
        }
    }

    private fun createAiDraft(transcript: String) {
        setVoiceLoading(true)
        voiceStatusText?.text = getString(R.string.voice_create_transcript, transcript)
        currentVoiceDraft = null
        voiceDraftCard?.isVisible = false
        voiceConfirmButton?.isEnabled = false

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val draft = OllamaRepository.createDraftFromVoice(transcript)
                currentVoiceDraft = draft
                applyDraftToForm(draft)
                showVoiceDraft(draft)
            } catch (e: Exception) {
                val message = e.message ?: "Unknown error"
                voiceStatusText?.text = getString(R.string.voice_create_error, message)
            } finally {
                setVoiceLoading(false)
            }
        }
    }

    private fun applyDraftToForm(draft: VoiceCreateDraft) {
        if (draft.type == "note") {
            noteTitleInput?.setText(draft.title)
            noteBodyInput?.setText(draft.details)
        } else {
            habitTitleInput?.setText(draft.title)
            habitNoteInput?.setText(draft.details)
            habitFlexibleSwitch?.isChecked = draft.isFlexible
            habitTargetInput?.setText(if (draft.isFlexible) draft.targetValue.toString() else "")
            habitUnitInput?.setText(if (draft.isFlexible) draft.unit else "")
        }
    }

    private fun showVoiceDraft(draft: VoiceCreateDraft) {
        val typeLabel = draft.type.replaceFirstChar { it.uppercase() }
        val flexibleText = if (draft.type == "habit" && draft.isFlexible) {
            " • ${draft.targetValue} ${draft.unit}".trimEnd()
        } else {
            ""
        }
        voiceDraftTypeText?.text = "$typeLabel$flexibleText"
        voiceDraftTitleText?.text = draft.title
        voiceDraftDetailsText?.text = draft.details.ifBlank { "No extra details." }
        voiceDraftCard?.isVisible = true
        voiceConfirmButton?.isEnabled = true
    }

    private fun setVoiceLoading(isLoading: Boolean) {
        voiceProgressBar?.isVisible = isLoading
        voiceCreateButton?.isEnabled = !isLoading
        voiceConfirmButton?.isEnabled = !isLoading && currentVoiceDraft != null
    }

    private fun confirmVoiceDraft(rootView: View) {
        when (currentVoiceDraft?.type) {
            "note" -> addNote(rootView)
            "habit" -> addHabit(rootView)
            else -> Toast.makeText(requireContext(), R.string.voice_create_no_speech, Toast.LENGTH_SHORT).show()
        }
    }

    private fun addHabit(rootView: View) {
        val title = habitTitleInput?.text?.toString()?.trim().orEmpty()
        val note = habitNoteInput?.text?.toString()?.trim().orEmpty()

        if (title.isBlank()) {
            Toast.makeText(requireContext(), R.string.error_habit_title_required, Toast.LENGTH_SHORT).show()
            return
        }

        val isFlexible = habitFlexibleSwitch?.isChecked == true
        val targetValue = habitTargetInput?.text?.toString()?.toIntOrNull() ?: 1
        val unit = habitUnitInput?.text?.toString()?.trim().orEmpty()

        HabitRepository.addHabit(title, note, isFlexible, targetValue, unit)
        habitTitleInput?.setText("")
        habitNoteInput?.setText("")
        habitTargetInput?.setText("")
        habitUnitInput?.setText("")
        habitFlexibleSwitch?.isChecked = false
        clearVoiceDraft()

        val snackbar = Snackbar.make(rootView, R.string.habit_added, Snackbar.LENGTH_SHORT)
        activity?.findViewById<View>(R.id.bottomNavigationView)?.let { navView ->
            snackbar.setAnchorView(navView)
        }
        snackbar.show()

        (activity as? DashboardActivity)?.showHabitsTab()
    }

    private fun addNote(rootView: View) {
        val title = noteTitleInput?.text?.toString()?.trim().orEmpty()
        val body = noteBodyInput?.text?.toString()?.trim().orEmpty()

        if (title.isBlank()) {
            Toast.makeText(requireContext(), R.string.error_note_title_required, Toast.LENGTH_SHORT).show()
            return
        }

        HabitRepository.addNote(title, body)
        noteTitleInput?.setText("")
        noteBodyInput?.setText("")
        clearVoiceDraft()

        val snackbar = Snackbar.make(rootView, R.string.note_added, Snackbar.LENGTH_SHORT)
        activity?.findViewById<View>(R.id.bottomNavigationView)?.let { navView ->
            snackbar.setAnchorView(navView)
        }
        snackbar.show()

        (activity as? DashboardActivity)?.showHabitsTab()
    }

    private fun clearVoiceDraft() {
        currentVoiceDraft = null
        voiceDraftCard?.isVisible = false
        voiceConfirmButton?.isEnabled = false
        voiceStatusText?.setText(R.string.voice_create_intro)
    }
}
