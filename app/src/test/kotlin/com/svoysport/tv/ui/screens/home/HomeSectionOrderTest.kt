package com.svoysport.tv.ui.screens.home

import com.svoysport.tv.domain.model.HomeSection
import com.svoysport.tv.domain.model.MatchItem
import com.svoysport.tv.domain.model.Team
import com.svoysport.tv.domain.model.Competition
import org.junit.Assert.assertEquals
import org.junit.Test

class HomeSectionOrderTest {
    private fun testMatch(id: String) = MatchItem(
        id = id,
        title = id,
        description = "",
        homeTeam = Team("h", "Home"),
        awayTeam = Team("a", "Away"),
        competition = Competition("c", "Лига"),
        isLive = id == "l",
        startTimeMs = 0L,
        thumbnailUrl = ""
    )

    @Test
    fun `online precedes upcoming and empty sections are hidden`() {
        val sections = listOf(
            HomeSection("football", "Футбол", emptyList()),
            HomeSection("upcoming", "Предстоящие трансляции", listOf(testMatch("u"))),
            HomeSection("live", "Онлайн", listOf(testMatch("l")))
        )

        assertEquals(listOf("Онлайн", "Предстоящие трансляции"), orderedVisibleSections(sections).map { it.title })
    }
}
