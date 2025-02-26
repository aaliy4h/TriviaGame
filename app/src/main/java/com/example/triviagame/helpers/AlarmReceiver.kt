package com.example.triviagame.helpers

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat.getString
import com.example.triviagame.R
import com.example.triviagame.activities.GameSelectionActivity
import com.example.triviagame.activities.PetActivity

/**
 * Online source used as an aid when creating this class [2] (see MainActivity)
 */
class AlarmReceiver : BroadcastReceiver() {
    companion object {
        const val CHANNEL_ONE_ID = "play_game"
        const val CHANNEL_TWO_ID = "pet_notification"
    }
    override fun onReceive(context: Context?, intent: Intent?) {
        context?.let {
            val notificationHelper = NotificationHelper(context)

            if (intent?.getStringExtra("type") == "play") {
                val activityIntent = Intent(context, GameSelectionActivity::class.java)
                activityIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK

                val pendingIntent = PendingIntent.getActivity(context, 1, activityIntent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

                val notification = notificationHelper.getNotification(getString(context, R.string.game_intent_title),
                    getString(context, R.string.game_intent_body), CHANNEL_ONE_ID, R.drawable.ic_play)
                    .setContentIntent(pendingIntent)
                notificationHelper.notify(1, notification)
                notification.build()
            } else {
                val petIntent = Intent(context, PetActivity::class.java)
                petIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK

                val pendingIntentTwo = PendingIntent.getActivity(context, 2, petIntent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

                val notificationTwo = notificationHelper.getNotification(getString(context, R.string.pet_intent_title),
                    getString(context, R.string.pet_intent_body), CHANNEL_TWO_ID, R.drawable.ic_pet)
                    .setContentIntent(pendingIntentTwo)
                notificationHelper.notify(2, notificationTwo)
                notificationTwo.build()

            }


        }
    }
}