package com.svoysport.tv.ui.screens.favorites

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.tv.material3.*
import com.svoysport.tv.R
import com.svoysport.tv.domain.model.MatchItem
import com.svoysport.tv.domain.repository.MatchRepository
import com.svoysport.tv.session.FavoritesManager
import com.svoysport.tv.ui.components.MatchCard
import com.svoysport.tv.ui.theme.Gray3
import com.svoysport.tv.ui.theme.Gray4
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val repository: MatchRepository
) : ViewModel() {
    private val _all = MutableStateFlow<List<MatchItem>>(emptyList())
    val all: StateFlow<List<MatchItem>> = _all.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getAllMatches().onSuccess { _all.value = it }
        }
    }
}

@Composable
fun FavoritesContent(
    onMatchClick: (String) -> Unit,
    viewModel: FavoritesViewModel = hiltViewModel()
) {
    val all by viewModel.all.collectAsState()
    val favIds = FavoritesManager.favoriteIds.value
    val favorites = remember(all, favIds) { all.filter { it.id in favIds } }

    // Фокус сразу в контент — чтобы сайдбар-оверлей свернулся и избранное было видно
    val contentFr = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(120)
        runCatching { contentFr.requestFocus() }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val sw = maxWidth.value
        val sh = maxHeight.value
        val scale = minOf(sw / 1920f, sh / 1080f, 1f).coerceAtLeast(0.35f)
        val pad: Dp = (48f * scale).dp

        Column(modifier = Modifier.fillMaxSize().padding(pad)
            .focusRequester(contentFr).focusable()) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy((14f * scale).dp)) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_bookmark_active),
                    contentDescription = null, tint = Color.White,
                    modifier = Modifier.size((34f * scale).dp)
                )
                Text(
                    text  = "Избранное",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = (40f * scale).sp, fontWeight = FontWeight.SemiBold, color = Color.White
                    )
                )
            }
            Spacer(Modifier.height((28f * scale).dp))

            if (favorites.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.ic_bookmark),
                            contentDescription = null, tint = Gray4,
                            modifier = Modifier.size((48f * scale).dp)
                        )
                        Spacer(Modifier.height((16f * scale).dp))
                        Text(
                            text  = "В избранном пока пусто",
                            style = MaterialTheme.typography.labelMedium.copy(fontSize = (20f * scale).sp, color = Gray4)
                        )
                        Spacer(Modifier.height((6f * scale).dp))
                        Text(
                            text  = "Добавляйте трансляции кнопкой-закладкой на экране матча",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = (15f * scale).sp, color = Gray3)
                        )
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive((240f * scale).dp),
                    horizontalArrangement = Arrangement.spacedBy((16f * scale).dp),
                    verticalArrangement   = Arrangement.spacedBy((16f * scale).dp),
                    contentPadding = PaddingValues(bottom = (24f * scale).dp)
                ) {
                    items(favorites, key = { it.id }) { match ->
                        MatchCard(match = match, onClick = onMatchClick, scale = scale)
                    }
                }
            }
        }
    }
}
