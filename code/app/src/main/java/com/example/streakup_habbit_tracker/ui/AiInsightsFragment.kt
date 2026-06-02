package com.example.streakup_habbit_tracker.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.streakup_habbit_tracker.R
import com.example.streakup_habbit_tracker.data.remote.OllamaRepository
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.launch

class AiInsightsFragment : Fragment() {

    private var generateInsightsButton: MaterialButton? = null
    private var loadingSpinner: ProgressBar? = null
    private var insightsCard: MaterialCardView? = null
    private var insightsResultText: TextView? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_ai_insights, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        generateInsightsButton = view.findViewById(R.id.generateInsightsButton)
        loadingSpinner = view.findViewById(R.id.loadingSpinner)
        insightsCard = view.findViewById(R.id.insightsCard)
        insightsResultText = view.findViewById(R.id.insightsResultText)

        generateInsightsButton?.setOnClickListener {
            generateInsights()
        }
    }

    private fun generateInsights() {
        loadingSpinner?.visibility = View.VISIBLE
        insightsCard?.visibility = View.GONE
        generateInsightsButton?.isEnabled = false

        viewLifecycleOwner.lifecycleScope.launch {
            val result = OllamaRepository.getInsights()
            
            loadingSpinner?.visibility = View.GONE
            insightsCard?.visibility = View.VISIBLE
            generateInsightsButton?.isEnabled = true
            
            insightsResultText?.text = result
        }
    }
}
