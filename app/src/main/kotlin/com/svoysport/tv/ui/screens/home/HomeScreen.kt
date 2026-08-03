package com.svoysport.tv.ui.screens.home

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.graphics.Brush
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import coil3.compose.AsyncImage
import com.svoysport.tv.domain.model.HomeSection
import com.svoysport.tv.domain.model.MatchItem
import com.svoysport.tv.session.SessionManager
import com.svoysport.tv.ui.components.ContentRow
import com.svoysport.tv.ui.components.TvScaffold
import com.svoysport.tv.ui.components.nav.NavTab
import com.svoysport.tv.ui.components.nav.SidebarItem
import com.svoysport.tv.ui.components.state.HomeErrorState
import com.svoysport.tv.ui.components.state.HomeLoadingState
import com.svoysport.tv.ui.screens.archive.ArchiveScreen
import com.svoysport.tv.ui.screens.favorites.FavoritesContent
import com.svoysport.tv.ui.screens.schedule.ScheduleScreen
import com.svoysport.tv.ui.screens.search.SearchContent
import com.svoysport.tv.ui.theme.Background
import com.svoysport.tv.ui.theme.Gray4

// Что показывать в области контента, помимо вкладок (Поиск/Избранное из сайдбара)
private enum class SidebarMode { NONE, SEARCH, FAVORITES }

private fun tabForSidebarSelection(item: SidebarItem): NavTab? = when (item) {
    SidebarItem.SEARCH, SidebarItem.BOOKMARKS -> null
    else -> NavTab.HOME
}

// ─── HomeScreen ──────────────────────────────────────────────────────────────

@Composable
fun HomeScreen(
    onMatchClick: (String) -> Unit,
    onAuthClick: () -> Unit = {},
    initialSidebarItem: SidebarItem? = null,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState         by viewModel.uiState.collectAsState()
    var selectedTab     by remember { mutableStateOf(NavTab.HOME) }
    var selectedSport   by remember { mutableStateOf<SidebarItem?>(null) }
    var sidebarMode     by remember { mutableStateOf(SidebarMode.NONE) }

    LaunchedEffect(initialSidebarItem) {
        when (initialSidebarItem) {
            SidebarItem.SEARCH -> {
                selectedSport = null
                sidebarMode = SidebarMode.SEARCH
            }
            SidebarItem.BOOKMARKS -> {
                selectedSport = null
                sidebarMode = SidebarMode.FAVORITES
            }
            null -> Unit
            else -> {
                selectedSport = initialSidebarItem
                selectedTab = NavTab.HOME
                sidebarMode = SidebarMode.NONE
            }
        }
    }

    TvScaffold(
        selectedTab         = selectedTab,
        onTabSelected       = { tab ->
            // Переключение вкладки сверху сбрасывает режим Поиск/Избранное
            selectedTab = tab
            sidebarMode = SidebarMode.NONE
        },
        isLoggedIn          = SessionManager.isLoggedIn.value ||
                              com.svoysport.tv.session.SubscriptionManager.isSubscribed.value,
        onAuthClick         = onAuthClick,
        selectedSidebarItem = selectedSport,
        onSidebarItemSelected = { item ->
            when (item) {
                SidebarItem.SEARCH    -> sidebarMode = SidebarMode.SEARCH
                SidebarItem.BOOKMARKS -> sidebarMode = SidebarMode.FAVORITES
                else                  -> {
                    selectedSport = item
                    selectedTab = tabForSidebarSelection(item) ?: selectedTab
                    sidebarMode = SidebarMode.NONE
                }
            }
        }
    ) {
        when (sidebarMode) {
            SidebarMode.SEARCH    -> SearchContent(onMatchClick = onMatchClick)
            SidebarMode.FAVORITES -> FavoritesContent(onMatchClick = onMatchClick)
            SidebarMode.NONE -> when (selectedTab) {
                NavTab.SCHEDULE -> ScheduleScreen(onMatchClick = onMatchClick)
                NavTab.ARCHIVE  -> ArchiveScreen(onMatchClick = onMatchClick)
                NavTab.HOME     -> HomeContent(
                    uiState      = uiState,
                    selectedSport = selectedSport,
                    onMatchClick = onMatchClick,
                    onWatchMore  = { sectionTitle ->
                        selectedTab = if (sectionTitle.contains("архив", ignoreCase = true)) NavTab.ARCHIVE else NavTab.SCHEDULE
                        sidebarMode = SidebarMode.NONE
                    },
                    onRetry      = { viewModel.loadHomeContent() }
                )
            }
        }
    }
}

// ─── HomeContent ─────────────────────────────────────────────────────────────

@Composable
private fun HomeContent(
    uiState: HomeUiState,
    selectedSport: SidebarItem?,
    onMatchClick: (String) -> Unit,
    onWatchMore: (String) -> Unit,
    onRetry: () -> Unit
) {
    when (uiState) {
        is HomeUiState.Loading -> HomeLoadingState()

        is HomeUiState.Error   -> HomeErrorState(
            message = uiState.message,
            onRetry = onRetry
        )

        is HomeUiState.Success -> {
            val sections = filterBySport(uiState.content.sections, selectedSport)

            if (sections.isEmpty()) {
                EmptyState()
            } else {
                // Figma BG: обложка главной/выбранной трансляции на заднем фоне —
                // 70% прозрачности, blur всего фрейма, градиентное затемнение сверху
                var focusedMatch by remember { mutableStateOf<MatchItem?>(null) }
                val bgMatch = focusedMatch ?: uiState.content.featuredMatch

                Box(modifier = Modifier.fillMaxSize().background(Background)) {
                    Crossfade(
                        targetState   = bgMatch.backgroundUrl ?: bgMatch.thumbnailUrl,
                        animationSpec = tween(400),
                        label         = "homeBg"
                    ) { url ->
                        AsyncImage(
                            model              = url,
                            contentDescription = null,
                            modifier           = Modifier.fillMaxSize().alpha(0.30f).blur(60.dp),
                            contentScale       = ContentScale.Crop
                        )
                    }

                    // Градиентный скрим: тёплое свечение остаётся только сверху,
                    // нижняя часть затемняется к фону #0F0F10 (как в макете)
                    Box(
                        modifier = Modifier.fillMaxSize().background(
                            Brush.verticalGradient(
                                0.00f to Background.copy(alpha = 0.10f),
                                0.45f to Background.copy(alpha = 0.65f),
                                0.80f to Background,
                                1.00f to Background
                            )
                        )
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        // HeroBanner — featured матч вверху
                        HeroBanner(
                            match          = uiState.content.featuredMatch,
                            onWatchClick   = onMatchClick,
                            onMatchFocused = { focusedMatch = it }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Горизонтальные секции: Онлайн, Предстоящие, Архив...
                        sections.forEach { section ->
                            ContentRow(
                                title          = section.title,
                                matches        = section.matches,
                                onMatchClick   = onMatchClick,
                                onMatchFocused = { focusedMatch = it },
                                onWatchMore    = { onWatchMore(section.title) },
                                modifier       = Modifier.padding(bottom = 28.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }
    }
}

// ─── EmptyState ──────────────────────────────────────────────────────────────

@Composable
private fun EmptyState() {
    Box(
        modifier         = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text  = "Трансляций по выбранному виду спорта нет",
            style = MaterialTheme.typography.labelMedium.copy(
                fontSize = 18.sp,
                color    = Gray4
            )
        )
    }
}

// ─── Helpers ─────────────────────────────────────────────────────────────────

private val SPORT_KEYWORDS = mapOf(
    SidebarItem.FOOTBALL   to "Футбол",
    SidebarItem.HOCKEY     to "Хоккей",
    SidebarItem.BASKETBALL to "Баскетбол",
    SidebarItem.VOLLEYBALL to "Волейбол",
    SidebarItem.HANDBALL   to "Гандбол"
)

private fun filterBySport(
    sections: List<HomeSection>,
    sport: SidebarItem?
): List<HomeSection> {
    val keyword = SPORT_KEYWORDS[sport] ?: return sections
    return sections
        .map { section ->
            section.copy(
                matches = section.matches.filter { match ->
                    match.competition.name.contains(keyword, ignoreCase = true) ||
                    match.title.contains(keyword, ignoreCase = true)
                }
            )
        }
        .filter { it.matches.isNotEmpty() }
}
