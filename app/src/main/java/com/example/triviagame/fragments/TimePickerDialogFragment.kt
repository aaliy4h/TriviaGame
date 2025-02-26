package com.example.triviagame.fragments

import android.app.Dialog
import android.os.Bundle
import android.widget.TimePicker
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.example.triviagame.R
import com.google.android.material.textview.MaterialTextView

/**
 * Dialog fragment containing the time picker
 */
class TimePickerDialogFragment: DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity.let {
            val builder = AlertDialog.Builder(requireContext())
            val inflater = requireActivity().layoutInflater
            val view = inflater.inflate(R.layout.dialog_time_picker, null)
            val titleView = inflater.inflate(R.layout.custom_dialog_title, null)
            val timePicker = view.findViewById<TimePicker>(R.id.time_picker)
            timePicker.setIs24HourView(true)
            val dialogTitle = titleView.findViewById<MaterialTextView>(R.id.dialog_title)
            dialogTitle.setText(R.string.time_picker_title)

            builder.setCustomTitle(dialogTitle)
                .setCustomTitle(titleView)
                .setView(view)
                .setPositiveButton(R.string.confirm) { _, _ ->
                    (activity as? DialogListener)?.onDialogResult(timePicker.hour, timePicker.minute)
                }
                .setNegativeButton(R.string.cancel, null)

            builder.create()
        }
    }

    interface DialogListener {
        fun onDialogResult(selectedHour: Int, selectedMinute: Int) {

        }
    }
}