package com.svoysport.tv.ui.screens.home

import org.junit.Assert.assertEquals
import org.junit.Test

class HomeBackgroundGeometryTest {
    @Test
    fun `home background spans scaffold width and matches figma height`() {
        val screenClass = Class.forName("com.svoysport.tv.ui.screens.home.HomeScreenKt")

        assertEquals(476f, screenClass.privateFloat("HOME_BACKGROUND_HEIGHT_DP"))
        assertEquals(60f, screenClass.privateFloat("SCAFFOLD_RAIL_WIDTH_DP"))
        assertEquals(64f, screenClass.privateFloat("SCAFFOLD_TOP_BAR_HEIGHT_DP"))
        assertEquals(105f, screenClass.privateFloat("HOME_BACKGROUND_BLUR_DP"))
    }

    private fun Class<*>.privateFloat(name: String): Float =
        getDeclaredField(name).apply { isAccessible = true }.getFloat(null)
}
