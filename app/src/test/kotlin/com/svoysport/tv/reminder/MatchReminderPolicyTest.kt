package com.svoysport.tv.reminder

import com.svoysport.tv.domain.model.Competition
import com.svoysport.tv.domain.model.MatchItem
import com.svoysport.tv.domain.model.Team
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MatchReminderPolicyTest {
    private val now = 1_000_000L

    @Test
    fun `logged in subscriber can set reminder for paid future match`() {
        assertTrue(canScheduleMatchReminder(match(startsInMinutes = 30), true, true, now))
    }

    @Test
    fun `free future match only requires login`() {
        assertTrue(canScheduleMatchReminder(match(startsInMinutes = 30, paid = false), true, false, now))
    }

    @Test
    fun `reminder is hidden for anonymous live archive near and ended matches`() {
        assertFalse(canScheduleMatchReminder(match(30), false, true, now))
        assertFalse(canScheduleMatchReminder(match(30, live = true), true, true, now))
        assertFalse(canScheduleMatchReminder(match(30, archive = true), true, true, now))
        assertFalse(canScheduleMatchReminder(match(5), true, true, now))
        assertFalse(canScheduleMatchReminder(match(-1), true, true, now))
    }

    @Test
    fun `paid future match requires active subscription`() {
        assertFalse(canScheduleMatchReminder(match(30), true, false, now))
    }

    @Test
    fun `notification is scheduled five minutes before start`() {
        assertEquals(now + 25 * 60_000L, reminderTriggerAt(now + 30 * 60_000L))
    }

    private fun match(
        startsInMinutes: Int,
        paid: Boolean = true,
        live: Boolean = false,
        archive: Boolean = false
    ) = MatchItem(
        id = "match",
        title = "Матч",
        description = "",
        homeTeam = Team("home", "Дом"),
        awayTeam = Team("away", "Гости"),
        competition = Competition("league", "Лига"),
        isLive = live,
        startTimeMs = now + startsInMinutes * 60_000L,
        thumbnailUrl = "",
        isSubscriptionRequired = paid,
        durationSec = if (archive) 60L else 0L
    )
}
