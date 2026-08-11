package com.svoysport.tv.ui.screens.player

import org.junit.Assert.assertEquals
import org.junit.Test

class PlayerOverlayLayoutTest {
    @Test fun `timeline uses the compact Figma time label`() {
        assertEquals("1:45:51 / 2:45:51", formatPlayerTimeline(6_351_000L, 9_951_000L))
    }

    @Test fun `player chrome scales from the 1920 design`() {
        assertEquals(88f, playerBackButtonSizeDp(1f), 0.01f)
        assertEquals(44f, playerBackButtonSizeDp(0.5f), 0.01f)
        assertEquals(144f, playerPrimaryControlWidthDp(1f), 0.01f)
        assertEquals(72f, playerPrimaryControlWidthDp(0.5f), 0.01f)
    }
}
