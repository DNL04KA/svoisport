package com.svoysport.tv.ui.screens.favorites

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FavoritesFocusTest {
    @Test
    fun `first favorite receives initial focus`() {
        assertNull(initialFavoriteFocusIndex(0))
        assertEquals(0, initialFavoriteFocusIndex(1))
        assertEquals(0, initialFavoriteFocusIndex(8))
    }
}
