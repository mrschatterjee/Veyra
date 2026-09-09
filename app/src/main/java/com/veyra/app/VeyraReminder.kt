package com.veyra.app

import android.content.Context

object VeyraReminder {
    private fun store(context: Context)=HabitReminderStore(context)
    fun isEnabled(context: Context):Boolean=store(context).all().any{it.enabled}
    fun hour(context: Context):Int=store(context).all().firstOrNull{it.enabled}?.hour?:20
    fun minute(context: Context):Int=store(context).all().firstOrNull{it.enabled}?.minute?:0
    fun habitId(context: Context):Long=store(context).all().firstOrNull{it.enabled}?.habitId?:-1L
    fun habitName(context: Context):String=store(context).all().firstOrNull{it.enabled}?.habitName?:""
    fun set(context:Context,enabled:Boolean,hour:Int=hour(context),minute:Int=minute(context),habitId:Long=habitId(context),habitName:String=habitName(context)){
        if(habitId<0)return
        store(context).save(HabitReminder(habitId,habitName,hour,minute,enabled))
    }
    fun schedule(context:Context,hour:Int,minute:Int){HabitReminderScheduler.rescheduleAll(context)}
    fun rescheduleAll(context:Context)=HabitReminderScheduler.rescheduleAll(context)
    fun cancel(context:Context){store(context).all().forEach{HabitReminderScheduler.cancel(context,it.habitId)}}
}
