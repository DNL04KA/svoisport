package com.svoysport.tv.ui.screens.home

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeTopBarScrollTest {
    @Test
    fun `top navigation leaves screen after home starts scrolling`() {
        assertFalse(shouldHideHomeTopBar(0, 0))
        assertTrue(shouldHideHomeTopBar(0, 1))
        assertTrue(shouldHideHomeTopBar(1, 0))
    }
}
