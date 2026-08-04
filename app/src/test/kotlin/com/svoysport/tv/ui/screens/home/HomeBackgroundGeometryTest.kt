package com.svoysport.tv.ui.screens.home

import org.junit.Assert.assertEquals
import org.junit.Test

class HomeBackgroundGeometryTest {
    @Test
    fun `home background spans the complete scaffold`() {
        val screenClass = Class.forName("com.svoysport.tv.ui.screens.home.HomeScreenKt")

        assertEquals(1880f, homeBackgroundWidth(1760f))
        assertEquals(1072f, homeBackgroundHeight(1016f))
        assertEquals(60f, screenClass.privateFloat("SCAFFOLD_RAIL_WIDTH_DP"))
        assertEquals(56f, screenClass.privateFloat("SCAFFOLD_TOP_BAR_HEIGHT_DP"))
        assertEquals(210f, screenClass.privateFloat("HOME_BACKGROUND_BLUR_DP"))
        assertEquals(0.30f, screenClass.privateFloat("HOME_BACKGROUND_IMAGE_ALPHA"))
        assertEquals(0.30f, screenClass.privateFloat("HOME_BACKGROUND_GRADIENT_ALPHA"))
    }

    private fun Class<*>.privateFloat(name: String): Float =
        getDeclaredField(name).apply { isAccessible = true }.getFloat(null)
}
