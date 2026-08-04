package com.svoysport.tv.ui.screens.home

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

private const val HOME_BACKGROUND_BLUR_DP = 210f
private const val HOME_BACKGROUND_IMAGE_ALPHA = 0.30f
private const val HOME_BACKGROUND_GRADIENT_ALPHA = 0.30f
private const val SCAFFOLD_RAIL_WIDTH_DP = 60f
private const val SCAFFOLD_TOP_BAR_HEIGHT_DP = 56f

internal fun homeBackgroundWidth(contentWidthDp: Float): Float =
    contentWidthDp + SCAFFOLD_RAIL_WIDTH_DP * 2f

internal fun homeBackgroundHeight(contentHeightDp: Float): Float =
    contentHeightDp + SCAFFOLD_TOP_BAR_HEIGHT_DP

internal fun firstHomeContentCardIndex(): Int = 0

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

private object HomeBringIntoViewSpec : BringIntoViewSpec {
    override fun calculateScrollDistance(offset: Float, size: Float, containerSize: Float): Float =
        homeFocusScrollDistance(offset, size, containerSize)
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
        },
        isLoggedIn          = SessionManager.isLoggedIn.value ||
                              com.svoysport.tv.session.SubscriptionManager.isSubscribed.value,
        onAuthClick         = onAuthClick,
        topBarHidden        = topBarHidden,
        selectedSidebarItem = visibleSidebarSelection(sidebarMode, selectedSport),
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
                // Один цельный фон главной: обложка hero не перемещается и не
                // меняется при навигации по карточкам ниже.
                val bgMatch = uiState.content.featuredMatch
                val scrollState = rememberLazyListState()
                val firstContentCardFocusRequester = remember { FocusRequester() }

                LaunchedEffect(selectedSport) {
                    scrollState.scrollToItem(0)
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

                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    // Компенсируем отступы scaffold: динамическая обложка является
                    // единым фоном всей главной, а не отдельной полосой сверху.
                    Box(
                        modifier = Modifier
                            .offset(
                                x = (-SCAFFOLD_RAIL_WIDTH_DP).dp,
                                y = (-SCAFFOLD_TOP_BAR_HEIGHT_DP).dp
                            )
                            .requiredSize(
                                width = homeBackgroundWidth(maxWidth.value).dp,
                                height = homeBackgroundHeight(maxHeight.value).dp
                            )
                            .clipToBounds()
                    ) {
                        AsyncImage(
                            model = bgMatch.backgroundUrl ?: bgMatch.thumbnailUrl,
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxSize()
                                .alpha(HOME_BACKGROUND_IMAGE_ALPHA)
                                .blur(
                                    radius = HOME_BACKGROUND_BLUR_DP.dp,
                                    edgeTreatment = BlurredEdgeTreatment.Unbounded
                                ),
                            contentScale = ContentScale.Crop
                        )

                        Box(
                            modifier = Modifier.fillMaxSize().background(
                                Brush.verticalGradient(
                                    0.00f to androidx.compose.ui.graphics.Color.Transparent,
                                    0.20f to androidx.compose.ui.graphics.Color.Transparent,
                                    0.68f to androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.22f),
                                    1.00f to androidx.compose.ui.graphics.Color.Black.copy(
                                        alpha = HOME_BACKGROUND_GRADIENT_ALPHA
                                    )
                                )
                            )
                        )
                    }

                    CompositionLocalProvider(LocalBringIntoViewSpec provides HomeBringIntoViewSpec) {
                        LazyColumn(
                            state = scrollState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 32.dp)
                        ) {
                            item(key = "hero") {
                                HeroBanner(
                                    match          = uiState.content.featuredMatch,
                                    onWatchClick   = onMatchClick,
                                    downFocusRequester = firstContentCardFocusRequester
                                )
                            }
                            item(key = "hero-gap") { Spacer(modifier = Modifier.height(12.dp)) }
                            itemsIndexed(sections, key = { _, section -> section.id }) { sectionIndex, section ->
                                ContentRow(
                                    title          = section.title,
                                    matches        = section.matches,
                                    onMatchClick   = onMatchClick,
                                    firstCardFocusRequester = if (sectionIndex == 0) firstContentCardFocusRequester else null,
                                    onWatchMore    = { onWatchMore(section.title) },
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
