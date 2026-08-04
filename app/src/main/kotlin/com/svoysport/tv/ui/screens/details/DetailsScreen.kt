package com.svoysport.tv.ui.screens.details

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.tv.material3.*
import coil3.compose.AsyncImage
import com.svoysport.tv.R
import com.svoysport.tv.domain.model.MatchItem
import com.svoysport.tv.domain.model.Team
import com.svoysport.tv.session.FavoritesManager
import com.svoysport.tv.session.SessionManager
import com.svoysport.tv.ui.components.ContentRow
import com.svoysport.tv.ui.components.LiveBadge
import com.svoysport.tv.ui.components.state.HomeErrorState
import com.svoysport.tv.ui.components.state.HomeLoadingState
import com.svoysport.tv.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

// ─── DetailsScreen ────────────────────────────────────────────────────────────

@Composable
fun DetailsScreen(
    onWatchClick:  (String) -> Unit,
    onBack:        () -> Unit = {},
    onLoginClick:  () -> Unit = {},
    viewModel: DetailsViewModel = hiltViewModel()
) {
    val uiState    by viewModel.uiState.collectAsState()
    val isLoggedIn by SessionManager.isLoggedIn

    when (val state = uiState) {
        is DetailsUiState.Loading -> HomeLoadingState()
        is DetailsUiState.Error   -> HomeErrorState(
            message = state.message,
            onRetry = { viewModel.loadDetails() }
        )
        is DetailsUiState.Success -> DetailsContent(
            match        = state.match,
            related      = state.related,
            isLoggedIn   = isLoggedIn,
            onWatchClick = onWatchClick,
            onBack       = onBack,
            onLoginClick = onLoginClick,
            onRetryMatch = { viewModel.loadDetails() }
        )
    }
}

// ─── DetailsContent ──────────────────────────────────────────────────────────

@Composable
private fun DetailsContent(
    match:        MatchItem,
    related:      List<MatchItem>,
    isLoggedIn:   Boolean,
    onWatchClick: (String) -> Unit,
    onBack:       () -> Unit,
    onLoginClick: () -> Unit,
    onRetryMatch: () -> Unit
) {
    // Показываем диалог подписки если нужна подписка и пользователь не авторизован
    var showSubDialog by remember { mutableStateOf(false) }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val sw = maxWidth.value
        val sh = maxHeight.value
        val scale = minOf(sw / 1920f, sh / 1080f, 1f).coerceAtLeast(0.35f)

        val padStart : Dp       = (60f  * scale).dp
        val padEnd   : Dp       = (40f  * scale).dp
        val backSz   : Dp       = (80f  * scale).dp
        val backIco  : Dp       = (24f  * scale).dp
        val topPad   : Dp       = (60f  * scale).dp

        // ── Фоновое изображение на весь экран ────────────────────────────────
        AsyncImage(
            model              = match.backgroundUrl ?: match.thumbnailUrl,
            contentDescription = null,
            modifier           = Modifier.fillMaxSize(),
            contentScale       = ContentScale.Crop
        )

        Box(
            modifier = Modifier.fillMaxSize().background(
                Brush.horizontalGradient(
                    0.00f to Color(0xFF0D0F1C), 0.45f to Color(0xE0080B18),
                    0.68f to Color(0x990D0F1C), 0.88f to Color(0x220D0F1C),
                    1.00f to Color.Transparent
                )
            )
        )
        Box(
            modifier = Modifier.fillMaxWidth().height((220f * scale).dp).align(Alignment.BottomCenter)
                .background(Brush.verticalGradient(listOf(Color.Transparent, Color(0xF00D0F1C))))
        )

        // ── Основной контент ─────────────────────────────────────────────────
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            DetailsTopBar(onBack = onBack, backSz = backSz, backIco = backIco, topPad = topPad, padStart = padStart)

            Row(
                modifier = Modifier.fillMaxWidth().padding(start = padStart, end = padEnd, top = (12f * scale).dp),
                horizontalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                MatchInfoPanel(
                    match        = match,
                    isLoggedIn   = isLoggedIn,
                    scale        = scale,
                    modifier     = Modifier.weight(0.52f),
                    onWatchClick = { matchId ->
                        if (match.isSubscriptionRequired && !isLoggedIn) showSubDialog = true
                        else onWatchClick(matchId)
                    }
                )
                Spacer(modifier = Modifier.weight(0.48f))
            }

            Spacer(modifier = Modifier.height((40f * scale).dp))

            if (related.isNotEmpty()) {
                ContentRow(
                    title        = "Похожие матчи",
                    matches      = related,
                    onMatchClick = onWatchClick,
                    modifier     = Modifier.padding(bottom = (48f * scale).dp)
                )
            } else {
                Spacer(modifier = Modifier.height((48f * scale).dp))
            }
        }

        if (showSubDialog) {
            Dialog(
                onDismissRequest = { showSubDialog = false },
                properties = DialogProperties(
                    dismissOnBackPress = true,
                    dismissOnClickOutside = false,
                    usePlatformDefaultWidth = false
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.60f)),
                    contentAlignment = Alignment.Center
                ) {
                    SubscriptionDialog(
                        onLogin   = { showSubDialog = false; onLoginClick() },
                        onDismiss = { showSubDialog = false },
                        scale     = scale
                    )
                }
            }
        }
    }
}

// ─── DetailsTopBar ───────────────────────────────────────────────────────────
// Figma: IconButton 80×80dp bg=#565a80 r=200 (arrow-left icon 40×40dp)
// Позиция: padding top=60dp start=60dp (аналогично CodeVerification close button)

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun DetailsTopBar(
    onBack   : () -> Unit,
    backSz   : Dp = 80.dp,
    backIco  : Dp = 24.dp,
    topPad   : Dp = 60.dp,
    padStart : Dp = 60.dp
) {
    val backFr = remember { FocusRequester() }
    Box(modifier = Modifier.fillMaxWidth().padding(start = padStart, top = topPad)) {
        Surface(
            onClick  = onBack,
            modifier = Modifier.size(backSz).focusRequester(backFr),
            shape    = ClickableSurfaceDefaults.shape(RoundedCornerShape(200.dp)),
            colors   = ClickableSurfaceDefaults.colors(
                containerColor = Color(0x33565A80), focusedContainerColor = Primary
            ),
            scale = ClickableSurfaceDefaults.scale(focusedScale = 1.08f)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Icon(
                    imageVector        = ImageVector.vectorResource(R.drawable.ic_arrow_left),
                    contentDescription = "Назад",
                    tint               = Color.White,
                    modifier           = Modifier.size(backIco)
                )
            }
        }
    }
}

// ─── MatchInfoPanel ──────────────────────────────────────────────────────────

@Composable
private fun MatchInfoPanel(
    match        : MatchItem,
    isLoggedIn   : Boolean,
    scale        : Float    = 1f,
    modifier     : Modifier = Modifier,
    onWatchClick : (String) -> Unit
) {
    val watchFr = remember { FocusRequester() }
    LaunchedEffect(Unit) { watchFr.requestFocus() }

    val leagueFs : TextUnit = (12f * scale).coerceAtLeast(10f).sp
    val titleFs  : TextUnit = (64f * scale).coerceAtLeast(20f).sp
    val titleLH  : TextUnit = (76f * scale).coerceAtLeast(24f).sp
    val metaFs   : TextUnit = (13f * scale).coerceAtLeast(10f).sp
    val descFs   : TextUnit = (14f * scale).coerceAtLeast(11f).sp
    val descLH   : TextUnit = (22f * scale).coerceAtLeast(16f).sp
    val metaIco  : Dp       = (14f * scale).dp

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(0.dp)) {
        Text(
            text  = match.competition.name.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = leagueFs, fontWeight = FontWeight.SemiBold,
                color = Gray3, letterSpacing = 1.8.sp
            )
        )
        Spacer(Modifier.height((10f * scale).dp))

        if (match.isLive) {
            LiveBadge()
            Spacer(Modifier.height((10f * scale).dp))
        }

        Text(
            text     = match.title,
            style    = MaterialTheme.typography.displayLarge.copy(
                fontSize = titleFs, fontWeight = FontWeight.SemiBold,
                lineHeight = titleLH, color = Color.White
            ),
            maxLines = 2, overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.height((20f * scale).dp))

        if (match.homeTeam.name.isNotBlank() || match.awayTeam.name.isNotBlank()) {
            TeamsRow(homeTeam = match.homeTeam, awayTeam = match.awayTeam, isLive = match.isLive, scale = scale)
            Spacer(Modifier.height((16f * scale).dp))
        }

        val timeLabel = buildString {
            if (match.isLive) append("Идёт сейчас")
            else append("Начало в ${SimpleDateFormat("d MMMM, HH:mm", Locale("ru")).format(Date(match.startTimeMs))}")
        }
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy((6f * scale).dp)
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.ic_calendar),
                contentDescription = null, tint = Gray3,
                modifier = Modifier.size(metaIco)
            )
            Text(text = timeLabel, style = MaterialTheme.typography.labelMedium.copy(fontSize = metaFs, color = Gray3))
        }
        Spacer(Modifier.height((20f * scale).dp))

        Text(
            text     = match.description,
            style    = MaterialTheme.typography.bodyMedium.copy(
                fontSize = descFs, lineHeight = descLH, color = Color.White.copy(alpha = 0.65f)
            ),
            maxLines = 4, overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.height((36f * scale).dp))

        ActionButtons(match = match, isLoggedIn = isLoggedIn, watchFr = watchFr, scale = scale, onWatchClick = onWatchClick)
    }
}

// ─── TeamsRow ────────────────────────────────────────────────────────────────

@Composable
private fun TeamsRow(
    homeTeam: Team,
    awayTeam: Team,
    isLive:   Boolean,
    scale:    Float = 1f
) {
    Row(
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy((16f * scale).dp)
    ) {
        TeamChip(team = homeTeam, isHome = true, scale = scale)
        val scoreText = if (isLive && homeTeam.score != null && awayTeam.score != null)
            "${homeTeam.score}  :  ${awayTeam.score}" else "vs"
        Text(
            text  = scoreText,
            style = MaterialTheme.typography.headlineLarge.copy(
                fontSize = (22f * scale).sp, fontWeight = FontWeight.Bold, color = Color.White
            )
        )
        TeamChip(team = awayTeam, isHome = false, scale = scale)
    }
}

@Composable
private fun TeamChip(team: Team, isHome: Boolean, scale: Float = 1f) {
    val logoSz  : Dp = (40f * scale).dp
    val innerSz : Dp = (32f * scale).dp
    Row(
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy((10f * scale).dp)
    ) {
        Box(
            modifier         = Modifier.size(logoSz)
                .background(Color.White.copy(alpha = 0.10f), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (team.logoUrl != null) {
                AsyncImage(
                    model = team.logoUrl, contentDescription = team.name,
                    modifier = Modifier.size(innerSz).clip(RoundedCornerShape(6.dp)),
                    contentScale = ContentScale.Fit
                )
            } else {
                Text(
                    text  = team.name.take(2).uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = (11f * scale).sp, fontWeight = FontWeight.Bold, color = Color.White
                    )
                )
            }
        }
        Text(
            text  = team.name,
            style = MaterialTheme.typography.labelMedium.copy(
                fontSize = (15f * scale).sp, fontWeight = FontWeight.SemiBold, color = Color.White
            )
        )
    }
}

// ─── ActionButtons ───────────────────────────────────────────────────────────

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun ActionButtons(
    match        : MatchItem,
    isLoggedIn   : Boolean,
    watchFr      : FocusRequester,
    scale        : Float = 1f,
    onWatchClick : (String) -> Unit
) {
    var isWatchFocused    by remember { mutableStateOf(false) }
    var isBookmarkFocused by remember { mutableStateOf(false) }
    val isBookmarked      = match.id in FavoritesManager.favoriteIds.value

    val watchScale    by animateFloatAsState(if (isWatchFocused)    1.08f else 1f, tween(150), label = "ws")
    val bookmarkScale by animateFloatAsState(if (isBookmarkFocused) 1.08f else 1f, tween(150), label = "bs")

    val btnW   : Dp = (202f * scale).dp
    val btnH   : Dp = (80f  * scale).dp
    val bkSz   : Dp = (52f  * scale).dp
    val bkIco  : Dp = (20f  * scale).dp
    val watchFs      = (28f  * scale).sp
    val gradBrush    = Brush.horizontalGradient(listOf(Color(0xFF4556EB), Color(0xFF6B78F0)))

    Row(
        horizontalArrangement = Arrangement.spacedBy((12f * scale).dp),
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Surface(
            onClick  = { onWatchClick(match.id) },
            modifier = Modifier.width(btnW).height(btnH)
                .focusRequester(watchFr).onFocusChanged { isWatchFocused = it.isFocused }.scale(watchScale),
            shape  = ClickableSurfaceDefaults.shape(RoundedCornerShape(20.dp)),
            colors = ClickableSurfaceDefaults.colors(containerColor = Color.Transparent, focusedContainerColor = Color.Transparent),
            scale  = ClickableSurfaceDefaults.scale(scale = 1f, focusedScale = 1f)
        ) {
            Box(modifier = Modifier.fillMaxSize().background(gradBrush, RoundedCornerShape(20.dp)), contentAlignment = Alignment.Center) {
                Text(text = "Смотреть", style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = watchFs, fontWeight = FontWeight.Medium, color = Color(0xFFE2E2E2)))
            }
        }

        Surface(
            onClick  = { FavoritesManager.toggle(match.id) },
            modifier = Modifier.size(bkSz).onFocusChanged { isBookmarkFocused = it.isFocused }.scale(bookmarkScale),
            shape    = ClickableSurfaceDefaults.shape(CircleShape),
            colors   = ClickableSurfaceDefaults.colors(
                containerColor = Color.White.copy(alpha = 0.12f), focusedContainerColor = Color.White.copy(alpha = 0.20f)
            ),
            border = ClickableSurfaceDefaults.border(
                focusedBorder = Border(border = androidx.compose.foundation.BorderStroke(2.dp, Primary), shape = CircleShape)
            ),
            scale = ClickableSurfaceDefaults.scale(scale = 1f, focusedScale = 1f)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Icon(
                    imageVector        = ImageVector.vectorResource(
                        if (isBookmarked || isBookmarkFocused) R.drawable.ic_bookmark_active else R.drawable.ic_bookmark
                    ),
                    contentDescription = if (isBookmarked) "Убрать из закладок" else "В закладки",
                    tint               = if (isBookmarked) Primary else Color.White,
                    modifier           = Modifier.size(bkIco)
                )
            }
        }

        if (match.isSubscriptionRequired && !isLoggedIn) {
            SubscriptionBadgeInline(scale = scale)
        }
    }
}

// ─── SubscriptionBadgeInline ─────────────────────────────────────────────────

@Composable
private fun SubscriptionBadgeInline(scale: Float = 1f) {
    Row(
        modifier              = Modifier
            .background(Primary.copy(alpha = 0.18f), RoundedCornerShape(20.dp))
            .padding(horizontal = (12f * scale).dp, vertical = (6f * scale).dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy((6f * scale).dp)
    ) {
        Icon(
            imageVector        = ImageVector.vectorResource(R.drawable.ic_info),
            contentDescription = null, tint = Primary,
            modifier           = Modifier.size((14f * scale).dp)
        )
        Text(
            text  = "Требуется подписка",
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = (12f * scale).sp, fontWeight = FontWeight.Medium, color = Primary
            )
        )
    }
}

// ─── SubscriptionDialog ──────────────────────────────────────────────────────

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun SubscriptionDialog(
    onLogin   : () -> Unit,
    onDismiss : () -> Unit,
    scale     : Float = 1f
) {
    var isLoginFocused   by remember { mutableStateOf(false) }
    var isDismissFocused by remember { mutableStateOf(false) }

    val loginFr = remember { FocusRequester() }
    LaunchedEffect(Unit) { loginFr.requestFocus() }

    val loginScale   by animateFloatAsState(if (isLoginFocused)   1.08f else 1f, tween(150), label = "l")
    val dismissScale by animateFloatAsState(if (isDismissFocused) 1.05f else 1f, tween(150), label = "d")

    Box(
        modifier = Modifier
            .width((480f * scale).dp)
            .background(Color(0xFF1A1C2E), RoundedCornerShape(20.dp))
            .padding((36f * scale).dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            Box(
                modifier         = Modifier.size((56f * scale).dp).background(Primary.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_user),
                    contentDescription = null, tint = Primary,
                    modifier = Modifier.size((26f * scale).dp)
                )
            }
            Spacer(Modifier.height((20f * scale).dp))
            Text(
                text  = "Требуется подписка",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontSize = (22f * scale).sp, fontWeight = FontWeight.Bold, color = Color.White
                )
            )
            Spacer(Modifier.height((12f * scale).dp))
            Text(
                text  = "Для просмотра этого матча необходимо\nоформить подписку или войти в аккаунт.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = (14f * scale).sp, lineHeight = (21f * scale).sp, color = Gray3
                ),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(Modifier.height((32f * scale).dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy((12f * scale).dp),
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Button(
                    onClick  = onLogin,
                    modifier = Modifier.focusRequester(loginFr)
                        .onFocusChanged { isLoginFocused = it.isFocused }.scale(loginScale).weight(1f),
                    shape  = ButtonDefaults.shape(shape = RoundedCornerShape(24.dp)),
                    colors = ButtonDefaults.colors(
                        containerColor         = Blue100,
                        focusedContainerColor  = Primary,
                        pressedContainerColor  = PrimaryPressed,
                        disabledContainerColor = PrimaryDisabled
                    ),
                    scale  = ButtonDefaults.scale(scale = 1f, focusedScale = 1f)
                ) {
                    Text(
                        text     = "Войти",
                        modifier = Modifier.padding(vertical = (6f * scale).dp),
                        style    = MaterialTheme.typography.labelMedium.copy(
                            fontSize = (15f * scale).sp, fontWeight = FontWeight.SemiBold, color = Color.White
                        )
                    )
                }
                Surface(
                    onClick  = onDismiss,
                    modifier = Modifier.onFocusChanged { isDismissFocused = it.isFocused }
                        .scale(dismissScale).weight(1f).height((48f * scale).dp),
                    shape  = ClickableSurfaceDefaults.shape(RoundedCornerShape(24.dp)),
                    colors = ClickableSurfaceDefaults.colors(
                        containerColor = Color.White.copy(alpha = 0.08f), focusedContainerColor = Color.White.copy(alpha = 0.14f)
                    ),
                    border = ClickableSurfaceDefaults.border(
                        focusedBorder = Border(
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, Color.White.copy(alpha = 0.35f)),
                            shape  = RoundedCornerShape(24.dp)
                        )
                    ),
                    scale = ClickableSurfaceDefaults.scale(scale = 1f, focusedScale = 1f)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Text(
                            text  = "Не сейчас",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontSize = (15f * scale).sp, fontWeight = FontWeight.Normal,
                                color    = Color.White.copy(alpha = 0.75f)
                            )
                        )
                    }
                }
            }
        }
    }
}
