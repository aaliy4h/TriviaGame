package com.example.triviagame.fragments

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.example.triviagame.R
import com.google.android.material.textview.MaterialTextView

/**
 * Dialog fragment containing the game results
 */
class GameDialogFragment(private val correctAnswers: Int, private val category:
String, private val numQuestions: String, private val answerType: String,
    private val gameType: String, private val coins: Int, private val title: String) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity.let{
            val builder = AlertDialog.Builder(requireContext())
            val inflater = requireActivity().layoutInflater
            val view = inflater.inflate(R.layout.dialog_game, null)
            val titleView = inflater.inflate(R.layout.custom_dialog_title, null)
            val dialogTitle = titleView.findViewById<MaterialTextView>(R.id.dialog_title)
            dialogTitle.text = title

            val gameSettings = view.findViewById<MaterialTextView>(R.id.game_settings)
            gameSettings.text = getString(R.string.game_settings, category, numQuestions, answerType, gameType)
            val numCorrect = view.findViewById<MaterialTextView>(R.id.correct_answers)
            numCorrect.text = getString(R.string.correct_answers, correctAnswers)
            val coinsAwarded = view.findViewById<MaterialTextView>(R.id.coins_awarded)
            coinsAwarded.text = getString(R.string.coins_awarded, coins)

            builder.setCustomTitle(titleView)
                .setView(view)
                .setPositiveButton(R.string.home) {_, _ ->
                    (activity as? DialogListener)?.onDialogResult("home")
                }
                .setNegativeButton(R.string.retry) {_, _ ->
                    (activity as? DialogListener)?.onDialogResult("retry")
                }

            builder.create()
        }
    }

    interface DialogListener {
        fun onDialogResult(screen: String)
    }


}