package com.veyra.app

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.util.Calendar

object HabitReminderScheduler {
    private const val BASE = 12000
    fun schedule(context: Context, item: HabitReminder) {
        val alarm = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val next = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, item.hour.coerceIn(0,23))
            set(Calendar.MINUTE, item.minute.coerceIn(0,59))
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) add(Calendar.DAY_OF_YEAR, 1)
        }
        alarm.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, next.timeInMillis, pendingIntent(context, item.habitId, item.habitName))
    }
    fun cancel(context: Context, habitId: Long) {
        val alarm = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarm.cancel(pendingIntent(context, habitId, ""))
    }
    fun rescheduleAll(context: Context) {
        HabitReminderStore(context).all().filter { it.enabled }.forEach { schedule(context, it) }
    }
    private fun pendingIntent(context: Context, id: Long, name: String): PendingIntent = PendingIntent.getBroadcast(
        context, BASE + (id % 100000).toInt(),
        Intent(context, ReminderReceiver::class.java).apply {
            putExtra("habit_id", id)
            putExtra("habit_name", name)
        }, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
}
