package com.example.triviagame.activities

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.triviagame.R
import com.example.triviagame.fragments.LogoutDialogFragment
import com.example.triviagame.fragments.TimePickerDialogFragment
import com.example.triviagame.helpers.AlarmReceiver
import com.example.triviagame.helpers.DatabaseHelper
import com.example.triviagame.helpers.NotificationHelper
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

/**
 * Entry point of the application for users that are already logged in.
 * The majority of screens can be accessed from here.
 */
class HomeScreenActivity : AppCompatActivity(), LogoutDialogFragment.DialogListener, TimePickerDialogFragment.DialogListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.homescreen_activity)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                1
            )
        }

        val toolbar = findViewById<MaterialToolbar>(R.id.app_bar)
        setSupportActionBar(toolbar)

        val playButton = findViewById<MaterialButton>(R.id.play_button)
        playButton.setOnClickListener {
            val intent = Intent(this, GameSelectionActivity::class.java)
            startActivity(intent)
        }

        val petButton = findViewById<MaterialButton>(R.id.pet_button)
        petButton.setOnClickListener {
            val intent = Intent(this, PetActivity::class.java)
            startActivity(intent)
        }

        val statisticsButton = findViewById<MaterialButton>(R.id.statistics_button)
        statisticsButton.setOnClickListener {
            val intent = Intent(this, StatisticsActivity::class.java)
            startActivity(intent)
        }

        val timePicker = findViewById<MaterialAutoCompleteTextView>(R.id.time_text_field)
        timePicker.setOnClickListener{
            TimePickerDialogFragment()
                .show(supportFragmentManager, "TimePickerDialog")
        }
    }

    /**
     * Online source was used as a basis for this method [2] (see MainActivity)
     * Sets the chosen time in the calendar and the alarm manager
     */
    private fun notificationTime(hour: Int, minute: Int) {
        val intent = Intent(this, AlarmReceiver::class.java)
        intent.putExtra("type", "play")

        val pendingIntent = PendingIntent.getBroadcast(this, 1, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()

            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
        }

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
    }



    /**
     * Receives the time selected by the user to be notified
     */
    override fun onDialogResult(selectedHour: Int, selectedMinute: Int) {
        notificationTime(selectedHour, selectedMinute)
        val timePicker = findViewById<MaterialAutoCompleteTextView>(R.id.time_text_field)
        timePicker.setText(getString(R.string.current_notification_time, selectedHour, selectedMinute))
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
