package com.svoysport.tv.ui.screens.home

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeTopBarScrollTest {
    @Test
    fun `top navigation stays through upcoming row and leaves for next section`() {
        assertFalse(shouldHideHomeTopBar(0, 0))
        assertFalse(shouldHideHomeTopBar(1, 200))
        assertTrue(shouldHideHomeTopBar(2, 0))
    }
}
