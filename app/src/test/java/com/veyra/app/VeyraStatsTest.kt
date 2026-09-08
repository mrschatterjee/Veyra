package com.veyra.app

import org.junit.Assert.assertEquals
import org.junit.Test

class VeyraStatsTest {
    @Test fun levelProgressIsStable() {
        assertEquals(1, VeyraStats.level(0))
        assertEquals(3, VeyraStats.level(235))
        assertEquals(35, VeyraStats.levelProgress(235))
    }

    @Test fun completionRateIsBounded() {
        assertEquals(50, VeyraStats.completionRate(5, 10))
        assertEquals(0, VeyraStats.completionRate(0, 0))
        assertEquals(100, VeyraStats.completionRate(20, 10))
    }

    @Test fun streakStopsAtFirstMiss() {
        assertEquals(3, VeyraStats.streakFromDailyCompletion(listOf(true, true, true, false, true)))
        assertEquals(0, VeyraStats.streakFromDailyCompletion(listOf(false, true)))
    }
}
