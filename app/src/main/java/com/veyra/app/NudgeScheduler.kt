package com.veyra.app

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.util.Calendar

object NudgeScheduler {
    private const val BASE = 30000

    fun schedule(context: Context, n: Nudge) {
        if (!n.enabled) {
            cancel(context, n.id)
            return
        }
        val minutes = n.intervalMinutes.coerceAtLeast(15)
        val start = n.startHour.coerceIn(0, 23)
        val end = n.endHour.coerceIn(0, 23)
        val alarm = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val now = Calendar.getInstance()
        val next = Calendar.getInstance().apply {
            add(Calendar.MINUTE, minutes)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // start == end means the nudge is allowed all day.
        val allDay = start == end
        if (!allDay && !inWindow(next.get(Calendar.HOUR_OF_DAY), start, end)) {
            // Move to the next opening of the configured active window.
            next.set(Calendar.HOUR_OF_DAY, start)
            next.set(Calendar.MINUTE, 0)
            next.set(Calendar.SECOND, 0)
            next.set(Calendar.MILLISECOND, 0)
            if (next.timeInMillis <= now.timeInMillis) next.add(Calendar.DAY_OF_YEAR, 1)
        }

        alarm.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            next.timeInMillis,
            pending(context, n.id, n.name, minutes, start, end)
        )
    }

    private fun inWindow(hour: Int, start: Int, end: Int): Boolean =
        if (start < end) hour >= start && hour < end else hour >= start || hour < end

    fun cancel(context: Context, id: Long) {
        (context.getSystemService(Context.ALARM_SERVICE) as AlarmManager)
            .cancel(pending(context, id, "", 60, 8, 22))
    }

    fun rescheduleAll(context: Context) {
        NudgeStore(context).all().filter { it.enabled }.forEach { schedule(context, it) }
    }

    private fun pending(context: Context, id: Long, name: String, interval: Int, start: Int, end: Int) =
        PendingIntent.getBroadcast(
            context,
            BASE + (id % 100000).toInt(),
            Intent(context, NudgeReceiver::class.java).apply {
                putExtra("id", id)
                putExtra("name", name)
                putExtra("interval", interval)
                putExtra("start", start)
                putExtra("end", end)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
}
