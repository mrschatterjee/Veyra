package com.veyra.app

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build

object NudgeScheduler {
    private const val BASE = 30000

    fun schedule(context: Context, n: Nudge) {
        if (!n.enabled) { cancel(context, n.id); return }
        val interval = n.intervalMinutes.coerceAtLeast(15)
        val start = n.startHour.coerceIn(0, 23)
        val end = n.endHour.coerceIn(0, 23)
        scheduleAt(context, n, VeyraAlarmTime.nextNudge(interval, start, end))
    }

    fun scheduleNext(context: Context, n: Nudge, scheduledAt: Long) {
        if (!n.enabled) { cancel(context, n.id); return }
        val next = VeyraAlarmTime.nextNudgeAfter(
            scheduledAt,
            n.intervalMinutes.coerceAtLeast(15),
            n.startHour.coerceIn(0, 23),
            n.endHour.coerceIn(0, 23)
        )
        scheduleAt(context, n, next)
    }

    private fun scheduleAt(context: Context, n: Nudge, triggerAt: Long) {
        val alarm = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = pending(context, n.id, n.name, n.intervalMinutes.coerceAtLeast(15), n.startHour.coerceIn(0, 23), n.endHour.coerceIn(0, 23), triggerAt)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && alarm.canScheduleExactAlarms()) {
            alarm.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, intent)
        } else {
            alarm.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, intent)
        }
    }

    fun cancel(context: Context, id: Long) {
        (context.getSystemService(Context.ALARM_SERVICE) as AlarmManager).cancel(pending(context, id, "", 60, 8, 22, 0L))
    }

    fun rescheduleAll(context: Context) {
        NudgeStore(context).all().filter { it.enabled }.forEach { schedule(context, it) }
    }

    private fun pending(context: Context, id: Long, name: String, interval: Int, start: Int, end: Int, scheduledAt: Long) = PendingIntent.getBroadcast(
        context, BASE + (id % 100000).toInt(), Intent(context, NudgeReceiver::class.java).apply {
            putExtra("id", id)
            putExtra("name", name)
            putExtra("interval", interval)
            putExtra("start", start)
            putExtra("end", end)
            putExtra("scheduled_at", scheduledAt)
        }, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
}
