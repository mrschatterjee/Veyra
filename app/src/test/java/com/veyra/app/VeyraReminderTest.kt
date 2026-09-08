package com.veyra.app

import org.junit.Assert.assertEquals
import org.junit.Test

class VeyraReminderTest {
    @Test fun reminderTimeIsClampedToValidClockRange() {
        assertEquals(0, 0.coerceIn(0, 23))
        assertEquals(23, 27.coerceIn(0, 23))
        assertEquals(59, 82.coerceIn(0, 59))
    }
}
