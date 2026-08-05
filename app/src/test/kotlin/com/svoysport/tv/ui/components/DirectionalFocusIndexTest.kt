package com.svoysport.tv.ui.components

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DirectionalFocusIndexTest {
    @Test
    fun `vertical navigation keeps the same column`() {
        assertEquals(3, directionalFocusIndex(currentIndex = 3, targetCount = 6))
    }

    @Test
    fun `vertical navigation clamps to the last item of a shorter row`() {
        assertEquals(2, directionalFocusIndex(currentIndex = 5, targetCount = 3))
    }

    @Test
    fun `vertical navigation has no target for an empty row`() {
        assertNull(directionalFocusIndex(currentIndex = 0, targetCount = 0))
    }
}
