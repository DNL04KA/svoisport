package com.svoysport.tv.ui.components

import org.junit.Assert.assertEquals
import org.junit.Test

class MatchCardVisualSpecTest {
    @Test
    fun `match card is one image with transparent description overlay`() {
        assertEquals(193f, MatchCardVisualSpec.heightDp)
        assertEquals(0.95f, MatchCardVisualSpec.bottomScrimAlpha)
        assertEquals("Футбол", cardSportLabel("Футбол. Чемпионат Колумбии"))
        assertEquals("Хоккей", cardSportLabel("Хоккей, Кубок Салея"))
    }
}
