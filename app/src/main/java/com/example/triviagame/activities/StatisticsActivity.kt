package com.example.triviagame.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.triviagame.R
import com.example.triviagame.adapters.ResponseAdapter
import com.example.triviagame.adapters.StatisticsAdapter
import com.example.triviagame.fragments.LogoutDialogFragment
import com.example.triviagame.helpers.DatabaseHelper
import com.example.triviagame.models.Answer
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.carousel.CarouselLayoutManager
import com.google.android.material.carousel.CarouselSnapHelper
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

/**
 * Activity containing past responses and general statistics for the user
 */
class StatisticsActivity : AppCompatActivity(), LogoutDialogFragment.DialogListener {

    private val questionAnswerList = mutableListOf<Answer>()
    private var userID = 0
    private lateinit var mDatabase: DatabaseHelper

    /**
     * Sets up the initial views
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.statistics_activity)

        val toolbar = findViewById<MaterialToolbar>(R.id.app_bar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        mDatabase = DatabaseHelper(this)

        val sharedPreferences = getSharedPreferences("preferences", Context.MODE_PRIVATE)
        userID = sharedPreferences.getInt("user_id", 0)
        val userStatisticsList = mDatabase.getUserStatistics(userID)

        // These four lines of code were taken from an online source [3] (see MainActivity)
        val carouselRecyclerView = findViewById<RecyclerView>(R.id.carousel_recycler_view)
        carouselRecyclerView.layoutManager = CarouselLayoutManager()
        val snapHelper = CarouselSnapHelper()
        snapHelper.attachToRecyclerView(carouselRecyclerView)


        val carouselAdapter = StatisticsAdapter(userStatisticsList, mDatabase, userID)
        carouselAdapter.updateStatistics()
        carouselRecyclerView.adapter = carouselAdapter
        val recyclerView = findViewById<RecyclerView>(R.id.question_recycler)
        recyclerView.layoutManager = LinearLayoutManager(this)
        val mAdapter = ResponseAdapter(questionAnswerList, mDatabase, userID )
        mAdapter.updateResponses()
        recyclerView.adapter = mAdapter

    }

    /**
     * Inflates the app bar layout
     */
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate((R.menu.appbar_layout), menu)
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