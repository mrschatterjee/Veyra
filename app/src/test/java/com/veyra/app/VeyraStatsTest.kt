package com.veyra.app

import org.junit.Assert.assertEquals
import org.junit.Test

class VeyraStatsTest {
    @Test fun levelProgressIsStable() {
        assertEquals(1, VeyraStats.level(0))
        assertEquals(3, VeyraStats.level(235))
        assertEquals(35, VeyraStats.levelProgress(235))
        assertEquals(65, VeyraStats.xpToNextLevel(235))
    }

    @Test fun levelBoundariesAreCorrect() {
        assertEquals(1, VeyraStats.level(99))
        assertEquals(2, VeyraStats.level(100))
        assertEquals(0, VeyraStats.levelProgress(100))
        assertEquals(1, VeyraStats.xpToNextLevel(99))
        assertEquals(100, VeyraStats.xpToNextLevel(200))
    }

    @Test fun levelTitlesScaleWithProgression() {
        assertEquals("Explorer", VeyraStats.levelTitle(1))
        assertEquals("Pathfinder", VeyraStats.levelTitle(3))
        assertEquals("Star Forger", VeyraStats.levelTitle(5))
        assertEquals("Cosmic Architect", VeyraStats.levelTitle(10))
        assertEquals("Universe Builder", VeyraStats.levelTitle(20))
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
