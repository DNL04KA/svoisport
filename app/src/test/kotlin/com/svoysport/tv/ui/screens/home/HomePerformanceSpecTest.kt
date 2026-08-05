package com.svoysport.tv.ui.screens.home

import org.junit.Assert.assertTrue
import org.junit.Test

class HomePerformanceSpecTest {
    @Test
    fun `background effects stay within TV friendly budget`() {
        assertTrue(HOME_BACKGROUND_BLUR_DP <= 48f)
        assertTrue(HOME_BACKGROUND_CROSSFADE_MS <= 200)
        assertTrue(HOME_BACKGROUND_FOCUS_DEBOUNCE_MS >= 180L)
    }

    @Test
    fun `focus cache retains adjacent row without retaining several screens`() {
        assertTrue(HOME_FOCUS_CACHE_VIEWPORTS >= 1f)
        assertTrue(HOME_FOCUS_CACHE_VIEWPORTS <= 1.25f)
    }
}
