package com.svoysport.tv.ui.components.nav

import org.junit.Assert.assertEquals
import org.junit.Test

class SidebarPanelSpecTest {
    @Test
    fun `sidebar uses one continuous translucent panel`() {
        assertEquals(0.78f, sidebarPanelAlpha)
    }
}
