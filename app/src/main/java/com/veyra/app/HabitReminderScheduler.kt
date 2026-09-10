package com.veyra.app

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build

object HabitReminderScheduler {
    private const val BASE = 12000

    fun schedule(context: Context, item: HabitReminder) {
        if (!item.enabled) { cancel(context, item.habitId); return }
        val alarm = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val triggerAt = VeyraAlarmTime.nextDaily(item.hour, item.minute)
        val intent = pendingIntent(context, item.habitId, item.habitName, triggerAt)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && alarm.canScheduleExactAlarms()) {
            alarm.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, intent)
        } else {
            alarm.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, intent)
        }
    }

    fun cancel(context: Context, habitId: Long) {
        val alarm = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarm.cancel(pendingIntent(context, habitId, "", 0L))
    }

    fun rescheduleAll(context: Context) {
        HabitReminderStore(context).all().filter { it.enabled }.forEach { schedule(context, it) }
    }

    private fun pendingIntent(context: Context, id: Long, name: String, triggerAt: Long): PendingIntent = PendingIntent.getBroadcast(
        context, BASE + (id % 100000).toInt(),
        Intent(context, ReminderReceiver::class.java).apply {
            putExtra("habit_id", id)
            putExtra("habit_name", name)
            putExtra("scheduled_at", triggerAt)
        }, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
}
