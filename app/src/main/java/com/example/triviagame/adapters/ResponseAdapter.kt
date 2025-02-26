package com.example.triviagame.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.triviagame.R
import com.example.triviagame.helpers.DatabaseHelper
import com.example.triviagame.models.Answer
import com.google.android.material.textview.MaterialTextView

/**
 * Adapter for the recycler view of past responses
 */
class ResponseAdapter(private val questionAnswerList: MutableList<Answer>, private val mDatabase: DatabaseHelper,
                      private val userID: Int):
    RecyclerView.Adapter<ResponseAdapter.ViewHolder>() {

    inner class ViewHolder(var layout: View) : RecyclerView.ViewHolder(layout) {
        var question = itemView.findViewById<View>(R.id.question) as MaterialTextView
        var userAnswer = itemView.findViewById<View>(R.id.user_answer) as MaterialTextView
        var correctAnswer = itemView.findViewById<View>(R.id.correct_answer) as MaterialTextView
    }

    fun updateResponses() {
        questionAnswerList.clear()
        questionAnswerList.addAll(mDatabase.getUserAnswers(userID))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val v = inflater.inflate(R.layout.cardview_rows, parent, false)
            return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val info = questionAnswerList[position]
        val context = holder.layout.context

        holder.question.text = info.question
        holder.correctAnswer.text = context.getString(R.string.correct_answer, info.correctAnswer)
        holder.userAnswer.text = context.getString(R.string.user_answer, info.userAnswer)

        if (info.correctAnswer == info.userAnswer) {
            holder.userAnswer.setBackgroundColor(context.getColor(R.color.light_green))
        } else {
            holder.userAnswer.setBackgroundColor(context.getColor(R.color.light_red))
        }
    }

    override fun getItemCount(): Int {
        return questionAnswerList.size
    }

}