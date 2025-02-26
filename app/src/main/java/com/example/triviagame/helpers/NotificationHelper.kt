package com.example.triviagame.helpers

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.ContextWrapper
import androidx.core.app.NotificationCompat

/**
 * Code in Lecture 14 slides was used as a basis for this class
 * Handles the creation of notifications for the app
 */
class NotificationHelper(base: Context) : ContextWrapper(base) {

    private val manager: NotificationManager =
        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init{
        createChannel()
        createChannelTwo()
    }

    private fun createChannel() {
        val notificationChannel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH)
        notificationChannel.enableLights(true)
        notificationChannel.setShowBadge(true)
        notificationChannel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        manager.createNotificationChannel(notificationChannel)

    }

    private fun createChannelTwo() {
        val notificationChannel = NotificationChannel(CHANNEL_ID_TWO, CHANNEL_NAME_TWO, NotificationManager.IMPORTANCE_HIGH)
        notificationChannel.enableLights(true)
        notificationChannel.setShowBadge(true)
        notificationChannel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        manager.createNotificationChannel(notificationChannel)

    }

    fun getNotification(title: String, body: String, channelID: String, icon: Int) : NotificationCompat.Builder{
        return NotificationCompat.Builder(applicationContext, channelID)
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(icon)
            .setAutoCancel(true)
    }

    fun notify(id: Int, notification: NotificationCompat.Builder) {
        manager.notify(id, notification.build())
    }

    companion object {
        const val CHANNEL_ID = "play_game"
        const val CHANNEL_NAME = "Play Game"
        const val CHANNEL_ID_TWO = "pet_notification"
        const val CHANNEL_NAME_TWO = "Pet Notification"
    }
}