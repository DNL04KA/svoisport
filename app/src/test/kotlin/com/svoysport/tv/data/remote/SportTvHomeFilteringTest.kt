package com.svoysport.tv.data.remote

import com.svoysport.tv.domain.model.Competition
import com.svoysport.tv.domain.model.MatchItem
import com.svoysport.tv.domain.model.Team
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SportTvHomeFilteringTest {
    private val repository = SportTvMatchRepository(
        api = com.svoysport.tv.data.remote.sporttv.SportTvApi()
    )

    @Test fun `home keeps live and future matches but removes expired non archive events`() {
        val now = 1_000_000L
        assertTrue(repository.isActiveHomeMatch(match(isLive = true, startsAt = now - 10_000), now))
        assertTrue(repository.isActiveHomeMatch(match(isLive = false, startsAt = now + 10_000), now))
        assertFalse(repository.isActiveHomeMatch(match(isLive = false, startsAt = now - 10_000), now))
    }

    private fun match(isLive: Boolean, startsAt: Long) = MatchItem(
        id = "id-$isLive-$startsAt",
        title = "Матч",
        description = "",
        homeTeam = Team("h", "H"),
        awayTeam = Team("a", "A"),
        competition = Competition("football", "Футбол"),
        isLive = isLive,
        startTimeMs = startsAt,
        thumbnailUrl = ""
    )
}
