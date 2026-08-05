package com.svoysport.tv.reminder

import org.junit.Assert.assertEquals
import org.junit.Test

class ReminderAlertTest {
    @Test
    fun `in app reminder contains match copy and lasts ten seconds`() {
        val alert = reminderAlert(
            matchId = "match-1",
            title = "Евролига. Олимпиакос - Анадолу Эфес",
            category = "Баскетбол",
            createdAtMs = 123L
        )

        assertEquals("match-1", alert.matchId)
        assertEquals("Баскетбол", alert.category)
        assertEquals(123L, alert.createdAtMs)
        assertEquals(10_000L, REMINDER_ALERT_DURATION_MS)
    }
}
