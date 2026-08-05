package com.svoysport.tv.ui.screens.details

import org.junit.Assert.assertEquals
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
    }
}
