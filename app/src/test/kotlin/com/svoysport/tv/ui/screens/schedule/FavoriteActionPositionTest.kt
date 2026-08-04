package com.svoysport.tv.ui.screens.schedule

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FavoriteActionPositionTest {
    private val match = ScheduleMatch(
        id = "1", time = "12:00", title = "Матч", competition = "Лига", sport = SportFilter.FOOTBALL
    )

    @Test
    fun `favorite action waits for both row and column coordinates`() {
        assertFalse(isFavoriteActionPositionReady(match, null, null))
        assertFalse(isFavoriteActionPositionReady(match, 240f, null))
        assertFalse(isFavoriteActionPositionReady(match, null, 120f))
        assertTrue(isFavoriteActionPositionReady(match, 240f, 120f))
    }
}
