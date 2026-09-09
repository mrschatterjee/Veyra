package com.veyra.app

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.util.Calendar

object NudgeScheduler {
    private const val BASE=30000
    fun schedule(context:Context,n:Nudge){
        val alarm=context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val now=Calendar.getInstance()
        val next=Calendar.getInstance().apply{add(Calendar.MINUTE,n.intervalMinutes.coerceAtLeast(15))}
        if(next.get(Calendar.HOUR_OF_DAY) !in n.startHour..n.endHour) { set(Calendar.HOUR_OF_DAY,n.startHour);set(Calendar.MINUTE,0);set(Calendar.SECOND,0);set(Calendar.MILLISECOND,0);if(timeInMillis<=System.currentTimeMillis())add(Calendar.DAY_OF_YEAR,1) }
        alarm.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,next.timeInMillis,pending(context,n.id,n.name,n.intervalMinutes,n.startHour,n.endHour))
    }
    fun cancel(context:Context,id:Long){(context.getSystemService(Context.ALARM_SERVICE) as AlarmManager).cancel(pending(context,id,"",0,0,0))}
    fun rescheduleAll(context:Context){NudgeStore(context).all().filter{it.enabled}.forEach{schedule(context,it)}}
    private fun pending(context:Context,id:Long,name:String,interval:Int,start:Int,end:Int)=PendingIntent.getBroadcast(context,BASE+(id%100000).toInt(),Intent(context,NudgeReceiver::class.java).apply{putExtra("id",id);putExtra("name",name);putExtra("interval",interval);putExtra("start",start);putExtra("end",end)},PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
}
