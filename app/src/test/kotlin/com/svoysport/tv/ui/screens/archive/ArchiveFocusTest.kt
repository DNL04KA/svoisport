package com.svoysport.tv.ui.screens.archive

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ArchiveFocusTest {

    @Test
    fun `first archive card receives focus when archive becomes visible`() {
        assertNull(initialArchiveFocusPosition(emptyList()))
        assertEquals(0 to 0, initialArchiveFocusPosition(listOf(2, 3)))
        assertEquals(1 to 0, initialArchiveFocusPosition(listOf(0, 1)))
    }

    @Test
    fun `archive title typography scales with its card`() {
        assertEquals(26f, archiveTitleSizeSp(237f), 0.01f)
        assertEquals(32f, archiveTitleLineHeightSp(237f), 0.01f)
        assertEquals(13f, archiveTitleSizeSp(118.5f), 0.01f)
        assertEquals(16f, archiveTitleLineHeightSp(118.5f), 0.01f)
    }
}
