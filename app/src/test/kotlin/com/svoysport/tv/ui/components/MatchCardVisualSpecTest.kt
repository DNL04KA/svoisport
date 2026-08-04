package com.svoysport.tv.ui.components

import org.junit.Assert.assertEquals
import org.junit.Test

class MatchCardVisualSpecTest {
    @Test
    fun `match card is one image with transparent description overlay`() {
        assertEquals(400f, MatchCardVisualSpec.widthDp)
        assertEquals(237f, MatchCardVisualSpec.heightDp)
        assertEquals(26f, MatchCardVisualSpec.titleSizeSp)
        assertEquals(32f, MatchCardVisualSpec.titleLineHeightSp)
        assertEquals(1f, contentCardScale(1100f))
        assertEquals(1f, contentCardScale(1760f))
        assertEquals(0.95f, MatchCardVisualSpec.bottomScrimAlpha)
        assertEquals("Футбол", cardSportLabel("Футбол. Чемпионат Колумбии"))
        assertEquals("Хоккей", cardSportLabel("Хоккей, Кубок Салея"))
    }
}
