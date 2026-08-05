package com.svoysport.tv.ui.screens.home

import com.svoysport.tv.domain.model.Competition
import com.svoysport.tv.domain.model.HomeSection
import com.svoysport.tv.domain.model.MatchItem
import com.svoysport.tv.domain.model.Team
import com.svoysport.tv.ui.components.nav.SidebarItem
import org.junit.Assert.assertEquals
import org.junit.Test

class HomeFilteredHeroTest {
    private fun match(id: String, sport: String) = MatchItem(
        id = id,
        title = "$sport. Матч $id",
        description = "",
        homeTeam = Team("h", "Home"),
        awayTeam = Team("a", "Away"),
        competition = Competition("c", sport),
        isLive = false,
        startTimeMs = 0L,
        thumbnailUrl = ""
    )

    @Test
    fun `football filter replaces unrelated featured match`() {
        val billiards = match("billiards", "Бильярд")
        val football = match("football", "Футбол")
        val sections = listOf(HomeSection("upcoming", "Предстоящие трансляции", listOf(football)))

        assertEquals(
            football,
            featuredMatchForSport(billiards, sections, SidebarItem.FOOTBALL)
        )
    }

    @Test
    fun `unfiltered home keeps backend featured match`() {
        val billiards = match("billiards", "Бильярд")

        assertEquals(billiards, featuredMatchForSport(billiards, emptyList(), null))
    }

    @Test
    fun `section zero is lazy item two after hero and spacer`() {
        assertEquals(2, homeLazyListIndexForSection(0))
        assertEquals(4, homeLazyListIndexForSection(2))
    }
}
