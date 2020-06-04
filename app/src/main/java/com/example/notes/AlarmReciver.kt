package com.example.notes

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat

class AlarmReciver: BroadcastReceiver() {

    companion object {
        const val ID = "CHANEL_ID"
        const val chanelName = "CHANEL_NAME"
    }

    override fun onReceive(context: Context?, intent: Intent?) {

        val manager = context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channelNotification = NotificationCompat.Builder(
            context, ID)
            .setContentTitle("Memo Lite")
            .setContentText("Your event just started!")
            .setSmallIcon(R.drawable.ic_calendar_36dp)

        val channel = NotificationChannel(ID, chanelName, NotificationManager.IMPORTANCE_HIGH)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(channel)
            manager.notify(1, channelNotification.build())
        }
    }
}