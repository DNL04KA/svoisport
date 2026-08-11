package com.svoysport.tv.data.mappers

import com.svoysport.tv.data.remote.sporttv.SportTvListingDto
import org.junit.Assert.assertEquals
import org.junit.Test

class SportTvMappersTest {

    @Test
    fun `scheduled listing does not become an archive recording from date end`() {
        val match = listing(duration = null).toMatchItem()

        assertEquals(0L, match.durationSec)
    }

    @Test
    fun `archive listing keeps explicit recording duration`() {
        val match = listing(duration = 5_400L).toMatchItem()

        assertEquals(5_400L, match.durationSec)
    }

    private fun listing(duration: Long?) = SportTvListingDto(
        date = "4102444800",
        date_end = "4102450200",
        title = "Футбол. Команда А - Команда Б",
        annotation = "Тест",
        sport_id = "1",
        source = "https://example.com/stream.m3u8",
        category_id = "1",
        duration = duration
    )
}
