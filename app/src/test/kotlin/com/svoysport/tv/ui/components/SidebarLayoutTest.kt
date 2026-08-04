package com.svoysport.tv.ui.components

import org.junit.Assert.assertEquals
import org.junit.Test

class SidebarLayoutTest {
    @Test
    fun `expanded sidebar moves content by its full width`() {
        assertEquals(60, sidebarContentStartDp(expanded = false))
        assertEquals(220, sidebarContentStartDp(expanded = true))
        assertEquals(160, sidebarExpansionDeltaDp)
    }
}
