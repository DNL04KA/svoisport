package com.svoysport.tv.data.remote.sporttv

/**
 * Data Transfer Object for external schedule from sport-tv.by
 * Link: https://sport-tv.by/list.php
 */
data class SportTvListingDto(
    val date: String,
    val date_end: String,
    val title: String,
    val annotation: String,
    val sport_id: String,
    val source: String,
    val category_id: String
)
