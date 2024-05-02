package com.example.alphatrade

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

class NotificationReceiver: BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        val stockSymbol = intent.getStringExtra("stockSymbol")
        val userID = intent.getStringExtra("userID")

        val name = context.getSharedPreferences("UserData", Context.MODE_PRIVATE).getString(userID, "")!!.split(", ").toMutableList()[1]

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("my_channel_id", "My Channel", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(context, "my_channel_id")
            .setContentTitle("Reminder from Alpha Trade")
            .setContentText("Hey $name!. This is a reminder for you to check on the stocks of $stockSymbol.")
            .setSmallIcon(R.drawable.logo)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setStyle(NotificationCompat.BigTextStyle().bigText("Hey $name!. This is a reminder for you to check on the stocks of $stockSymbol."))

        notificationManager.notify(1, builder.build())
    }
}