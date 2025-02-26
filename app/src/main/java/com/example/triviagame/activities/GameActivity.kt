package com.example.triviagame.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Menu
import android.view.View
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.triviagame.R
import com.example.triviagame.fragments.GameDialogFragment
import com.example.triviagame.fragments.LogoutDialogFragment
import com.example.triviagame.helpers.DatabaseHelper
import com.example.triviagame.models.Answer
import com.example.triviagame.models.User
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textview.MaterialTextView
import org.json.JSONArray

/**
 * Abstract class containing the game functionality
 */
abstract class GameActivity : AppCompatActivity(), GameDialogFragment.DialogListener,
    LogoutDialogFragment.DialogListener {

    abstract val layoutId : Int
    abstract fun getSelectedRadioGroup(): RadioGroup
    abstract fun getSelectedButton(): RadioButton?

    private var currentNumber = 1
    private var category = ""
    private var numQuestions = ""
    private var answerType = ""
    private var gameType = ""
    private var correctAnswers = 0
    private var incorrectAnswers = 0
    private var userID = 0
    private lateinit var mDatabase: DatabaseHelper
    private lateinit var apiResults: String

    override fun onCreate(savedInstanceState: Bundle?) {
        val extras = intent.extras
        val layout: Int = extras?.getInt("layout")!!
        super.onCreate(savedInstanceState)
        setContentView(layout)

        val sharedPreferences = getSharedPreferences("preferences", Context.MODE_PRIVATE)
        userID = sharedPreferences.getInt("user_id", 0)
        mDatabase = DatabaseHelper(this)

        val toolbar = findViewById<MaterialToolbar>(R.id.app_bar)
        setSupportActionBar(toolbar)

        retrieveExtras()
        screenDisplay(null)
        gameFunctionality()

    }

    /**
     * Retrieves the extras from the intent
     */
    private fun retrieveExtras() {
        val extras = intent.extras
        category = extras?.getString("category") as String
        numQuestions = extras.getString("question_number") as String
        answerType = extras.getString("answer_type") as String
        gameType = extras.getString("game_mode") as String
        apiResults = extras.getString("results") as String
    }

    /**
     * Sets the information bar values and updates the question number and question text
     */
    private fun screenDisplay(currentQuestion: String?) {
        val categoryInfo = findViewById<TextView>(R.id.current_category)
        categoryInfo.text = category

        val questionInfo = findViewById<TextView>(R.id.current_question)
        val questionNumDisplay = if (gameType == "Timed") {
            "$currentNumber"
        } else {
            "$currentNumber/$numQuestions"
        }
        questionInfo.text = questionNumDisplay

        val answerInfo = findViewById<TextView>(R.id.current_type)
        answerInfo.text = answerType
        if (currentQuestion != null) {
            findViewById<MaterialTextView>(R.id.question).text = formatText(currentQuestion)
        }

    }

    /**
     * Controls the general functionality of a game
     */
    private fun gameFunctionality() {
        val number = numQuestions.toInt()
        var hangmanFinished = false
        val myJson = JSONArray(apiResults)
        var answerIndex = 0

        if (answerType == "Multiple Choice") {
            populateMultipleChoice(answerIndex)
        }
        val firstQuestion = myJson.getJSONObject(0).getString("question")
        findViewById<MaterialTextView>(R.id.question).text = formatText(firstQuestion)

        if (gameType == "Timed") {
            timedGame()
        }

        val submit = findViewById<MaterialButton>(R.id.submit_button)
        submit.setOnClickListener {
            val radioGroup = getSelectedRadioGroup()
            val button = getSelectedButton()
            val userAnswer = button?.text.toString()

            val answerArray = myJson.getJSONObject(currentNumber-1)
            val currentAnswer = answerArray.getString("correct_answer")
            addToDatabase(currentAnswer, userAnswer, apiResults)

            if (button == null) {
                val contextView = findViewById<View>(R.id.main)
                Snackbar.make(contextView, getString(R.string.no_answer), Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (userAnswer == currentAnswer) {
                correctAnswers ++
                button.setBackgroundResource(R.drawable.radio_correct_selector)
                Snackbar.make(findViewById(R.id.main), getString(R.string.correct), 1000).show()
            } else {
                incorrectAnswers ++
                button.setBackgroundResource(R.drawable.radio_incorrect_selector)
                Snackbar.make(findViewById(R.id.main), getString(R.string.incorrect), 1000).show()
            }

            if (gameType == "Hangman") {
                hangmanFinished = hangmanGame(incorrectAnswers)
            }

            if (currentNumber < number) {
                val questionArray = myJson.getJSONObject(currentNumber)
                val nextQuestion = questionArray.getString("question")
                currentNumber ++
                answerIndex ++
                Handler(Looper.getMainLooper()).postDelayed({
                    updateScreen(nextQuestion, radioGroup, button)
                    if (answerType == "Multiple Choice") {
                        populateMultipleChoice(answerIndex)
                    }
                }, 1500)
            } else {
                Handler(Looper.getMainLooper()).postDelayed({
                updateScreen(null, radioGroup, button)
                }, 1500)
                showDialog(getString(R.string.game_end))

            }

        }

    }

    /**
     * Controls the functionality for a hangman game
     */
    private fun hangmanGame(incorrectAnswers: Int): Boolean {
        val hangmanImage = findViewById<ImageView>(R.id.hangman_image)
        if (incorrectAnswers < 7) {
            when (incorrectAnswers) {
                1 -> hangmanImage.setImageResource(R.drawable.hangman_stage_2)
                2 -> hangmanImage.setImageResource(R.drawable.hangman_stage_3)
                3 -> hangmanImage.setImageResource(R.drawable.hangman_stage_4)
                4 -> hangmanImage.setImageResource(R.drawable.hangman_stage_5)
                5 -> hangmanImage.setImageResource(R.drawable.hangman_stage_6)
                6 -> hangmanImage.setImageResource(R.drawable.hangman_stage_7)
            }
            return false
        } else {
            hangmanImage.setImageResource(R.drawable.hangman_stage_8)
            showDialog(getString(R.string.hangman_game_lose))
            return true
        }

    }

    /**
     * Countdown Timer implementation was taken from an online source [4] (see MainActivity)
     * Controls the functionality for a timed game using a countdown timer
     */
    private fun timedGame(): Boolean {
        val timer = findViewById<MaterialTextView>(R.id.timer)

        val countdownTimer = object : CountDownTimer(31000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsRemaining = millisUntilFinished / 1000
                timer.text = getString(R.string.time_remaining, secondsRemaining)
            }
            override fun onFinish() {
                showDialog(getString(R.string.timed_game_end))
            }
        }
        countdownTimer.start()
        return true
    }

    /**
     * Populates the multiple choice options
     */
    private fun populateMultipleChoice(answerIndex: Int) {
        val myJson = JSONArray(apiResults)
        val answers = myJson.getJSONObject(answerIndex).getJSONArray("incorrect_answers")
        val correctAnswer = myJson.getJSONObject(answerIndex).getString("correct_answer")
        answers.put(correctAnswer)

        val answerArray = ArrayList<String>()
        for (i in 0 until answers.length()) {
            answerArray.add(formatText(answers.getString(i)))
        }
        answerArray.shuffle()

        findViewById<RadioButton>(R.id.top_right_choice).text = answerArray[0]
        findViewById<RadioButton>(R.id.top_left_choice).text = answerArray[1]
        findViewById<RadioButton>(R.id.bottom_right_choice).text = answerArray[2]
        findViewById<RadioButton>(R.id.bottom_left_choice).text = answerArray[3]
    }

    /**
     * Adds the user's answers to the database
     */
    private fun addToDatabase(correctAnswer: String, userAnswer: String, apiResults: String) {
        val myJson = JSONArray(apiResults)
        val question = myJson.getJSONObject(currentNumber-1).getString("question")
        val answers = Answer(null, userID, formatText(question), correctAnswer, userAnswer)
        val mDatabase = DatabaseHelper(this)
        mDatabase.addAnswer(answers)
    }

    /**
     * Formats the JSON text
     */
    private fun formatText(question: String): String {
        val regex = "&(quot|#039);".toRegex()
        val replaced = question.replace(regex, "'")
        return replaced
    }

    /**
     * Resets the screen after a question is answered
     */
    private fun updateScreen(currentQuestion: String?, radioGroup: RadioGroup, button: RadioButton) {
            if (currentQuestion != null) {
                screenDisplay(currentQuestion)
            }
            button.setBackgroundResource(R.drawable.radio_selector)
            radioGroup.clearCheck()
    }

    /**
     * Shows the dialog fragment and disables the user from clicking outside the dialog
     */
    private fun showDialog(title: String) {
        val coinsReceived = updateUserStatistics()
        val dialog = GameDialogFragment(correctAnswers, category, currentNumber.toString(),
            answerType, gameType, coinsReceived, title)
        dialog.isCancelable = false
        dialog.show(supportFragmentManager, "GameDialog")
    }

    /**
     * Updates the user's game statistics and updates the database
     */
    private fun updateUserStatistics(): Int {
        val sharedPreferences = getSharedPreferences("preferences", Context.MODE_PRIVATE)
        val accountID = sharedPreferences.getString("account_id", null)
        val mDatabase = DatabaseHelper(this)
        val gameStatistics = mDatabase.getUserStatistics(userID)
        Log.d("Debug", "Game Statistics: $gameStatistics")
        var classicPlayed = gameStatistics[0]
        var timedHighScore = gameStatistics[1]
        var hangmanWon = gameStatistics[2]
        var totalCorrect = gameStatistics[3]
        var coins = gameStatistics[4]

        val coinsMultiplier = numQuestions.toInt() / 2
        val coinsReceived: Int
        totalCorrect += correctAnswers

        when (gameType) {
            "Classic" -> {
                classicPlayed++
                coins += coinsMultiplier
                coinsReceived = coinsMultiplier
            }
            "Timed" -> {
                if (correctAnswers > timedHighScore) {
                    timedHighScore = correctAnswers
                }
                coins += 10
                coinsReceived = 10
            }
            else -> {
                if (incorrectAnswers < 7) {
                    hangmanWon++
                    coins += numQuestions.toInt()
                    coinsReceived = numQuestions.toInt()
                } else {
                    coins += coinsMultiplier
                    coinsReceived = coinsMultiplier
                }

            }
        }

        val user = User(userID, accountID, classicPlayed, timedHighScore,
            hangmanWon, totalCorrect, coins)
        mDatabase.updateUser(user)
        return coinsReceived
    }

    /**
     * Sends the user to the home screen or the game selection screen based on the dialog result
     */
    override fun onDialogResult(screen: String) {
        if (screen == "home") {
            val intent = Intent(this, HomeScreenActivity::class.java)
            startActivity(intent)
        } else {
            val intent = Intent(this, GameSelectionActivity::class.java)
            startActivity(intent)
        }

    }

    /**
     * Inflates the app bar layout
     */
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate((R.menu.game_appbar_layout), menu)
        val sharedPreferences = getSharedPreferences("preferences", Context.MODE_PRIVATE)
        val userID = sharedPreferences.getInt("user_id", 0)
        val mDatabase = DatabaseHelper(this)
        val coins = mDatabase.getUserCoins(userID)
        val string = getString(R.string.coins, coins)
        menu?.findItem(R.id.coins)?.title = string

        return super.onCreateOptionsMenu(menu)
    }

}