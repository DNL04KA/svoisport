package com.svoysport.tv.ui.screens.home

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.BringIntoViewSpec
import androidx.compose.foundation.gestures.LocalBringIntoViewSpec
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import coil3.compose.AsyncImage
import com.svoysport.tv.domain.model.HomeSection
import com.svoysport.tv.domain.model.MatchItem
import com.svoysport.tv.session.SessionManager
import com.svoysport.tv.ui.components.ContentRow
import com.svoysport.tv.ui.components.AppBackground
import com.svoysport.tv.ui.components.TvScaffold
import com.svoysport.tv.ui.components.nav.NavTab
import com.svoysport.tv.ui.components.nav.SidebarItem
import com.svoysport.tv.ui.components.state.HomeErrorState
import com.svoysport.tv.ui.components.state.HomeLoadingState
import com.svoysport.tv.ui.screens.archive.ArchiveScreen
import com.svoysport.tv.ui.screens.favorites.FavoritesContent
import com.svoysport.tv.ui.screens.schedule.ScheduleScreen
import com.svoysport.tv.ui.screens.search.SearchContent
import com.svoysport.tv.ui.theme.Gray4
import kotlinx.coroutines.flow.distinctUntilChanged

// Что показывать в области контента, помимо вкладок (Поиск/Избранное из сайдбара)
internal enum class SidebarMode { NONE, SEARCH, FAVORITES }

internal fun visibleSidebarSelection(
    mode: SidebarMode,
    selectedSport: SidebarItem?
): SidebarItem? = when (mode) {
    SidebarMode.SEARCH -> SidebarItem.SEARCH
    SidebarMode.FAVORITES -> SidebarItem.BOOKMARKS
    SidebarMode.NONE -> selectedSport
}

private const val HOME_BACKGROUND_BLUR_DP = 80f
private const val HOME_ROW_ITEM_LIMIT = 10
private const val HOME_BACKGROUND_IMAGE_ALPHA = 0.30f
private const val HOME_BACKGROUND_GRADIENT_ALPHA = 0.30f
internal fun homeBackgroundWidth(screenWidthDp: Float): Float = screenWidthDp

internal fun homeBackgroundHeight(screenHeightDp: Float): Float = screenHeightDp

internal fun firstHomeContentCardIndex(): Int = 0

internal fun homeRowMatches(matches: List<MatchItem>): List<MatchItem> =
    matches.take(HOME_ROW_ITEM_LIMIT)

private fun tabForSidebarSelection(item: SidebarItem): NavTab? = when (item) {
    SidebarItem.SEARCH, SidebarItem.BOOKMARKS -> null
    else -> NavTab.HOME
}

internal fun shouldHideHomeTopBar(firstVisibleItem: Int, scrollOffset: Int): Boolean =
    firstVisibleItem >= 2

internal fun homeFocusScrollDistance(offset: Float, itemSize: Float, viewportSize: Float): Float {
    val targetOffset = viewportSize * 0.20f
    return if (kotlin.math.abs(offset - targetOffset) < 1f) 0f else offset - targetOffset
}

internal fun firstHomeRowScrollDistance(): Float = 0f

internal fun shouldScrollForHomeFocusTransition(previousSection: Int, nextSection: Int): Boolean =
    nextSection > 0 ||
        (nextSection == 0 && previousSection > 0) ||
        (nextSection == -1 && previousSection >= 0)

private class HomeBringIntoViewSpec(
    private val shouldScroll: () -> Boolean
) : BringIntoViewSpec {
    override fun calculateScrollDistance(offset: Float, size: Float, containerSize: Float): Float =
        if (shouldScroll()) homeFocusScrollDistance(offset, size, containerSize)
        else firstHomeRowScrollDistance()
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
    var topBarHidden    by remember { mutableStateOf(false) }
    var expandedSection by remember { mutableStateOf<String?>(null) }
    var focusedHomeMatch by remember { mutableStateOf<MatchItem?>(null) }
    var pendingHomeMatch by remember { mutableStateOf<MatchItem?>(null) }

    LaunchedEffect(pendingHomeMatch) {
        val pending = pendingHomeMatch ?: return@LaunchedEffect
        kotlinx.coroutines.delay(140)
        focusedHomeMatch = pending
    }

    BackHandler(enabled = expandedSection != null) { expandedSection = null }

    LaunchedEffect(selectedTab, sidebarMode) {
        if (selectedTab != NavTab.HOME || sidebarMode != SidebarMode.NONE) {
            topBarHidden = false
        }
    }

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
            expandedSection = null
            focusedHomeMatch = null
            pendingHomeMatch = null
        },
        isLoggedIn          = SessionManager.isLoggedIn.value ||
                              com.svoysport.tv.session.SubscriptionManager.isSubscribed.value,
        onAuthClick         = onAuthClick,
        topBarHidden        = topBarHidden,
        selectedSidebarItem = visibleSidebarSelection(sidebarMode, selectedSport),
        background = {
            AppBackground()
            val featuredMatch = (uiState as? HomeUiState.Success)?.content?.featuredMatch
            val homeMatch = focusedHomeMatch ?: featuredMatch
            if (
                homeMatch != null && selectedTab == NavTab.HOME &&
                sidebarMode == SidebarMode.NONE && expandedSection == null
            ) {
                HomeBackground(match = homeMatch)
            }
        },
        onSidebarItemSelected = { item ->
            expandedSection = null
            focusedHomeMatch = null
            pendingHomeMatch = null
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
            SidebarMode.NONE -> if (expandedSection != null && uiState is HomeUiState.Success) {
                val section = (uiState as HomeUiState.Success).content.sections
                    .firstOrNull { it.title == expandedSection }
                AllSectionContent(
                    title = expandedSection.orEmpty(),
                    matches = section?.matches.orEmpty(),
                    onMatchClick = onMatchClick,
                    onBack = { expandedSection = null }
                )
            } else when (selectedTab) {
                NavTab.SCHEDULE -> ScheduleScreen(onMatchClick = onMatchClick)
                NavTab.ARCHIVE  -> ArchiveScreen(onMatchClick = onMatchClick)
                NavTab.HOME     -> HomeContent(
                    uiState      = uiState,
                    selectedSport = selectedSport,
                    onMatchClick = onMatchClick,
                    onWatchMore  = { sectionTitle ->
                        expandedSection = sectionTitle
                        focusedHomeMatch = null
                        pendingHomeMatch = null
                    },
                    onBackgroundMatchFocused = { pendingHomeMatch = it },
                    onRetry      = { viewModel.loadHomeContent() },
                    onTopBarHiddenChange = { topBarHidden = it }
                )
            }
        }
    }
}

// ─── HomeContent ─────────────────────────────────────────────────────────────

@Composable
@OptIn(ExperimentalFoundationApi::class)
private fun HomeContent(
    uiState: HomeUiState,
    selectedSport: SidebarItem?,
    onMatchClick: (String) -> Unit,
    onWatchMore: (String) -> Unit,
    onBackgroundMatchFocused: (MatchItem?) -> Unit,
    onRetry: () -> Unit,
    onTopBarHiddenChange: (Boolean) -> Unit
) {
    when (uiState) {
        is HomeUiState.Loading -> HomeLoadingState()

        is HomeUiState.Error   -> HomeErrorState(
            message = uiState.message,
            onRetry = onRetry
        )

        is HomeUiState.Success -> {
            val sections = orderedVisibleSections(filterBySport(uiState.content.sections, selectedSport))

            if (sections.isEmpty()) {
                LaunchedEffect(Unit) { onTopBarHiddenChange(false) }
                EmptyState()
            } else {
                val scrollState = rememberLazyListState()
                val sectionFocusRequesters = remember(sections.map { it.id }) {
                    List(sections.size) { FocusRequester() }
                }
                val firstContentCardFocusRequester = sectionFocusRequesters.first()
                var focusedSectionIndex by remember { mutableIntStateOf(-1) }
                var shouldScrollFocusedSection by remember { mutableStateOf(false) }
                val bringIntoViewSpec = remember {
                    HomeBringIntoViewSpec { shouldScrollFocusedSection }
                }

                LaunchedEffect(selectedSport) {
                    scrollState.scrollToItem(0)
                    focusedSectionIndex = -1
                    shouldScrollFocusedSection = false
                    onBackgroundMatchFocused(uiState.content.featuredMatch)
                    onTopBarHiddenChange(false)
                }

                LaunchedEffect(scrollState) {
                    snapshotFlow {
                        shouldHideHomeTopBar(
                            scrollState.firstVisibleItemIndex,
                            scrollState.firstVisibleItemScrollOffset
                        )
                    }
                        .distinctUntilChanged()
                        .collect(onTopBarHiddenChange)
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    CompositionLocalProvider(LocalBringIntoViewSpec provides bringIntoViewSpec) {
                        LazyColumn(
                            state = scrollState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 32.dp)
                        ) {
                            item(key = "hero") {
                                HeroBanner(
                                    match          = uiState.content.featuredMatch,
                                    onWatchClick   = onMatchClick,
                                    onMatchFocused = {
                                        shouldScrollFocusedSection = shouldScrollForHomeFocusTransition(
                                            previousSection = focusedSectionIndex,
                                            nextSection = -1
                                        )
                                        focusedSectionIndex = -1
                                        onBackgroundMatchFocused(uiState.content.featuredMatch)
                                    },
                                    downFocusRequester = firstContentCardFocusRequester
                                )
                            }
                            item(key = "hero-gap") { Spacer(modifier = Modifier.height(12.dp)) }
                            itemsIndexed(sections, key = { _, section -> section.id }) { sectionIndex, section ->
                                ContentRow(
                                    title          = section.title,
                                    matches        = homeRowMatches(section.matches),
                                    onMatchClick   = onMatchClick,
                                    onMatchFocused = { onBackgroundMatchFocused(it) },
                                    onRowFocused   = {
                                        shouldScrollFocusedSection = shouldScrollForHomeFocusTransition(
                                            previousSection = focusedSectionIndex,
                                            nextSection = sectionIndex
                                        )
                                        focusedSectionIndex = sectionIndex
                                    },
                                    firstCardFocusRequester = if (sectionIndex == 0) firstContentCardFocusRequester else null,
                                    upFocusRequester = sectionFocusRequesters.getOrNull(sectionIndex - 1),
                                    onWatchMore    = if (section.matches.size > HOME_ROW_ITEM_LIMIT) {
                                        { onWatchMore(section.title) }
                                    } else null,
                                    modifier       = Modifier.padding(bottom = 28.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeBackground(match: MatchItem) {
    Box(Modifier.fillMaxSize().clipToBounds()) {
        Crossfade(
            targetState = match.backgroundUrl ?: match.thumbnailUrl,
            animationSpec = tween(durationMillis = 250),
            label = "focusedMatchBackground"
        ) { imageUrl ->
            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize()
                    .alpha(HOME_BACKGROUND_IMAGE_ALPHA)
                    .blur(
                        radius = HOME_BACKGROUND_BLUR_DP.dp,
                        edgeTreatment = BlurredEdgeTreatment.Unbounded
                    ),
                contentScale = ContentScale.Crop
            )
        }
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(
                    0.00f to Color.Transparent,
                    0.20f to Color.Transparent,
                    0.68f to Color.Black.copy(alpha = 0.22f),
                    1.00f to Color.Black.copy(alpha = HOME_BACKGROUND_GRADIENT_ALPHA)
                )
            )
        )
    }
}

@Composable
private fun AllSectionContent(
    title: String,
    matches: List<MatchItem>,
    onMatchClick: (String) -> Unit,
    onBack: () -> Unit
) {
    Column(Modifier.fillMaxSize().padding(horizontal = 28.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Button(
                onClick = onBack,
                colors = ButtonDefaults.colors(
                    containerColor = Color(0xFF414654),
                    focusedContainerColor = com.svoysport.tv.ui.theme.Primary
                ),
                shape = ButtonDefaults.shape(RoundedCornerShape(18.dp))
            ) { Text("← Назад", color = Color.White) }
            Text(
                text = title,
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
            )
        }
        LazyVerticalGrid(
            columns = GridCells.Adaptive(230.dp),
            contentPadding = PaddingValues(bottom = 32.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            items(matches, key = { it.id }) { match ->
                com.svoysport.tv.ui.components.MatchCard(match = match, onClick = onMatchClick)
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

private val ALL_NAMED_SPORTS = SPORT_KEYWORDS.values

internal fun sectionTitleForSport(title: String, sport: SidebarItem?): String {
    if (sport == null || ALL_NAMED_SPORTS.none { title.contains(it, ignoreCase = true) }) return title
    return if (sport == SidebarItem.OTHER) "Другой спорт" else SPORT_KEYWORDS[sport] ?: title
}

internal fun sportMatchesSelection(
    competition: String,
    title: String,
    sport: SidebarItem?
): Boolean {
    val searchable = "$competition $title"
    if (sport == null) return true
    if (sport == SidebarItem.OTHER) {
        return ALL_NAMED_SPORTS.none { searchable.contains(it, ignoreCase = true) }
    }
    val keyword = SPORT_KEYWORDS[sport] ?: return false
    return searchable.contains(keyword, ignoreCase = true)
}

private fun filterBySport(
    sections: List<HomeSection>,
    sport: SidebarItem?
): List<HomeSection> {
    if (sport == null) return sections
    return sections
        .map { section ->
            section.copy(
                title = sectionTitleForSport(section.title, sport),
                matches = section.matches.filter { match ->
                    sportMatchesSelection(match.competition.name, match.title, sport)
                }
            )
        }
        .filter { it.matches.isNotEmpty() }
}

internal fun orderedVisibleSections(sections: List<HomeSection>): List<HomeSection> =
    sections.filter { it.matches.isNotEmpty() }.sortedBy { section ->
        when {
            section.id.equals("live", ignoreCase = true) ||
                section.title.equals("Онлайн", ignoreCase = true) -> 0
            section.id.equals("upcoming", ignoreCase = true) ||
                section.title.contains("Предстоящ", ignoreCase = true) -> 1
            else -> 2
        }
    }
