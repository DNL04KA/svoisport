package com.svoysport.tv.ui.screens.details

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DetailsAccessStateTest {
    @Test fun `paid match asks anonymous user to sign in`() {
        assertEquals("Войти в аккаунт", detailsPrimaryActionLabel(true, false, false, true))
    }

    @Test fun `paid match asks signed in user without subscription to subscribe`() {
        assertEquals("Оформить подписку", detailsPrimaryActionLabel(true, true, false, true))
    }

    @Test fun `subscriber can watch and far future match cannot be opened`() {
        assertEquals("Смотреть", detailsPrimaryActionLabel(true, true, true, true))
        assertEquals("Скоро начнётся", detailsPrimaryActionLabel(false, true, false, false))
        assertEquals(
            "Трансляция завершена",
            detailsPrimaryActionLabel(false, true, false, false, isExpired = true)
        )
    }

    @Test fun `future match action shows live countdown`() {
        assertEquals("Начало через 00:25:48", formatDetailsCountdown(25 * 60_000L + 48_000L))
    }

    @Test fun `favorite action is wide enough for the full label and fits the row`() {
        assertEquals(420f, detailsFavoriteButtonWidthDp(), 0.01f)
        assertEquals(1_220f, detailsActionRowWidthDp(isUpcoming = true), 0.01f)
    }

    @Test fun `details hero follows the Figma lower third`() {
        assertEquals(568f, detailsHeroTopDp(), 0.01f)
        assertEquals(140f, detailsHeroStartDp(), 0.01f)
    }
}
