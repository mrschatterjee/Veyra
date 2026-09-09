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
        val name = intent.getStringExtra("name") ?: "Nudge"
        val id = intent.getLongExtra("id", 0L)
        val interval = intent.getIntExtra("interval", 60)
        val start = intent.getIntExtra("start", 8)
        val end = intent.getIntExtra("end", 22)
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
                .setContentTitle(name)
                .setContentText("A small nudge from Veyra.")
                .setAutoCancel(true)
                .setContentIntent(PendingIntent.getActivity(context, 0, Intent(context, MainActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE))
                .build()
            manager.notify(50000 + (id % 10000).toInt(), notification)
        }
        NudgeScheduler.schedule(context, Nudge(id, name, interval, start, end, true))
    }
}
