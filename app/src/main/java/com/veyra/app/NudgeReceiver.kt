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

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= 26) {
            manager.createNotificationChannel(
                NotificationChannel("veyra_nudges", "Veyra Nudges", NotificationManager.IMPORTANCE_DEFAULT)
            )
        }
        val allowed = Build.VERSION.SDK_INT < 33 || context.checkSelfPermission("android.permission.POST_NOTIFICATIONS") == PackageManager.PERMISSION_GRANTED
        if (allowed) {
            val builder = if (Build.VERSION.SDK_INT >= 26) {
                android.app.Notification.Builder(context, "veyra_nudges")
            } else {
                @Suppress("DEPRECATION") android.app.Notification.Builder(context)
            }
            val notification = builder
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(saved.name)
                .setContentText("A small nudge from Veyra.")
                .setAutoCancel(true)
                .setContentIntent(PendingIntent.getActivity(context, 0, Intent(context, MainActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE))
                .build()
            manager.notify(50000 + (id % 10000).toInt(), notification)
        }

        // Re-read the saved record so edits made after the previous alarm take effect.
        NudgeScheduler.schedule(context, saved)
    }
}
