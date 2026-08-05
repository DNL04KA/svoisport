package com.svoysport.tv.ui.screens.home

import org.junit.Assert.assertEquals
import org.junit.Test

class HomeBackgroundGeometryTest {
    @Test
    fun `home background spans the complete scaffold`() {
        val screenClass = Class.forName("com.svoysport.tv.ui.screens.home.HomeScreenKt")

        assertEquals(1920f, homeBackgroundWidth(1920f))
        assertEquals(1080f, homeBackgroundHeight(1080f))
        assertEquals(80f, screenClass.privateFloat("HOME_BACKGROUND_BLUR_DP"))
        assertEquals(0.30f, screenClass.privateFloat("HOME_BACKGROUND_IMAGE_ALPHA"))
        assertEquals(0.30f, screenClass.privateFloat("HOME_BACKGROUND_GRADIENT_ALPHA"))
    }

    private fun Class<*>.privateFloat(name: String): Float =
        getDeclaredField(name).apply { isAccessible = true }.getFloat(null)
}
