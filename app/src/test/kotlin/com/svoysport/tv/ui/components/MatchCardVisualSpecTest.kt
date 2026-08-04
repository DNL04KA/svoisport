package com.svoysport.tv.ui.components

import org.junit.Assert.assertEquals
import org.junit.Test

class MatchCardVisualSpecTest {
    @Test
    fun `match card is one image with transparent description overlay`() {
        assertEquals(230f, MatchCardVisualSpec.widthDp)
        assertEquals(136f, MatchCardVisualSpec.heightDp)
        assertEquals(16f, MatchCardVisualSpec.titleSizeSp)
        assertEquals(20f, MatchCardVisualSpec.titleLineHeightSp)
        assertEquals(1f, contentCardScale(1100f))
        assertEquals(1f, contentCardScale(1760f))
        assertEquals(0.95f, MatchCardVisualSpec.bottomScrimAlpha)
        assertEquals("Футбол", cardSportLabel("Футбол. Чемпионат Колумбии"))
        assertEquals("Хоккей", cardSportLabel("Хоккей, Кубок Салея"))
    }
}
