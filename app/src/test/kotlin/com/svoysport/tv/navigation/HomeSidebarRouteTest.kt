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

    @Test
    fun `all content routes keep their target identifiers`() {
        assertEquals("details/match-42", Screen.Details.createRoute("match-42"))
        assertEquals("player/match-42", Screen.Player.createRoute("match-42"))
        assertEquals("activation?planId=36735", Screen.Activation.createRoute("36735"))
    }

    @Test
    fun `related match opens details before player`() {
        assertEquals("details/related-7", relatedMatchRoute("related-7"))
    }
}
