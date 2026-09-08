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
        val manager=context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId="veyra_reminders"
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O)manager.createNotificationChannel(NotificationChannel(channelId,"Veyra reminders",NotificationManager.IMPORTANCE_DEFAULT))
        val open=PendingIntent.getActivity(context,0,Intent(context,MainActivity::class.java),PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val n=if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O)android.app.Notification.Builder(context,channelId) else android.app.Notification.Builder(context)
        n.setSmallIcon(android.R.drawable.ic_popup_reminder).setContentTitle("Veyra").setContentText("Build your universe. Take one small action.").setContentIntent(open).setAutoCancel(true)
        manager.notify(1001,n.build())
        if(VeyraReminder.isEnabled(context))VeyraReminder.schedule(context,VeyraReminder.hour(context),VeyraReminder.minute(context))
    }
}
