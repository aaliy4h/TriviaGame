package com.example.triviagame.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.triviagame.R
import com.example.triviagame.helpers.DatabaseHelper
import com.google.android.material.textview.MaterialTextView

/**
 * Adapter for the recycler view of general user statistics
 */
class StatisticsAdapter(private val userStatisticsList: MutableList<Int>, private val mDatabase: DatabaseHelper,
                        private val userID: Int):
    RecyclerView.Adapter<StatisticsAdapter.ViewHolder>() {

    inner class ViewHolder(var layout: View) : RecyclerView.ViewHolder(layout) {
        val title = itemView.findViewById<View>(R.id.list_title) as MaterialTextView
        val information = itemView.findViewById<View>(R.id.list_information) as MaterialTextView
    }

    fun updateStatistics() {
        userStatisticsList.clear()
        userStatisticsList.addAll(mDatabase.getUserStatistics(userID))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val v = inflater.inflate(R.layout.statistics_list_item, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val context = holder.layout.context

        when (position) {
            0 -> {
                holder.title.text = context.getString(R.string.classic_played)
                val classicPlayed = userStatisticsList[0].toString()
                holder.information.text = classicPlayed
            }
            1 -> {
                holder.title.text = context.getString(R.string.timed_high_score)
                val timedHighScore = userStatisticsList[1].toString()
                holder.information.text = timedHighScore
            }
            2 -> {
                holder.title.text = context.getString(R.string.hangman_won)
                val hangmanWon = userStatisticsList[2].toString()
                holder.information.text = hangmanWon
            }
            3 -> {
                holder.title.text = context.getString(R.string.total_correct)
                val correctAnswers = userStatisticsList[3].toString()
                holder.information.text = correctAnswers
            }
        }
    }

    override fun getItemCount(): Int {
        return if (userStatisticsList.isNotEmpty()) 4 else 0
    }

}