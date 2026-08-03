package com.svoysport.tv.ui.screens.home

import com.svoysport.tv.ui.components.nav.NavTab
import com.svoysport.tv.ui.components.nav.SidebarItem
import org.junit.Assert.assertEquals
import org.junit.Test

class HomeSidebarNavigationTest {
    @Test
    fun `selecting any sport opens the home tab`() {
        val method = runCatching {
            Class.forName("com.svoysport.tv.ui.screens.home.HomeScreenKt")
                .getDeclaredMethod("tabForSidebarSelection", SidebarItem::class.java)
                .apply { isAccessible = true }
        }.getOrNull()
        val sports = listOf(
            SidebarItem.FOOTBALL,
            SidebarItem.HOCKEY,
            SidebarItem.HANDBALL,
            SidebarItem.BASKETBALL,
            SidebarItem.VOLLEYBALL,
            SidebarItem.OTHER
        )

        sports.forEach { sport ->
            assertEquals("$sport must leave schedule", NavTab.HOME, method?.invoke(null, sport))
        }
    }
}
