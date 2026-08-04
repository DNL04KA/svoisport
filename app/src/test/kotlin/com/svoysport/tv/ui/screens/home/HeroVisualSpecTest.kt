package com.svoysport.tv.ui.screens.home

import org.junit.Assert.assertEquals
import org.junit.Test

class HeroVisualSpecTest {
    @Test
    fun `hero remains visually larger than content cards`() {
        assertEquals(340f, HeroVisualSpec.heightDp)
        assertEquals(36f, HeroVisualSpec.titleSizeSp)
        assertEquals(44f, HeroVisualSpec.titleLineHeightSp)
        assertEquals(40f, HeroVisualSpec.bookmarkSizeDp)
        assertEquals(0.65f, heroVisualScale(700f))
    }
}
