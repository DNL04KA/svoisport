package com.svoysport.tv.ui.screens.home

import com.svoysport.tv.ui.components.nav.SidebarItem
import org.junit.Assert.assertEquals
import org.junit.Test

class SidebarSelectionStateTest {
    @Test
    fun `search and favorites override previous sport selection`() {
        assertEquals(
            SidebarItem.SEARCH,
            visibleSidebarSelection(SidebarMode.SEARCH, SidebarItem.HOCKEY)
        )
        assertEquals(
            SidebarItem.BOOKMARKS,
            visibleSidebarSelection(SidebarMode.FAVORITES, SidebarItem.HOCKEY)
        )
        assertEquals(
            SidebarItem.HOCKEY,
            visibleSidebarSelection(SidebarMode.NONE, SidebarItem.HOCKEY)
        )
    }
}
