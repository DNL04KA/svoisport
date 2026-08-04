package com.svoysport.tv.navigation

import com.svoysport.tv.ui.components.nav.SidebarItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class HomeSidebarRouteTest {
    @Test
    fun `home route preserves profile sidebar selection`() {
        assertEquals("home?sidebar=FOOTBALL", Screen.Home.createRoute(SidebarItem.FOOTBALL))
        assertEquals(SidebarItem.FOOTBALL, sidebarItemFromRoute("FOOTBALL"))
        assertEquals(SidebarItem.BOOKMARKS, sidebarItemFromRoute("BOOKMARKS"))
    }

    @Test
    fun `unknown home sidebar route is ignored safely`() {
        assertNull(sidebarItemFromRoute(null))
        assertNull(sidebarItemFromRoute("UNKNOWN"))
    }
}
