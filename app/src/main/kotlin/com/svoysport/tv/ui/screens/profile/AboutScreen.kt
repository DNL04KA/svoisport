package com.svoysport.tv.ui.screens.profile

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.*
import com.svoysport.tv.R
import com.svoysport.tv.ui.components.AppBackground
import com.svoysport.tv.ui.theme.Primary
import com.svoysport.tv.ui.theme.PrimaryPressed

private val _AboutBg     = Color(0xFF0F0F10)
private val _AboutPanel  = Color(0x33565A80)
private val _AboutText   = Color(0xFFE2E2E2)
private val _AboutPrimary = Color(0xFF4556EB)

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun AboutScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        AppBackground()
        val sw = maxWidth.value
        val sh = maxHeight.value
        val scale = minOf(sw / 1920f, sh / 1080f, 1f).coerceAtLeast(0.35f)

        val pad       : Dp       = (60f  * scale).dp
        val closeSz   : Dp       = (80f  * scale).dp
        val iconSz    : Dp       = (24f  * scale).dp
        val logoSz    : Dp       = (80f  * scale).dp
        val titleSp   : TextUnit = (54f  * scale).coerceAtLeast(18f).sp
        val headSp    : TextUnit = (32f  * scale).coerceAtLeast(14f).sp
        val bodySp    : TextUnit = (24f  * scale).coerceAtLeast(12f).sp
        val smallSp   : TextUnit = (20f  * scale).coerceAtLeast(11f).sp
        val sectionGap: Dp       = (40f  * scale).dp
        val itemGap   : Dp       = (16f  * scale).dp

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = pad, vertical = pad)
        ) {
            // ── Back button + Title ──────────────────────────────────────────
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy((20f * scale).dp)
            ) {
                var backFocused by remember { mutableStateOf(false) }
                val backSc by animateFloatAsState(if (backFocused) 1.08f else 1f, tween(150), label = "back")
                Surface(
                    onClick   = onBack,
                    modifier  = Modifier.size(closeSz).onFocusChanged { backFocused = it.isFocused }.scale(backSc),
                    shape     = ClickableSurfaceDefaults.shape(RoundedCornerShape(200.dp)),
                    colors    = ClickableSurfaceDefaults.colors(
                        containerColor        = _AboutPanel,
                        focusedContainerColor = Primary,
                        pressedContainerColor = PrimaryPressed
                    ),
                    scale     = ClickableSurfaceDefaults.scale(scale = 1f, focusedScale = 1f)
                ) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector        = ImageVector.vectorResource(R.drawable.ic_arrow_left),
                            contentDescription = "Назад",
                            tint               = if (backFocused) Color.White else _AboutText,
                            modifier           = Modifier.size(iconSz)
                        )
                    }
                }
                Text(
                    text  = "О приложении",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = titleSp, fontWeight = FontWeight.Medium, color = _AboutText
                    )
                )
            }

            Spacer(Modifier.height((60f * scale).dp))

            // ── Logo ─────────────────────────────────────────────────────────
            Image(
                painter            = painterResource(R.drawable.logo_icon),
                contentDescription = "Свой Спорт",
                modifier           = Modifier.size(logoSz)
            )

            Spacer(Modifier.height((24f * scale).dp))

            // ── Description ──────────────────────────────────────────────────
            Text(
                text  = "Свой Спорт — приложение для просмотра спортивных трансляций в прямом эфире и в записи.\nСмотрите матчи любимых команд, изучайте расписание, добавляйте важные игры в избранное.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = bodySp, color = _AboutText.copy(alpha = 0.75f),
                    lineHeight = (bodySp.value * 1.5f).sp
                )
            )

            Spacer(Modifier.height(sectionGap))

            // ── Версия ───────────────────────────────────────────────────────
            AboutSectionScaled(title = "Версия приложения", headSp = headSp, gap = itemGap) {
                Text(
                    text  = "1.2.3",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = smallSp, color = _AboutText.copy(alpha = 0.70f)
                    )
                )
            }

            Spacer(Modifier.height(sectionGap))

            // ── Поддержка ────────────────────────────────────────────────────
            AboutSectionScaled(title = "Поддержка", headSp = headSp, gap = itemGap) {
                Text(
                    text  = "Если у вас возникли вопросы по работе сервиса, оплате или устройствам, свяжитесь с нами:",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = smallSp, color = _AboutText.copy(alpha = 0.50f)
                    )
                )
                Spacer(Modifier.height((8f * scale).dp))
                Text(
                    text  = "info@sport-tv.by",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = smallSp, color = _AboutPrimary
                    )
                )
            }

            Spacer(Modifier.height(sectionGap))

            // ── Правовая информация ──────────────────────────────────────────
            AboutSectionScaled(title = "Правовая информация", headSp = headSp, gap = itemGap) {
                Text(
                    text  = "Договор публичной оферты",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = smallSp, color = _AboutPrimary,
                        textDecoration = TextDecoration.Underline
                    )
                )
            }
        }
    }
}

@Composable
private fun AboutSectionScaled(
    title  : String,
    headSp : TextUnit,
    gap    : Dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Text(
        text  = title,
        style = MaterialTheme.typography.bodyLarge.copy(
            fontSize = headSp, fontWeight = FontWeight.SemiBold, color = Color(0xFFE2E2E2)
        )
    )
    Spacer(Modifier.height(gap))
    Column(content = content)
}
