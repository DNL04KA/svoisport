package com.svoysport.tv.ui.screens.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.svoysport.tv.domain.repository.MatchRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailsViewModel @Inject constructor(
    private val repository: MatchRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val matchId: String = checkNotNull(savedStateHandle["matchId"])

    private val _uiState = MutableStateFlow<DetailsUiState>(DetailsUiState.Loading)
    val uiState: StateFlow<DetailsUiState> = _uiState.asStateFlow()

    init { loadDetails() }

    fun loadDetails() {
        viewModelScope.launch {
            _uiState.value = DetailsUiState.Loading
            repository.getMatchDetails(matchId)
                .onSuccess { match ->
                    // Загружаем похожие матчи (вся лента → исключаем текущий)
                    val related = repository.getHomeContent()
                        .getOrNull()
                        ?.sections
                        ?.flatMap { it.matches }
                        ?.filter { it.id != matchId && it.competition.id == match.competition.id }
                        ?.distinctBy { it.id }
                        ?: emptyList()
                    _uiState.value = DetailsUiState.Success(match = match, related = related)
                }
                .onFailure { error ->
                    _uiState.value = DetailsUiState.Error(error.message ?: "Ошибка загрузки")
                }
        }
    }
}
