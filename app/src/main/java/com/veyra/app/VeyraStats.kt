package com.veyra.app

object VeyraStats {
    private const val XP_PER_LEVEL = 100

    fun level(xp: Int): Int = 1 + xp.coerceAtLeast(0) / XP_PER_LEVEL

    fun levelProgress(xp: Int): Int = xp.coerceAtLeast(0) % XP_PER_LEVEL

    fun xpToNextLevel(xp: Int): Int = XP_PER_LEVEL - levelProgress(xp)

    fun levelTitle(level: Int): String = when {
        level >= 20 -> "Universe Builder"
        level >= 10 -> "Cosmic Architect"
        level >= 5 -> "Star Forger"
        level >= 3 -> "Pathfinder"
        else -> "Explorer"
    }

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

    fun bestStreakFromDailyCompletion(completedDays: List<Boolean>): Int {
        var best = 0
        var current = 0
        for (completed in completedDays) {
            if (completed) {
                current++
                best = maxOf(best, current)
            } else {
                current = 0
            }
        }
        return best
    }
}
