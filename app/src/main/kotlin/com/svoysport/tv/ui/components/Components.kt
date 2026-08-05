package com.svoysport.tv.ui.components

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.*
import coil3.compose.AsyncImage
import com.svoysport.tv.R
import com.svoysport.tv.domain.model.MatchItem
import com.svoysport.tv.session.FavoritesManager
import com.svoysport.tv.ui.components.focus.tvFocusScale
import com.svoysport.tv.ui.components.nav.LeftSidebar
import com.svoysport.tv.ui.components.nav.NavTab
import com.svoysport.tv.ui.components.nav.SidebarItem
import com.svoysport.tv.ui.components.nav.TopNavigationBar
import com.svoysport.tv.ui.theme.*

// ─── TvScaffold ──────────────────────────────────────────────────────────────

internal const val sidebarExpansionDeltaDp = 160
internal fun sidebarContentStartDp(expanded: Boolean): Int = if (expanded) 220 else 60

@Composable
fun AppBackground(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(R.drawable.bg_app),
        contentDescription = null,
        modifier = modifier.fillMaxSize(),
        contentScale = ContentScale.Crop
    )
}

@Composable
fun TvScaffold(
    selectedTab: NavTab = NavTab.HOME,
    onTabSelected: (NavTab) -> Unit = {},
    isLoggedIn: Boolean = false,
    onAuthClick: () -> Unit = {},
    topBarHidden: Boolean = false,
    selectedSidebarItem: SidebarItem? = null,
    onSidebarItemSelected: (SidebarItem) -> Unit = {},
    background: @Composable BoxScope.() -> Unit = { AppBackground() },
    content: @Composable () -> Unit
) {
    var sidebarExpanded by remember { mutableStateOf(false) }
    val contentStart by animateDpAsState(
        targetValue = sidebarContentStartDp(sidebarExpanded).dp,
        animationSpec = tween(
            durationMillis = if (sidebarExpanded) 300 else 200,
            easing = if (sidebarExpanded) CubicBezierEasing(0f, 0f, 0.58f, 1f)
            else CubicBezierEasing(0.42f, 0f, 1f, 1f)
        ),
        label = "sidebarContentStart"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        background()
        LeftSidebar(
            selectedItem     = selectedSidebarItem,
            onItemSelected   = onSidebarItemSelected,
            onExpandedChange = { sidebarExpanded = it },
            modifier         = Modifier.align(Alignment.TopStart)
        )
        Column(modifier = Modifier.fillMaxSize()) {
            AnimatedVisibility(
                visible = !topBarHidden,
                enter = slideInVertically(tween(220)) { -it } +
                    expandVertically(tween(220), expandFrom = Alignment.Top) + fadeIn(tween(160)),
                exit = slideOutVertically(tween(180)) { -it } +
                    shrinkVertically(tween(180), shrinkTowards = Alignment.Top) + fadeOut(tween(120))
            ) {
                TopNavigationBar(
                    selectedTab   = selectedTab,
                    onTabSelected = onTabSelected,
                    isLoggedIn    = isLoggedIn,
                    onAuthClick   = onAuthClick,
                    logoExpanded  = sidebarExpanded
                )
            }
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                Box(modifier = Modifier.fillMaxSize().padding(start = contentStart)) {
                    content()
                }
            }
        }
    }
}

// ─── SectionHeader ───────────────────────────────────────────────────────────

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        text  = title,
        style = MaterialTheme.typography.headlineLarge.copy(
            fontWeight = FontWeight.SemiBold,
            fontSize   = 20.sp,
            color      = Color.White
        ),
        modifier = modifier.padding(bottom = 12.dp)
    )
}

// ─── MatchCard ───────────────────────────────────────────────────────────────

internal object MatchCardVisualSpec {
    const val widthDp = 230f
    const val heightDp = 136f
    const val titleSizeSp = 16f
    const val titleLineHeightSp = 20f
    const val bottomScrimAlpha = 0.95f
}

internal fun contentCardScale(availableWidthDp: Float): Float = 1f

internal fun cardSportLabel(competitionName: String): String =
    competitionName.substringBefore('.').substringBefore(',').trim().ifEmpty { competitionName }

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun MatchCard(
    match    : MatchItem,
    onClick  : (String) -> Unit,
    scale    : Float    = 1f,
    onFocused: (MatchItem) -> Unit = {},
    modifier : Modifier = Modifier
) {
    val cardW     = (MatchCardVisualSpec.widthDp * scale).dp
    val cardH     = (MatchCardVisualSpec.heightDp * scale).dp
    val corner    = 12.dp
    var isFocused by remember { mutableStateOf(false) }

    Surface(
        onClick  = { onClick(match.id) },
        modifier = modifier.width(cardW).onFocusChanged {
            isFocused = it.isFocused
            if (it.isFocused) onFocused(match)
        }.tvFocusScale(isFocused),
        shape  = ClickableSurfaceDefaults.shape(RoundedCornerShape(corner)),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = Color.Transparent,
            focusedContainerColor = Color.Transparent
        ),
        border = ClickableSurfaceDefaults.border(
            border        = Border(border = androidx.compose.foundation.BorderStroke(0.dp, Color.Transparent), shape = RoundedCornerShape(corner)),
            focusedBorder = Border(border = androidx.compose.foundation.BorderStroke(2.dp, Primary), shape = RoundedCornerShape(corner))
        ),
        scale = ClickableSurfaceDefaults.scale(scale = 1f, focusedScale = 1f)
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(cardH)) {
            AsyncImage(
                model = match.thumbnailUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier.fillMaxSize().background(
                    Brush.verticalGradient(
                        0.00f to Color.Transparent,
                        0.38f to Color.Transparent,
                        0.72f to Color.Black.copy(alpha = 0.58f),
                        1.00f to Color.Black.copy(alpha = MatchCardVisualSpec.bottomScrimAlpha)
                    )
                )
            )
            if (match.isLive) {
                LiveBadge(modifier = Modifier.align(Alignment.TopStart).padding((8f * scale).dp))
            }
            val isFav = match.id in FavoritesManager.favoriteIds.value
            Box(
                modifier = Modifier.align(Alignment.TopEnd).padding((8f * scale).dp)
                    .size((24f * scale).dp).background(Color.Black.copy(alpha = 0.45f), RoundedCornerShape(6.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(
                        if (isFav) R.drawable.ic_bookmark_active else R.drawable.ic_bookmark
                    ),
                    contentDescription = if (isFav) "В избранном" else "Закладка",
                    tint = if (isFav) Primary else if (isFocused) Color.White else Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size((13f * scale).dp)
                )
            }
            Column(
                modifier = Modifier.align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(horizontal = (10f * scale).dp, vertical = (9f * scale).dp)
            ) {
                Text(
                    text = match.title,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = (MatchCardVisualSpec.titleSizeSp * scale).sp,
                        lineHeight = (MatchCardVisualSpec.titleLineHeightSp * scale).sp,
                        letterSpacing = 0.sp,
                        color = Color.White
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height((4f * scale).dp))
                Text(
                    text = cardSportLabel(match.competition.name),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = (17f * scale).sp,
                        color = Gray3
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (match.isSubscriptionRequired) {
                SubscriptionBadge(
                    modifier = Modifier.align(Alignment.BottomEnd).padding((8f * scale).dp)
                )
            }
        }
    }
}

// ─── LiveBadge ───────────────────────────────────────────────────────────────

@Composable
fun LiveBadge(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(LiveRed, RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 3.dp)
    ) {
        Text(
            text  = "Live",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize   = 13.sp,
                color      = Color.White
            )
        )
    }
}

// ─── SubscriptionBadge ───────────────────────────────────────────────────────

@Composable
fun SubscriptionBadge(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(Primary, RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 3.dp)
    ) {
        Text(
            text  = "Подписка",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Medium,
                fontSize   = 12.sp,
                color      = Color.White
            )
        )
    }
}

// ─── ContentRow ──────────────────────────────────────────────────────────────

@Composable
fun ContentRow(
    title         : String,
    matches       : List<MatchItem>,
    onMatchClick  : (String) -> Unit,
    onMatchFocused: (MatchItem) -> Unit = {},
    onRowFocused  : () -> Unit = {},
    onWatchMore   : (() -> Unit)? = null,
    firstCardFocusRequester: FocusRequester? = null,
    modifier      : Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier) {
        val scale = contentCardScale(maxWidth.value)
        val pad   = (20f * scale).dp
        val gap   = (14f * scale).dp

        Column {
            SectionHeader(title = title, modifier = Modifier.padding(start = pad))
            LazyRow(
                modifier = Modifier.softHorizontalEdges(),
                contentPadding        = PaddingValues(horizontal = pad),
                horizontalArrangement = Arrangement.spacedBy(gap)
            ) {
                itemsIndexed(items = matches, key = { _, match -> match.id }) { index, match ->
                    MatchCard(
                        match = match,
                        onClick = onMatchClick,
                        scale = scale,
                        onFocused = { focusedMatch ->
                            onRowFocused()
                            onMatchFocused(focusedMatch)
                        },
                        modifier = if (index == 0 && firstCardFocusRequester != null) {
                            Modifier.focusRequester(firstCardFocusRequester)
                        } else Modifier
                    )
                }
                if (onWatchMore != null) item {
                    WatchMoreCard(onClick = onWatchMore, scale = scale, onFocused = onRowFocused)
                }
            }
        }
    }
}

// ─── WatchMoreCard ───────────────────────────────────────────────────────────

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun WatchMoreCard(
    onClick  : () -> Unit,
    scale    : Float    = 1f,
    onFocused: () -> Unit = {},
    modifier : Modifier = Modifier
) {
    val cardW  = (MatchCardVisualSpec.widthDp * scale).dp
    val cardH  = (MatchCardVisualSpec.heightDp * scale).dp
    val corner = 12.dp
    var isFocused by remember { mutableStateOf(false) }

    Surface(
        onClick  = onClick,
        modifier = modifier.width(cardW).height(cardH)
            .onFocusChanged {
                isFocused = it.isFocused
                if (it.isFocused) onFocused()
            }.tvFocusScale(isFocused),
        shape  = ClickableSurfaceDefaults.shape(RoundedCornerShape(corner)),
        colors = ClickableSurfaceDefaults.colors(containerColor = White10, focusedContainerColor = White20),
        border = ClickableSurfaceDefaults.border(
            focusedBorder = Border(border = androidx.compose.foundation.BorderStroke(2.dp, Primary), shape = RoundedCornerShape(corner))
        ),
        scale = ClickableSurfaceDefaults.scale(scale = 1f, focusedScale = 1f)
    ) {
        Column(
            modifier            = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector        = ImageVector.vectorResource(R.drawable.ic_arrow_right),
                contentDescription = null,
                tint               = Color.White.copy(alpha = if (isFocused) 1f else 0.45f),
                modifier           = Modifier.size((24f * scale).dp)
            )
            Spacer(modifier = Modifier.height((8f * scale).dp))
            Text(
                text  = "Смотреть ещё",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontSize = (20f * scale).sp,
                    color    = Color.White.copy(alpha = if (isFocused) 1f else 0.45f)
                )
            )
        }
    }
}

fun Modifier.softHorizontalEdges(): Modifier = this
    .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
    .drawWithContent {
        drawContent()
        val edge = 42.dp.toPx().coerceAtMost(size.width * 0.12f)
        drawRect(
            brush = Brush.horizontalGradient(
                0f to Color.Transparent,
                edge / size.width to Color.Black,
                1f - edge / size.width to Color.Black,
                1f to Color.Transparent
            ),
            blendMode = BlendMode.DstIn
        )
    }
