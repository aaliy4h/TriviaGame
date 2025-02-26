package com.example.triviagame.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkRequest
import androidx.work.WorkManager
import com.example.triviagame.R
import com.example.triviagame.fragments.LogoutDialogFragment
import com.example.triviagame.helpers.DatabaseHelper
import com.example.triviagame.helpers.PetWorker
import com.example.triviagame.models.Pet
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

/**
 * Activity containing the user's pet
 */
class PetActivity: AppCompatActivity(), LogoutDialogFragment.DialogListener {

    private var coins: Int = 0
    private var userID: Int = 0
    private var appBar: Menu? = null
    private lateinit var mDatabase: DatabaseHelper
    private lateinit var hungerBar: ProgressBar
    private lateinit var entertainmentBar: ProgressBar
    private lateinit var happinessBar: ProgressBar


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pet_activity)

        val toolbar = findViewById<MaterialToolbar>(R.id.app_bar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        val uploadWorkRequest: WorkRequest = PeriodicWorkRequest
            .Builder(PetWorker::class.java, 1, java.util.concurrent.TimeUnit.DAYS)
            .setInitialDelay(1, java.util.concurrent.TimeUnit.DAYS)
            .build()
        WorkManager.getInstance(this).enqueue(uploadWorkRequest)

        mDatabase = DatabaseHelper(this)
        hungerBar = findViewById(R.id.hunger_bar)
        entertainmentBar = findViewById(R.id.entertainment_bar)
        happinessBar = findViewById(R.id.happiness_bar)

        getPreferences()
        initialisePetBars()

        val feedButton = findViewById<MaterialButton>(R.id.feed_button)
        feedButton.setOnClickListener {
            interactWithPet("feed")
        }

        val playButton = findViewById<MaterialButton>(R.id.play_pet_button)
        playButton.setOnClickListener {
            interactWithPet("play")
        }
    }

    /**
     * Retrieves user ID and coins
     */
    private fun getPreferences() {
        val sharedPreferences = getSharedPreferences("preferences", Context.MODE_PRIVATE)
        userID = sharedPreferences.getInt("user_id", 0)
        Log.d("debug", "userID: $userID")
        coins = mDatabase.getUserCoins(userID).toInt()
    }

    /**
     * Sets up the initial pet bar views
     */
    private fun initialisePetBars() {
        val petImage = findViewById<ImageView>(R.id.pet_image)
        val petLevels = mDatabase.getPetLevels(userID)
        val hunger = petLevels[0]
        val entertainment = petLevels[1]
        val happiness = petLevels[2]

        hungerBar.progress = hunger
        entertainmentBar.progress = entertainment
        happinessBar.progress = happiness

        if (happinessBar.progress < 50) {
            petImage.setImageResource(R.drawable.pet_sad)
        } else {
            petImage.setImageResource(R.drawable.pet_happy)
        }
    }

    /**
     * Handles user interaction with the pet
     */
    private fun interactWithPet(action: String) {
        val contextView = findViewById<View>(R.id.main)
        if (coins < 10) {
            Snackbar.make(contextView, getString(R.string.no_coins), Snackbar.LENGTH_SHORT).show()
        } else {
            coins -= 10
            mDatabase.updateUserCoins(userID, coins)
            val petImage = findViewById<ImageView>(R.id.pet_image)
            val actionMessage: Int
            if (action == "feed") {
                actionMessage = R.string.pet_eating
                petImage.setImageResource(R.drawable.pet_eating)
                hungerBar.progress += 10
            } else {
                actionMessage = R.string.pet_playing
                petImage.setImageResource(R.drawable.pet_playing)
                entertainmentBar.progress += 10
            }
            onCreateOptionsMenu(appBar)
            Snackbar.make(contextView, getString(actionMessage), 2000).show()
            Handler(Looper.getMainLooper()).postDelayed({
                updateHappiness()
            }, 2000)
            updateDatabase()
        }
    }

    /**
     * Updates the happiness bar to reflect the current pet levels
     */
    private fun updateHappiness() {
        val happiness = (hungerBar.progress + entertainmentBar.progress) / 2
        happinessBar.progress = happiness
        val petImage = findViewById<ImageView>(R.id.pet_image)
        if (happinessBar.progress < 50) {
            petImage.setImageResource(R.drawable.pet_sad)
        } else {
            petImage.setImageResource(R.drawable.pet_happy)
        }
    }

    /**
     * Inserts the updated pet levels into the database
     */
    private fun updateDatabase() {
        val petID = mDatabase.getPetID(userID)
        val pet = Pet(petID, userID, "PetName", hungerBar.progress,
            entertainmentBar.progress, happinessBar.progress)
        mDatabase.updatePet(pet)
    }


    /**
     * Inflates the app bar layout
     */
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate((R.menu.pet_appbar_layout), menu)
        val sharedPreferences = getSharedPreferences("preferences", Context.MODE_PRIVATE)
        val userID = sharedPreferences.getInt("user_id", 0)
        val coins = mDatabase.getUserCoins(userID)
        val string = getString(R.string.coins, coins)
        menu?.findItem(R.id.coins)?.title = string
        appBar = menu
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