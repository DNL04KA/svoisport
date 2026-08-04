package com.svoysport.tv.ui.screens.home

import org.junit.Assert.assertEquals
import org.junit.Test

class HomeFocusScrollSpecTest {
    @Test
    fun `focused row settles near upper fifth of viewport`() {
        assertEquals(540f, homeFocusScrollDistance(offset = 700f, itemSize = 240f, viewportSize = 800f))
        assertEquals(0f, homeFocusScrollDistance(offset = 160f, itemSize = 240f, viewportSize = 800f))
    }
}
