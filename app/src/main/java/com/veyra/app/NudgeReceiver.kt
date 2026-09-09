package com.veyra.app

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

class NudgeReceiver:BroadcastReceiver(){
    override fun onReceive(context:Context,intent:Intent){
        val name=intent.getStringExtra("name")?:"Nudge"
        val id=intent.getLongExtra("id",0)
        val interval=intent.getIntExtra("interval",60)
        val start=intent.getIntExtra("start",8)
        val end=intent.getIntExtra("end",22)
        val manager=context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if(Build.VERSION.SDK_INT>=26)manager.createNotificationChannel(NotificationChannel("veyra_nudges","Veyra Nudges",NotificationManager.IMPORTANCE_DEFAULT))
        if(Build.VERSION.SDK_INT<33 || context.checkSelfPermission("android.permission.POST_NOTIFICATIONS")==android.content.pm.PackageManager.PERMISSION_GRANTED){
            manager.notify(50000+(id%10000).toInt(),NotificationCompat.Builder(context,"veyra_nudges").setSmallIcon(com.veyra.app.R.drawable.ic_launcher).setContentTitle(name).setContentText("A small nudge from Veyra.").setAutoCancel(true).setPriority(NotificationCompat.PRIORITY_DEFAULT).build())
        }
        val next=Nudge(id,name,interval,start,end,true)
        NudgeScheduler.schedule(context,next)
    }
}
