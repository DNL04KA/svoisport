package com.svoysport.tv.ui.screens.player

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PlayerErrorPresentationTest {
    @Test
    fun `regional restriction uses dedicated copy and hides retry`() {
        val presentation = playerErrorPresentation("Доступ ограничен в вашем регионе")

        assertEquals("Трансляция недоступна в вашем регионе", presentation.title)
        assertEquals(
            "Некоторые трансляции ограничены правами показа в разных странах.",
            presentation.description
        )
        assertFalse(presentation.showRetry)
    }

    @Test
    fun `ordinary playback error remains retryable`() {
        assertTrue(playerErrorPresentation("Ошибка сети").showRetry)
    }

    @Test
    fun `geo restricted http responses use regional screen`() {
        assertFalse(playerErrorPresentation("HTTP 451").showRetry)
        assertFalse(playerErrorPresentation("HTTP 403 geo blocked").showRetry)
    }
}
