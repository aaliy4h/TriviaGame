package com.example.triviagame.helpers

import android.content.Context
import android.util.Log
import com.koushikdutta.ion.Ion
import org.json.JSONObject

class DataRetrieval(private val category: String, private val questionNumber: String,
                    private val answerType: String, private val triviaCategories: HashMap<String, Int>,
                    private val context: Context, private val callback: (String) -> Unit) {

    private var currentQuestion: String = ""

    init {
        getQuestions()
    }

    private fun getQuestions() {
        val type: String = if (answerType == "True or False") {
            "boolean"
        } else {
            "multiple"
        }

        val categoryId = triviaCategories[category]
        val myUrl = "https://opentdb.com/api.php?amount=$questionNumber&category=$categoryId&type=$type"

        Ion.with(context).load("GET", myUrl)
            .asString()
            .setCallback { e, result ->
                if (e != null) {
                // Log the exception or handle the error
                Log.e("Debug", "Error fetching questions: ${e.message}")
                callback("")// Handle as an empty result
            } else if (result != null) {
                // Process the result if no exception occurred
                processQuestions(result)
            } }

    }

    private fun processQuestions(result: String) {
        val myJson = JSONObject(result)
        // get values from response_code attribute
        val responseCode = myJson.getInt("response_code")

        if (responseCode != 0) {
            callback("")
            Log.d("Debug", "if statement $currentQuestion")
        } else {
            // get array from results attribute
            val resultsArray = myJson.getJSONArray("results")
            // gets the first question
            //currentQuestion = resultsArray.getJSONObject(0).getString("question")
            callback(resultsArray.toString())
            Log.d("Debug", "else statement ${resultsArray.toString()}")
        }
    }

}