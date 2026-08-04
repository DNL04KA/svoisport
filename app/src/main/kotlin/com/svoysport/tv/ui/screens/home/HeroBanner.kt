package com.svoysport.tv.ui.screens.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.*
import coil3.compose.AsyncImage
import com.svoysport.tv.R
import com.svoysport.tv.domain.model.MatchItem
import com.svoysport.tv.ui.components.LiveBadge
import com.svoysport.tv.ui.theme.*

private val HeroGradientColors = listOf(Color(0xFF4556EB), Color(0xFF273185))
private val HeroImageShape     = RoundedCornerShape(16.dp)

internal object HeroVisualSpec {
    const val heightDp = 340f
    const val titleSizeSp = 36f
    const val titleLineHeightSp = 44f
    const val bookmarkSizeDp = 40f
}

internal fun heroVisualScale(availableWidthDp: Float): Float =
    minOf(availableWidthDp / 1760f, 1f).coerceAtLeast(0.65f)

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun HeroBanner(
    match: MatchItem,
    onWatchClick: (String) -> Unit,
    onMatchFocused: (MatchItem) -> Unit = {},
    downFocusRequester: FocusRequester? = null,
    modifier: Modifier = Modifier
) {
    val focusRequester    = remember { FocusRequester() }
    var isButtonFocused   by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    val imageScale by animateFloatAsState(
        targetValue   = if (isButtonFocused) 1.02f else 1.0f,
        animationSpec = tween(200),
        label         = "heroImageScale"
    )

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val sw = maxWidth.value
        val scale = heroVisualScale(sw)

        val bannerH  : Dp = (HeroVisualSpec.heightDp * scale).dp
        val padH     : Dp = (20f  * scale).dp
        val padEnd   : Dp = (32f  * scale).dp
        val imgGap   : Dp = (28f  * scale).dp
        val leagueFs      = (13f  * scale).coerceAtLeast(10f).sp
        val titleFs       = (HeroVisualSpec.titleSizeSp * scale).coerceAtLeast(18f).sp
        val lineH         = (HeroVisualSpec.titleLineHeightSp * scale).coerceAtLeast(22f).sp
        val badgeSz  : Dp = (HeroVisualSpec.bookmarkSizeDp * scale).dp
        val badgeIco : Dp = (22f  * scale).dp

        Surface(
            onClick = { onWatchClick(match.id) },
            modifier = Modifier.fillMaxWidth().height(bannerH)
                .focusRequester(focusRequester)
                .focusProperties {
                    if (downFocusRequester != null) down = downFocusRequester
                }
                .onFocusChanged {
                    isButtonFocused = it.isFocused
                    if (it.isFocused) onMatchFocused(match)
                },
            colors = ClickableSurfaceDefaults.colors(
                containerColor = Color.Transparent,
                focusedContainerColor = Color.Transparent
            ),
            scale = ClickableSurfaceDefaults.scale(scale = 1f, focusedScale = 1f)
        ) {
            Row(
                modifier = Modifier.fillMaxSize()
                    .padding(start = padH, end = padEnd, top = padH, bottom = (8f * scale).dp),
                horizontalArrangement = Arrangement.spacedBy(imgGap),
                verticalAlignment = Alignment.CenterVertically
            ) {
            // ── Картинка слева (~42%) ─────────────────────────────────────────
            Box(modifier = Modifier.weight(0.42f).fillMaxHeight()) {
                if (isButtonFocused) {
                    Box(
                        modifier = Modifier.fillMaxSize().clip(HeroImageShape)
                            .background(Brush.linearGradient(HeroGradientColors)).padding(2.dp)
                    )
                }
                AsyncImage(
                    model              = match.backgroundUrl ?: match.thumbnailUrl,
                    contentDescription = null,
                    modifier           = Modifier.fillMaxSize()
                        .padding(if (isButtonFocused) 2.dp else 0.dp)
                        .clip(HeroImageShape).scale(imageScale),
                    contentScale = ContentScale.Crop
                )
            }

            // ── Инфо справа (~58%) ────────────────────────────────────────────
            Column(
                modifier            = Modifier.weight(0.58f).fillMaxHeight(),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text  = match.competition.name,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = leagueFs, color = Gray3, fontWeight = FontWeight.Medium
                    )
                )

                Spacer(Modifier.height((8f * scale).dp))

                Text(
                    text     = match.title,
                    style    = MaterialTheme.typography.displayLarge.copy(
                        fontSize = titleFs, fontWeight = FontWeight.Bold,
                        lineHeight = lineH, color = Color.White
                    ),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(Modifier.height((14f * scale).dp))

                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy((8f * scale).dp)
                ) {
                    if (match.isLive) LiveBadge()
                    Box(
                        modifier = Modifier.size(badgeSz)
                            .background(
                                if (isButtonFocused) Primary.copy(alpha = 0.40f)
                                else Color.White.copy(alpha = 0.16f),
                                RoundedCornerShape(8.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector        = ImageVector.vectorResource(R.drawable.ic_bookmark),
                            contentDescription = "Закладка",
                            tint               = if (isButtonFocused) Color.White else Color.White.copy(alpha = 0.70f),
                            modifier           = Modifier.size(badgeIco)
                        )
                    }
                    if (match.isHot) {
                        Text(text = "🔥", fontSize = (16f * scale).sp)
                    }
                }

            }
        }
    }
}
}
