package com.veyra.app

object VeyraStats {
    fun level(xp: Int): Int = 1 + xp.coerceAtLeast(0) / 100
    fun levelProgress(xp: Int): Int = xp.coerceAtLeast(0) % 100

    fun completionRate(completed: Int, possible: Int): Int {
        if (possible <= 0) return 0
        return ((completed.coerceAtLeast(0).toFloat() / possible) * 100f).toInt().coerceIn(0, 100)
    }

    fun streakFromDailyCompletion(completedDays: List<Boolean>): Int {
        var streak = 0
        for (completed in completedDays) {
            if (!completed) break
            streak++
        }
        return streak
    }
}
