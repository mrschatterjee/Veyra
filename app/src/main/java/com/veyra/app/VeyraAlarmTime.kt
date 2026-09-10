package com.veyra.app

import java.util.Calendar

/** Local-time calculations shared by Veyra reminders and nudges. */
object VeyraAlarmTime {
    fun nextDaily(hour: Int, minute: Int, nowMillis: Long = System.currentTimeMillis()): Long {
        val next = Calendar.getInstance().apply {
            timeInMillis = nowMillis
            set(Calendar.HOUR_OF_DAY, hour.coerceIn(0, 23))
            set(Calendar.MINUTE, minute.coerceIn(0, 59))
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= nowMillis) add(Calendar.DAY_OF_YEAR, 1)
        }
        return next.timeInMillis
    }

    fun nextNudge(intervalMinutes: Int, startHour: Int, endHour: Int, nowMillis: Long = System.currentTimeMillis()): Long {
        val interval = intervalMinutes.coerceAtLeast(15)
        if (startHour == endHour) return plusMinutes(nowMillis, interval)
        val candidate = plusMinutes(nowMillis, interval)
        return normalizeToWindow(candidate, startHour, endHour)
    }

    /** Keeps the original cadence instead of drifting after a delayed alarm delivery. */
    fun nextNudgeAfter(scheduledAtMillis: Long, intervalMinutes: Int, startHour: Int, endHour: Int, nowMillis: Long = System.currentTimeMillis()): Long {
        val interval = intervalMinutes.coerceAtLeast(15)
        if (startHour == endHour) {
            var next = plusMinutes(scheduledAtMillis, interval)
            while (next <= nowMillis) next = plusMinutes(next, interval)
            return next
        }
        var next = normalizeToWindow(plusMinutes(scheduledAtMillis, interval), startHour, endHour)
        while (next <= nowMillis) {
            next = normalizeToWindow(plusMinutes(next, interval), startHour, endHour)
        }
        return next
    }

    private fun plusMinutes(baseMillis: Long, minutes: Int): Long = Calendar.getInstance().apply {
        timeInMillis = baseMillis
        add(Calendar.MINUTE, minutes)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    private fun normalizeToWindow(candidateMillis: Long, startHour: Int, endHour: Int): Long {
        val start = startHour.coerceIn(0, 23)
        val end = endHour.coerceIn(0, 23)
        val candidate = Calendar.getInstance().apply { timeInMillis = candidateMillis }
        val hour = candidate.get(Calendar.HOUR_OF_DAY)
        val inside = if (start < end) hour >= start && hour < end else hour >= start || hour < end
        if (inside) return candidateMillis

        if (start < end) {
            if (hour >= end) candidate.add(Calendar.DAY_OF_YEAR, 1)
            candidate.set(Calendar.HOUR_OF_DAY, start)
        } else {
            // Overnight window such as 22:00–07:00: outside means 07:00–22:00.
            candidate.set(Calendar.HOUR_OF_DAY, start)
            if (hour >= end && hour < start) {
                // Same-day start is the next valid window.
            } else {
                candidate.add(Calendar.DAY_OF_YEAR, 1)
            }
        }
        candidate.set(Calendar.MINUTE, 0)
        candidate.set(Calendar.SECOND, 0)
        candidate.set(Calendar.MILLISECOND, 0)
        return candidate.timeInMillis
    }
}
