package com.veyra.app

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val id = intent?.getLongExtra("habit_id", -1L) ?: -1L
        val saved = if (id >= 0) HabitReminderStore(context).get(id) else null
        if (saved == null || !saved.enabled) return

        // The alarm is one-shot, so always schedule its next occurrence even if
        // notifications are currently blocked. This keeps the daily reminder alive.
        if (Build.VERSION.SDK_INT >= 33 && context.checkSelfPermission("android.permission.POST_NOTIFICATIONS") != PackageManager.PERMISSION_GRANTED) {
            HabitReminderScheduler.schedule(context, saved)
            return
        }

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "veyra_reminders"
        if (Build.VERSION.SDK_INT >= 26) {
            manager.createNotificationChannel(NotificationChannel(channelId, "Veyra reminders", NotificationManager.IMPORTANCE_HIGH))
        }
        val open = PendingIntent.getActivity(
            context, 0, Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = if (Build.VERSION.SDK_INT >= 26) {
            android.app.Notification.Builder(context, channelId)
                .setSmallIcon(android.R.drawable.ic_popup_reminder)
                .setContentTitle("Time for ${saved.habitName}")
                .setContentText("Your ${saved.habitName} habit is scheduled now.")
                .setContentIntent(open)
                .setAutoCancel(true)
                .setPriority(android.app.Notification.PRIORITY_HIGH)
                .build()
        } else {
            android.app.Notification.Builder(context)
                .setSmallIcon(android.R.drawable.ic_popup_reminder)
                .setContentTitle("Time for ${saved.habitName}")
                .setContentText("Your ${saved.habitName} habit is scheduled now.")
                .setContentIntent(open)
                .setAutoCancel(true)
                .setPriority(android.app.Notification.PRIORITY_HIGH)
                .build()
        }
        manager.notify(10000 + (id % 10000).toInt(), notification)
        HabitReminderScheduler.schedule(context, saved)
    }
}
