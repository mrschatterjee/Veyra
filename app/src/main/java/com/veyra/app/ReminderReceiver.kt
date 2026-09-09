package com.veyra.app

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "veyra_reminders"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) manager.createNotificationChannel(NotificationChannel(channelId, "Veyra reminders", NotificationManager.IMPORTANCE_DEFAULT))
        val id = intent?.getLongExtra("habit_id", -1L) ?: -1L
        val task = intent?.getStringExtra("habit_name").orEmpty().ifBlank { "Take one small action" }
        val hour = intent?.getIntExtra("hour", 20) ?: 20
        val minute = intent?.getIntExtra("minute", 0) ?: 0
        val open = PendingIntent.getActivity(context, 0, Intent(context, MainActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val n = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) android.app.Notification.Builder(context, channelId) else android.app.Notification.Builder(context)
        n.setSmallIcon(android.R.drawable.ic_popup_reminder).setContentTitle("Time for $task").setContentText("Your $task habit is scheduled now.").setContentIntent(open).setAutoCancel(true)
        manager.notify(1001 + (id % 10000).toInt().coerceAtLeast(0), n.build())
        if (id >= 0L) HabitReminderScheduler.schedule(context, HabitReminder(id, task, hour, minute, true))
    }
}
