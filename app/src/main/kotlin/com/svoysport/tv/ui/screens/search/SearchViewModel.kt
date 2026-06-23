package com.svoysport.tv.ui.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.svoysport.tv.domain.model.MatchItem
import com.svoysport.tv.domain.repository.MatchRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchUiState(
    val query:    String         = "",
    val results:  List<MatchItem> = emptyList(),
    val allLoaded: Boolean        = false,
    val error:    String?        = null
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: MatchRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SearchUiState())
    val state: StateFlow<SearchUiState> = _state.asStateFlow()

    private var all: List<MatchItem> = emptyList()

    init { load() }

    private fun load() {
        viewModelScope.launch {
            repository.getAllMatches()
                .onSuccess { items ->
                    all = items
                    _state.value = _state.value.copy(allLoaded = true, error = null, results = filter(_state.value.query))
                }
                .onFailure { e ->
                    _state.value = _state.value.copy(error = e.message ?: "Не удалось загрузить трансляции")
                }
        }
    }

    fun onQueryChange(query: String) {
        _state.value = _state.value.copy(query = query, results = filter(query))
    }

    private fun filter(query: String): List<MatchItem> {
        val q = query.trim()
        if (q.isEmpty()) return emptyList()
        return all.filter {
            it.title.contains(q, ignoreCase = true) ||
            it.description.contains(q, ignoreCase = true) ||
            it.competition.name.contains(q, ignoreCase = true)
        }
    }
}
