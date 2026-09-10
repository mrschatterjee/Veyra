package com.veyra.app

import java.util.Calendar
import java.util.TimeZone
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class VeyraAlarmTimeTest {
    private val originalZone = TimeZone.getDefault()

    @Before
    fun setUp() {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Kolkata"))
    }

    @After
    fun tearDown() {
        TimeZone.setDefault(originalZone)
    }

    @Test
    fun alignsNormalWindowToDeviceClockGrid() {
        val now = local(2026, 9, 10, 10, 7)
        assertEquals(local(2026, 9, 10, 10, 30), VeyraAlarmTime.nextNudge(30, 8, 22, now))
    }

    @Test
    fun startsAtWindowBoundaryBeforeWindow() {
        val now = local(2026, 9, 10, 7, 10)
        assertEquals(local(2026, 9, 10, 8, 0), VeyraAlarmTime.nextNudge(30, 8, 22, now))
    }

    @Test
    fun movesToNextDayAfterWindow() {
        val now = local(2026, 9, 10, 22, 10)
        assertEquals(local(2026, 9, 11, 8, 0), VeyraAlarmTime.nextNudge(30, 8, 22, now))
    }

    @Test
    fun handlesOvernightWindow() {
        val now = local(2026, 9, 11, 6, 15)
        assertEquals(local(2026, 9, 11, 6, 30), VeyraAlarmTime.nextNudge(30, 22, 7, now))

        val later = local(2026, 9, 11, 6, 45)
        assertEquals(local(2026, 9, 11, 22, 0), VeyraAlarmTime.nextNudge(30, 22, 7, later))
    }

    @Test
    fun delayedDeliveryDoesNotCreateDrift() {
        val scheduled = local(2026, 9, 10, 10, 0)
        val deliveredLate = local(2026, 9, 10, 10, 3)
        assertEquals(local(2026, 9, 10, 10, 30), VeyraAlarmTime.nextNudgeAfter(30, 30, 8, 22, deliveredLate))
        assertEquals(scheduled, local(2026, 9, 10, 10, 0))
    }

    private fun local(year: Int, month: Int, day: Int, hour: Int, minute: Int): Long = Calendar.getInstance().apply {
        clear()
        set(year, month, day, hour, minute, 0)
    }.timeInMillis
}
