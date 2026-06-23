package com.svoysport.tv.ui.screens.home

import com.svoysport.tv.domain.model.HomeContent

sealed class HomeUiState {
    object Loading : HomeUiState()
    data class Success(val content: HomeContent) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}
