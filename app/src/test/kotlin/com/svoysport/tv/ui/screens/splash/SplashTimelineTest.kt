package com.svoysport.tv.ui.screens.splash

import org.junit.Assert.assertEquals
import org.junit.Test

class SplashTimelineTest {
    @Test
    fun `figma timeline keeps every required phase`() {
        assertEquals(600L, SplashTimeline.initialFrameMs)
        assertEquals(1_000, SplashTimeline.backgroundRevealMs)
        assertEquals(200L, SplashTimeline.logoFrameMs)
        assertEquals(1_000, SplashTimeline.wordmarkRevealMs)
        assertEquals(500, SplashTimeline.loaderRevealMs)
        assertEquals(3_300L, SplashTimeline.minimumDurationMs)
    }

    @Test
    fun `figma geometry maps 1920px design to tv density`() {
        assertEquals(100, SplashGeometry.logoSizeDp)
        assertEquals(40, SplashGeometry.loaderSizeDp)
        assertEquals(-15, SplashGeometry.groupOffsetXDp)
        assertEquals(35, SplashGeometry.groupOffsetYDp)
    }
}
