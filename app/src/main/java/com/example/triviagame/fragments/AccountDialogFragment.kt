package com.example.triviagame.fragments

import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.example.triviagame.R
import com.google.android.material.textview.MaterialTextView

/**
 * Dialog fragment containing user input for login and register
 */
class AccountDialogFragment(private val title : String, private val position : Int) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?) : AlertDialog {
        return activity.let{
            val builder = AlertDialog.Builder(requireContext())
            val inflater = requireActivity().layoutInflater;
            val view = inflater.inflate(R.layout.dialog_login, null)
            val username = view.findViewById<EditText>(R.id.username)
            val password = view.findViewById<EditText>(R.id.password)
            val titleView = inflater.inflate(R.layout.custom_dialog_title, null)
            val dialogTitle = titleView.findViewById<MaterialTextView>(R.id.dialog_title)
            dialogTitle.text = title

            builder.setCustomTitle(dialogTitle)
                .setCustomTitle(titleView)
                .setView(view)
                .setPositiveButton(R.string.confirm) { _, _ ->
                    (activity as? DialogListener)?.onDialogResult(username.text.toString(),
                        password.text.toString(), position)
                }
                .setNegativeButton(R.string.cancel, null)

            builder.create()

        }
    }

    interface DialogListener {
        fun onDialogResult(username: String, password: String, position: Int)
    }

}