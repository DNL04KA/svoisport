package com.svoysport.tv.ui.components.nav

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Test

class TopNavigationLayoutTest {
    @Test
    fun `top navigation does not move with sidebar`() {
        assertEquals(36f, topNavigationLeadingSpaceDp(sidebarExpanded = false))
        assertEquals(36f, topNavigationLeadingSpaceDp(sidebarExpanded = true))
    }

    @Test
    fun `selected top tab is gray`() {
        assertEquals(Color(0xFF414654), topTabContainerColor(selected = true, focused = false))
        assertEquals(Color(0xFF4556EB), topTabContainerColor(selected = true, focused = true))
    }
}
