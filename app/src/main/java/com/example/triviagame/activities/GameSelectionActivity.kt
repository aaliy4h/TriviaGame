package com.example.triviagame.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.example.triviagame.helpers.DataRetrieval
import com.example.triviagame.R
import com.example.triviagame.fragments.LogoutDialogFragment
import com.example.triviagame.helpers.DatabaseHelper
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.koushikdutta.ion.Ion
import org.json.JSONArray
import org.json.JSONObject

/**
 * Activity that allows user to choose different aspects of their game
 */
class GameSelectionActivity : AppCompatActivity(), LogoutDialogFragment.DialogListener {

    private var categorySelected: String = ""
    private var questionNumSelected: String = ""
    private var answerTypeSelected: String = ""
    private val triviaCategories = HashMap<String, Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.game_selection_activity)

        val toolbar = findViewById<MaterialToolbar>(R.id.app_bar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        getCategories()

        val classicGame = findViewById<MaterialButton>(R.id.classic_button)
        classicGame.setOnClickListener {
            gamesScreens("Classic")
        }

        val timedGame = findViewById<MaterialButton>(R.id.timed_button)
        timedGame.setOnClickListener {
            gamesScreens("Timed")
        }

        val hangmanGame = findViewById<MaterialButton>(R.id.hangman_button)
        hangmanGame.setOnClickListener {
            gamesScreens("Hangman")
        }

    }

    /**
     * Populates the dropdown menu with the different elements
     */
    private fun populateDropdown(menu: Int, menuArray: Int?, selectedItem: (String) -> Unit) {
        val dropdownMenu = findViewById<MaterialAutoCompleteTextView>(menu)
        val items: Array<String> = if (menuArray == null) {
            triviaCategories.keys.toTypedArray()
        } else {
            resources.getStringArray(menuArray)
        }

        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, items)

        dropdownMenu.setAdapter(adapter)
        dropdownMenu.setOnClickListener {
            dropdownMenu.showDropDown()
        }
        dropdownMenu.setOnItemClickListener {_, _, position, _ ->
            selectedItem(items[position])
        }

    }

    /**
     * Handles the different game modes
     */
    private fun gamesScreens(gameMode: String) {
        val contextView = findViewById<View>(R.id.main)
        if (categorySelected.isEmpty() || questionNumSelected.isEmpty() || answerTypeSelected.isEmpty()) {
            Snackbar.make(contextView, getString(R.string.empty_dropdown), Snackbar.LENGTH_SHORT).show()
            return
        }

        if (gameMode != "Timed" && questionNumSelected == "No Limit [Timed]") {
            Snackbar.make(contextView, getString(R.string.invalid_selection), Snackbar.LENGTH_SHORT).show()
            return
        } else if (gameMode == "Timed" && questionNumSelected != "No Limit [Timed]") {
            Snackbar.make(contextView, getString(R.string.invalid_selection_timed), Snackbar.LENGTH_SHORT).show()
            return
        }

        if (gameMode == "Timed") {
            questionNumSelected = "30"
        }

        DataRetrieval(categorySelected, questionNumSelected, answerTypeSelected, triviaCategories, this) {
            apiResults -> startGame(apiResults, gameMode)
        }

    }

    /**
     * Sets the layout and chooses the activity depending on what the user selected
     */
    private fun startGame(apiResults: String, gameMode: String) {
        if (apiResults == "") {
            val contextView = findViewById<View>(R.id.main)
            Snackbar.make(contextView, getString(R.string.no_questions), Snackbar.LENGTH_LONG).show()
        } else {
            val layout = when (answerTypeSelected) {
                "True or False" -> when (gameMode) {
                    "Classic" -> R.layout.classic_true_false_activity
                    "Timed" -> R.layout.timed_true_false_activity
                    else -> R.layout.hangman_true_false_activity
                }
                "Multiple Choice" -> when (gameMode) {
                    "Classic" -> R.layout.classic_multiple_choice_activity
                    "Timed" -> R.layout.timed_multiple_choice_activity
                    else -> R.layout.hangman_multiple_choice_activity
                }
                else -> null
            }

            val activity = when (answerTypeSelected) {
                "True or False" -> TrueFalseActivity::class.java
                "Multiple Choice" -> MultipleChoiceActivity::class.java
                else -> null
            }

            val intent = Intent(this, activity)
            intent.putExtra("category", categorySelected)
            intent.putExtra("question_number", questionNumSelected)
            intent.putExtra("answer_type", answerTypeSelected)
            intent.putExtra("game_mode", gameMode)
            intent.putExtra("results", apiResults)
            intent.putExtra("layout", layout)
            startActivity(intent)

        }
    }

    /**
     * Gets the categories from the API
     */
    private fun getCategories() {
        val myUrl = "https://opentdb.com/api_category.php"
        Ion.with(this).load("GET", myUrl)
            .asString()
            .setCallback{ex, result -> processCategories(result)}
    }

    /**
     * Processes the categories from the API
     */
    private fun processCategories(result: String) {
        val myJson = JSONObject(result)
        val categories = myJson.getString("trivia_categories")

        val myArray = JSONArray(categories)

        for (i in 0 until myArray.length()) {
            val category = myArray.getJSONObject(i)
            val name = category.get("name")
            val id = category.get("id")
            triviaCategories[name.toString()] = id as Int
        }

        populateDropdown(R.id.category_menu, null) {
                currentSelection -> categorySelected = currentSelection }
        populateDropdown(R.id.question_num_menu, R.array.num_questions) {
                currentSelection -> questionNumSelected = currentSelection }
        populateDropdown(R.id.type_menu, R.array.question_type) {
                currentSelection -> answerTypeSelected = currentSelection }
    }

    /**
     * Inflates the app bar layout
     */
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate((R.menu.appbar_layout), menu)
        val sharedPreferences = getSharedPreferences("preferences", Context.MODE_PRIVATE)
        val userID = sharedPreferences.getInt("user_id", 0)
        val mDatabase = DatabaseHelper(this)
        val coins = mDatabase.getUserCoins(userID)
        val string = getString(R.string.coins, coins)
        menu?.findItem(R.id.coins)?.title = string
        return super.onCreateOptionsMenu(menu)
    }

    /**
     * Handles user interaction with app bar elements
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.logout -> {
                LogoutDialogFragment()
                    .show(supportFragmentManager, "LogoutDialog")
            } R.id.pet -> {
            val intent = Intent(this, PetActivity::class.java)
            startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * Clears preferences and signs out of firebase when the user chooses to logout
     */
    override fun onDialogResult(logout: Boolean) {
        if (logout) {
            val sharedPreferences = getSharedPreferences("preferences", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.clear()
            editor.apply()

            Firebase.auth.signOut()

            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)

        }
    }

}