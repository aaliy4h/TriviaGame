package com.example.triviagame.helpers

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.triviagame.models.Pet

/**
 * Online source used for the general idea of this class [1] (see MainActivity)
 * Class that controls the background behaviour of the pet - decreasing pet levels and notifying the user about the changes
 */
class PetWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    private val mDatabase = DatabaseHelper(context)
    private val mContext = context

    /**
     * Decrease the pet levels by a set amount and updates the database with the new values
     */
    override fun doWork(): Result {
        val sharedPreferences = mContext.getSharedPreferences("preferences", Context.MODE_PRIVATE)
        val userID = sharedPreferences.getInt("user_id", 0)

        val petLevels = mDatabase.getPetLevels(userID)
        var hungerLevel = petLevels[0]
        var entertainmentLevel = petLevels[1]

        if (hungerLevel > 20 ) {
            hungerLevel -=  20
        } else {
            hungerLevel = 0
        }
        if (entertainmentLevel > 20) {
            entertainmentLevel -= 20
        } else {
            entertainmentLevel = 0
        }

        val happinessLevel: Int = (hungerLevel + entertainmentLevel) / 2

        Log.d("PetWorker", "Updated pet levels: Hunger=$hungerLevel, entertainment=$entertainmentLevel")
        Log.d("PetWorker", "Worker executed at: ${System.currentTimeMillis()}")
        val petID = mDatabase.getPetID(userID)
        val pet = Pet(petID, userID, "PetName", hungerLevel, entertainmentLevel, happinessLevel)
        mDatabase.updatePet(pet)
        petNotificationTime()

        return Result.success()
    }

    /**
     * Online source was used as a basis for this method [2] (see MainActivity)
     * Sets a time in the calendar and the alarm manager for pet notifications
     */
    private fun petNotificationTime() {
        val intent = Intent(mContext, AlarmReceiver::class.java)
        intent.putExtra("type", "pet")

        val pendingIntent = PendingIntent.getBroadcast(mContext, 2, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()

            set(Calendar.HOUR_OF_DAY, 12)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }

        val alarmManager = mContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
    }

}