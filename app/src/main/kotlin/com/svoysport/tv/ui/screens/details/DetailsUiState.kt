package com.svoysport.tv.ui.screens.details

import com.svoysport.tv.domain.model.MatchItem

sealed class DetailsUiState {
    object Loading : DetailsUiState()
    data class Success(
        val match: MatchItem,
        /** Похожие матчи той же лиги / спорта */
        val related: List<MatchItem> = emptyList()
    ) : DetailsUiState()
    data class Error(val message: String) : DetailsUiState()
}
