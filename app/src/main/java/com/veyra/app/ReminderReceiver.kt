package com.veyra.app

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val id=intent?.getLongExtra("habit_id",-1L)?:-1L
        val saved=if(id>=0)HabitReminderStore(context).get(id) else null
        if(saved==null||!saved.enabled)return
        val manager=context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId="veyra_reminders"
        if(Build.VERSION.SDK_INT>=26)manager.createNotificationChannel(NotificationChannel(channelId,"Veyra reminders",NotificationManager.IMPORTANCE_HIGH))
        if(Build.VERSION.SDK_INT>=33&&context.checkSelfPermission("android.permission.POST_NOTIFICATIONS")!=android.content.pm.PackageManager.PERMISSION_GRANTED)return
        val open=PendingIntent.getActivity(context,0,Intent(context,MainActivity::class.java),PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val notification=NotificationCompat.Builder(context,channelId).setSmallIcon(android.R.drawable.ic_popup_reminder).setContentTitle("Time for ${saved.habitName}").setContentText("Your ${saved.habitName} habit is scheduled now.").setContentIntent(open).setAutoCancel(true).setPriority(NotificationCompat.PRIORITY_HIGH).build()
        manager.notify(10000+(id%10000).toInt(),notification)
        HabitReminderScheduler.schedule(context,saved)
    }
}
