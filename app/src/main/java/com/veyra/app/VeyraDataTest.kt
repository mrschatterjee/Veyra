package com.veyra.app

import org.junit.Assert.assertEquals
import org.junit.Test

class VeyraDataTest {
    @Test fun habitRoundTripModelIsStable() {
        val original = Habit(42L, "Read", true)
        assertEquals(42L, original.id)
        assertEquals("Read", original.name)
        assertEquals(true, original.done)
    }
    @Test fun levelProgressIsDeterministic() {
        val xp = 235
        assertEquals(3, 1 + xp / 100)
        assertEquals(35, xp % 100)
    }
}
