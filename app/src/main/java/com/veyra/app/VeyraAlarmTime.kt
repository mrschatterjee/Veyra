package com.veyra.app

import java.util.Calendar

/**
 * Device-clock based alarm calculations shared by Veyra reminders and nudges.
 * Calendar uses the phone's current locale/time-zone and system wall clock.
 */
object VeyraAlarmTime {
    fun nextDaily(hour: Int, minute: Int, nowMillis: Long = System.currentTimeMillis()): Long {
        val now = Calendar.getInstance().apply { timeInMillis = nowMillis }
        val next = (now.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, hour.coerceIn(0, 23))
            set(Calendar.MINUTE, minute.coerceIn(0, 59))
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= nowMillis) add(Calendar.DAY_OF_YEAR, 1)
        }
        return next.timeInMillis
    }

    /**
     * Returns the next nudge on a fixed device-clock grid.
     * Example: a 30-minute nudge window starting at 08:00 fires at 08:00,
     * 08:30, 09:00 ... rather than drifting from the time the user saved it.
     */
    fun nextNudge(
        intervalMinutes: Int,
        startHour: Int,
        endHour: Int,
        nowMillis: Long = System.currentTimeMillis()
    ): Long {
        val interval = intervalMinutes.coerceAtLeast(15)
        val start = startHour.coerceIn(0, 23)
        val end = endHour.coerceIn(0, 23)
        if (start == end) return nextAlignedAllDay(interval, nowMillis)

        val now = Calendar.getInstance().apply { timeInMillis = nowMillis }
        val windowStart = currentWindowStart(now, start, end)
        val first = nextGridPoint(windowStart, interval, nowMillis)
        return if (isInsideWindow(first, start, end)) first else nextWindowStart(now, start)
    }

    /**
     * Recomputes from the current device clock after an alarm is delivered.
     * Missed slots are skipped and the next notification stays on the same
     * wall-clock grid instead of drifting with Android's delivery delay.
     */
    fun nextNudgeAfter(
        scheduledAtMillis: Long,
        intervalMinutes: Int,
        startHour: Int,
        endHour: Int,
        nowMillis: Long = System.currentTimeMillis()
    ): Long = nextNudge(intervalMinutes, startHour, endHour, nowMillis)

    private fun nextAlignedAllDay(interval: Int, nowMillis: Long): Long {
        val now = Calendar.getInstance().apply { timeInMillis = nowMillis }
        val minutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
        val nextSlot = ((minutes / interval) + 1) * interval
        val next = (now.clone() as Calendar).apply {
            if (nextSlot >= 24 * 60) {
                add(Calendar.DAY_OF_YEAR, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
            } else {
                set(Calendar.HOUR_OF_DAY, nextSlot / 60)
                set(Calendar.MINUTE, nextSlot % 60)
            }
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return next.timeInMillis
    }

    private fun currentWindowStart(now: Calendar, start: Int, end: Int): Calendar {
        val result = (now.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, start)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        if (start < end) {
            if (now.get(Calendar.HOUR_OF_DAY) < start) result.add(Calendar.DAY_OF_YEAR, -1)
        } else {
            // Overnight window, e.g. 22:00–07:00.
            val hour = now.get(Calendar.HOUR_OF_DAY)
            if (hour < start && hour >= end) return result // daytime gap; next window starts today
            if (hour < start) result.add(Calendar.DAY_OF_YEAR, -1)
        }
        return result
    }

    private fun nextWindowStart(now: Calendar, start: Int): Long {
        val result = (now.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, start)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= now.timeInMillis) add(Calendar.DAY_OF_YEAR, 1)
        }
        return result.timeInMillis
    }

    private fun nextGridPoint(windowStart: Calendar, interval: Int, nowMillis: Long): Long {
        val candidate = windowStart.clone() as Calendar
        if (candidate.timeInMillis <= nowMillis) {
            val elapsed = nowMillis - candidate.timeInMillis
            val slots = (elapsed / (interval * 60_000L)) + 1L
            candidate.add(Calendar.MINUTE, (slots * interval).toInt())
        }
        candidate.set(Calendar.SECOND, 0)
        candidate.set(Calendar.MILLISECOND, 0)
        return candidate.timeInMillis
    }

    private fun isInsideWindow(timeMillis: Long, start: Int, end: Int): Boolean {
        val hour = Calendar.getInstance().apply { timeInMillis = timeMillis }.get(Calendar.HOUR_OF_DAY)
        return if (start < end) hour >= start && hour < end else hour >= start || hour < end
    }
}
