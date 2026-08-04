package com.svoysport.tv.ui.screens.home

import org.junit.Assert.assertEquals
import org.junit.Test

class HomePrimaryFocusTest {
    @Test
    fun `down from hero targets first card in first visible section`() {
        assertEquals(0, firstHomeContentCardIndex())
    }
}
