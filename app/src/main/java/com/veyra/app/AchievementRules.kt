package com.veyra.app

object AchievementRules {
    fun unlockedIds(xp: Int, streak: Int, hasCompletedHabit: Boolean, hasJournal: Boolean): Set<String> = buildSet {
        if (hasCompletedHabit) add("first_step")
        if (xp >= 100) add("century")
        if (streak >= 7) add("week_warrior")
        if (hasJournal) add("journalist")
    }
}
