package com.svoysport.tv.ui.screens.home

import com.svoysport.tv.domain.model.Competition
import com.svoysport.tv.domain.model.MatchItem
import com.svoysport.tv.domain.model.Team
import org.junit.Assert.assertEquals
import org.junit.Test

class HomeRowLimitTest {
    @Test fun `home row displays at most ten matches before watch more`() {
        val matches = (1..14).map { index -> match(index.toString()) }
        assertEquals(10, homeRowMatches(matches).size)
        assertEquals("10", homeRowMatches(matches).last().id)
    }

    private fun match(id: String) = MatchItem(
        id = id,
        title = id,
        description = "",
        homeTeam = Team("h", "H"),
        awayTeam = Team("a", "A"),
        competition = Competition("football", "Футбол"),
        isLive = false,
        startTimeMs = Long.MAX_VALUE,
        thumbnailUrl = ""
    )
}
