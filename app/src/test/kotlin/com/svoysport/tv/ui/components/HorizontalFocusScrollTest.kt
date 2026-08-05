package com.svoysport.tv.ui.components

import org.junit.Assert.assertEquals
import org.junit.Test

class HorizontalFocusScrollTest {
    @Test fun `visible card stays still and offscreen card scrolls into viewport`() {
        assertEquals(0f, horizontalFocusScrollDistance(100f, 230f, 800f))
        assertEquals(130f, horizontalFocusScrollDistance(700f, 230f, 800f))
        assertEquals(-40f, horizontalFocusScrollDistance(-40f, 230f, 800f))
    }
}
