package com.svoysport.tv.ui.screens.home

import org.junit.Assert.assertEquals
import org.junit.Test

class HomeFocusScrollSpecTest {
    @Test
    fun `hero to first row stays still but returning from lower row scrolls up`() {
        assertEquals(false, shouldScrollForHomeFocusTransition(previousSection = -1, nextSection = 0))
        assertEquals(true, shouldScrollForHomeFocusTransition(previousSection = 0, nextSection = 1))
        assertEquals(true, shouldScrollForHomeFocusTransition(previousSection = 1, nextSection = 0))
    }

    @Test
    fun `first content row does not move the home screen`() {
        assertEquals(0f, firstHomeRowScrollDistance())
    }

    @Test
    fun `focused row settles near upper fifth of viewport`() {
        assertEquals(540f, homeFocusScrollDistance(offset = 700f, itemSize = 240f, viewportSize = 800f))
        assertEquals(0f, homeFocusScrollDistance(offset = 160f, itemSize = 240f, viewportSize = 800f))
    }
}
