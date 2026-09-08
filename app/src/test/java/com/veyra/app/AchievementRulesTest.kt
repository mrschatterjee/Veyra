package com.veyra.app

import org.junit.Assert.assertEquals
import org.junit.Test

class AchievementRulesTest {
    @Test fun noProgressHasNoAchievements() {
        assertEquals(emptySet<String>(), AchievementRules.unlockedIds(0, 0, false, false))
    }

    @Test fun progressUnlocksExpectedAchievements() {
        assertEquals(setOf("first_step", "century", "week_warrior", "journalist"), AchievementRules.unlockedIds(100, 7, true, true))
    }
}
