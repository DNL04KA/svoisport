package com.svoysport.tv.ui.screens.home

import com.svoysport.tv.ui.components.nav.SidebarItem
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SportFilterTest {
    @Test
    fun `other sport excludes every named sidebar sport`() {
        assertFalse(sportMatchesSelection("Футбол. Лига чемпионов", "Матч", SidebarItem.OTHER))
        assertFalse(sportMatchesSelection("Кубок", "Гандбол Беларусь", SidebarItem.OTHER))
        assertTrue(sportMatchesSelection("Бильярд", "Florida Open Pool", SidebarItem.OTHER))
    }

    @Test
    fun `sport category heading follows selected sidebar sport`() {
        assertTrue(sectionTitleForSport("Футбол", SidebarItem.OTHER) == "Другой спорт")
        assertTrue(sectionTitleForSport("Футбол", SidebarItem.HANDBALL) == "Гандбол")
        assertTrue(sectionTitleForSport("Предстоящие трансляции", SidebarItem.OTHER) == "Предстоящие трансляции")
    }
}
