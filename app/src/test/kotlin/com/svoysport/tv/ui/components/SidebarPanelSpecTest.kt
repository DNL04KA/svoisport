package com.svoysport.tv.ui.components.nav

import org.junit.Assert.assertEquals
import org.junit.Test
import androidx.compose.ui.graphics.Color

class SidebarPanelSpecTest {
    @Test
    fun `sidebar uses one continuous translucent panel`() {
        assertEquals(0.92f, sidebarPanelStartAlpha)
        assertEquals(0.12f, sidebarPanelEndAlpha)
    }

    @Test
    fun `selected item is gray and focused item is blue`() {
        assertEquals(Color(0xFF343B4B), sidebarItemContainerColor(isSelected = true, isFocused = false))
        assertEquals(Color(0xFF4556EB), sidebarItemContainerColor(isSelected = true, isFocused = true))
        assertEquals(Color(0xFF4556EB), sidebarItemContainerColor(isSelected = false, isFocused = true))
    }
}
