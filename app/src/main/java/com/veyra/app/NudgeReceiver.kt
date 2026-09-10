package com.veyra.app

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build

class NudgeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getLongExtra("id", -1L)
        if (id < 0L) return
        val saved = NudgeStore(context).all().firstOrNull { it.id == id }
        if (saved == null || !saved.enabled) return

        val scheduledAt = intent.getLongExtra("scheduled_at", System.currentTimeMillis())
        // Re-arm from the intended trigger time, not from delivery time, so a delayed
        // Android alarm does not slowly move the user's entire nudge schedule.
        NudgeScheduler.scheduleNext(context, saved, scheduledAt)

        val allowed = Build.VERSION.SDK_INT < 33 || context.checkSelfPermission("android.permission.POST_NOTIFICATIONS") == PackageManager.PERMISSION_GRANTED
        if (!allowed) return

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "veyra_nudges_v2"
        if (Build.VERSION.SDK_INT >= 26) {
            val channel = NotificationChannel(channelId, "Veyra Nudges", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Scheduled nudges from Veyra"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 180, 100, 180)
            }
            manager.createNotificationChannel(channel)
        }
        val builder = if (Build.VERSION.SDK_INT >= 26) {
            android.app.Notification.Builder(context, channelId)
        } else {
            @Suppress("DEPRECATION") android.app.Notification.Builder(context)
        }
        val notification = builder
            .setSmallIcon(R.drawable.ic_launcher)
            .setContentTitle(saved.name)
            .setContentText("A small nudge from Veyra.")
            .setAutoCancel(true)
            .setCategory(android.app.Notification.CATEGORY_REMINDER)
            .setPriority(android.app.Notification.PRIORITY_HIGH)
            .setContentIntent(PendingIntent.getActivity(context, 0, Intent(context, MainActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE))
            .build()
        manager.notify(50000 + (id % 10000).toInt(), notification)
    }
}
