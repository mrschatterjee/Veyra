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

        // Always re-arm the one-shot daily alarm, even if notifications are blocked.
        HabitReminderScheduler.schedule(context, saved)

        if (Build.VERSION.SDK_INT >= 33 && context.checkSelfPermission("android.permission.POST_NOTIFICATIONS") != PackageManager.PERMISSION_GRANTED) return

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "veyra_reminders_v2"
        if (Build.VERSION.SDK_INT >= 26) {
            val channel = NotificationChannel(channelId, "Veyra Reminders", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Scheduled habit reminders from Veyra"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 250, 120, 250)
            }
            manager.createNotificationChannel(channel)
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
                .setCategory(android.app.Notification.CATEGORY_REMINDER)
                .setAutoCancel(true)
                .setPriority(android.app.Notification.PRIORITY_HIGH)
                .build()
        } else {
            android.app.Notification.Builder(context)
                .setSmallIcon(android.R.drawable.ic_popup_reminder)
                .setContentTitle("Time for ${saved.habitName}")
                .setContentText("Your ${saved.habitName} habit is scheduled now.")
                .setContentIntent(open)
                .setCategory(android.app.Notification.CATEGORY_REMINDER)
                .setAutoCancel(true)
                .setPriority(android.app.Notification.PRIORITY_HIGH)
                .build()
        }
        manager.notify(10000 + (id % 10000).toInt(), notification)
    }
}
