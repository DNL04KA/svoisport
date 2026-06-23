package com.svoysport.tv.ui.screens.profile

import androidx.activity.compose.BackHandler
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.*
import com.svoysport.tv.R

// ─── SubscriptionScreen ───────────────────────────────────────────────────────
// Figma 551:18485 — 1920×1080 full-screen
// Было: padding(start=445, top=65) и т.д. → теперь Column + Row с правильным layout

private val _SubPrimaryGrad = Brush.horizontalGradient(listOf(Color(0xFF4556EB), Color(0xFF273085)))
private val _CardDarkGrad   = Brush.verticalGradient(listOf(Color(0xFF000000), Color(0xFF42434E)))
private val _CardBlueGrad   = Brush.verticalGradient(listOf(Color(0xFF000000), Color(0xFF3D50F8)))
private val _BadgePrimaryGrad = Brush.horizontalGradient(listOf(Color(0xFF4556EB), Color(0xFF273085)))
private val _BadgeGrayGrad    = Brush.horizontalGradient(listOf(Color(0xFF8C8E9D), Color(0xFF2B2C2F)))

private data class PlanData(
    val title: String, val hint: String,
    val cardGrad: Brush, val badge: String?, val badgeGrad: Brush?
)

private val plans = listOf(
    PlanData("1 месяц",    "Попробовать сервис",    _CardDarkGrad, null,               null),
    PlanData("6 месяцев",  "Экономия 15%",          _CardBlueGrad, "Популярный выбор", _BadgePrimaryGrad),
    PlanData("12 месяцев", "Самая выгодная цена",   _CardDarkGrad, "Экономия 35%",     _BadgeGrayGrad)
)

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun SubscriptionScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    BackHandler(onBack = onBack)

    BoxWithConstraints(
        modifier = modifier.fillMaxSize().background(Color(0xFF0F0F10))
    ) {
        val sw = maxWidth.value
        val sh = maxHeight.value
        val scale = minOf(sw / 1920f, sh / 1080f, 1f).coerceAtLeast(0.35f)

        val pad        : Dp       = (60f  * scale).dp
        val closeSz    : Dp       = (80f  * scale).dp
        val titleSp    : TextUnit = (54f  * scale).coerceAtLeast(18f).sp
        val subtitleSp : TextUnit = (28f  * scale).coerceAtLeast(12f).sp
        val cardW      : Dp       = (400f * scale).dp
        val cardH      : Dp       = (462f * scale).dp
        val cardGap    : Dp       = (20f  * scale).dp
        val footnoteSp : TextUnit = (24f  * scale).coerceAtLeast(11f).sp
        val btnH       : Dp       = (80f  * scale).dp
        val btnW       : Dp       = (347f * scale).dp
        val btnFontSp  : TextUnit = (28f  * scale).coerceAtLeast(12f).sp
        val footerSp   : TextUnit = (20f  * scale).coerceAtLeast(11f).sp

        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = pad, vertical = pad),
            verticalArrangement = Arrangement.spacedBy((20f * scale).dp)
        ) {
            // ── Back button ──────────────────────────────────────────────────
            var backFocused by remember { mutableStateOf(false) }
            val backSc by animateFloatAsState(if (backFocused) 1.08f else 1f, tween(150), label = "back")
            Surface(
                onClick   = onBack,
                modifier  = Modifier.size(closeSz).onFocusChanged { backFocused = it.isFocused }.scale(backSc),
                shape     = ClickableSurfaceDefaults.shape(RoundedCornerShape(200.dp)),
                colors    = ClickableSurfaceDefaults.colors(containerColor = Color(0x33565A80), focusedContainerColor = Color(0x33565A80)),
                scale     = ClickableSurfaceDefaults.scale(scale = 1f, focusedScale = 1f)
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Icon(imageVector = ImageVector.vectorResource(R.drawable.ic_arrow_left),
                        contentDescription = "Назад", tint = Color(0xFFE2E2E2),
                        modifier = Modifier.size((24f * scale).dp))
                }
            }

            // ── Title + subtitle ─────────────────────────────────────────────
            // Figma: title centered at x=445 in 1920 screen → roughly center-ish after sidebar
            Column(
                modifier            = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy((16f * scale).dp)
            ) {
                Text(text = "Подписка", style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = titleSp, fontWeight = FontWeight.Medium, color = Color(0xFFE2E2E2)))
                Text(text = "Оформите подписку чтобы смотреть любые трансляции без ограничений",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = subtitleSp, fontWeight = FontWeight.Medium, color = Color(0xFFE2E2E2)))
            }

            Spacer(Modifier.height((20f * scale).dp))

            // ── Plan cards ────────────────────────────────────────────────────
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                plans.forEach { plan ->
                    PlanCard(plan = plan, width = cardW, height = cardH,
                        modifier = Modifier.padding(horizontal = cardGap / 2))
                }
            }

            // ── Footnote ──────────────────────────────────────────────────────
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(text = "*1 месяц равен 30 календарным дням",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = footnoteSp, fontWeight = FontWeight.Medium, color = Color(0xFFA8A9B2)))
            }

            // ── Subscribe button ──────────────────────────────────────────────
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                var btnFocused by remember { mutableStateOf(false) }
                val btnSc by animateFloatAsState(if (btnFocused) 1.08f else 1f, tween(150), label = "btn")
                Box(
                    modifier = Modifier.width(btnW).height(btnH)
                        .onFocusChanged { btnFocused = it.isFocused }.scale(btnSc)
                        .clip(RoundedCornerShape(20.dp)).background(_SubPrimaryGrad)
                ) {
                    Surface(
                        onClick  = { /* оформление */ },
                        modifier = Modifier.fillMaxSize(),
                        shape    = ClickableSurfaceDefaults.shape(RoundedCornerShape(20.dp)),
                        colors   = ClickableSurfaceDefaults.colors(containerColor = Color.Transparent, focusedContainerColor = Color.Transparent),
                        scale    = ClickableSurfaceDefaults.scale(scale = 1f, focusedScale = 1f)
                    ) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(text = "Оформить подписку", style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = btnFontSp, fontWeight = FontWeight.Medium, color = Color(0xFFE2E2E2)))
                        }
                    }
                }
            }

            // ── Footer ───────────────────────────────────────────────────────
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(text = "Договор публичной оферты",
                    style = TextStyle(
                        fontSize       = footerSp,
                        fontWeight     = FontWeight.Medium,
                        color          = Color(0xFF65666E),
                        textDecoration = TextDecoration.Underline
                    )
                )
            }
        }
    }
}

// ─── PlanCard ─────────────────────────────────────────────────────────────────

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun PlanCard(
    plan    : PlanData,
    width   : Dp       = 400.dp,
    height  : Dp       = 462.dp,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }
    val sc by animateFloatAsState(if (isFocused) 1.08f else 1f, tween(150), label = "card")

    Box(
        modifier = modifier.width(width).height(height)
            .onFocusChanged { isFocused = it.isFocused }.scale(sc)
            .clip(RoundedCornerShape(20.dp)).background(plan.cardGrad)
    ) {
        Surface(
            onClick  = { /* выбор тарифа */ },
            modifier = Modifier.fillMaxSize(),
            shape    = ClickableSurfaceDefaults.shape(RoundedCornerShape(20.dp)),
            colors   = ClickableSurfaceDefaults.colors(containerColor = Color.Transparent, focusedContainerColor = Color.Transparent),
            border   = ClickableSurfaceDefaults.border(
                focusedBorder = Border(border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF4556EB)),
                    shape = RoundedCornerShape(20.dp))
            ),
            scale = ClickableSurfaceDefaults.scale(scale = 1f, focusedScale = 1f)
        ) {
            Box(modifier = Modifier.fillMaxSize().padding(24.dp)) {
                Column(
                    modifier            = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(52.dp)
                ) {
                    Column(
                        modifier            = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(80.dp)
                    ) {
                        Text(text = plan.title, style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = (36f * (width.value / 400f)).coerceAtLeast(12f).sp,
                            fontWeight = FontWeight.SemiBold, color = Color(0xFFE2E2E2)))
                        Spacer(modifier = Modifier.fillMaxWidth().weight(1f))
                    }
                    Text(text = plan.hint, style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = (20f * (width.value / 400f)).coerceAtLeast(10f).sp,
                        fontWeight = FontWeight.Medium, color = Color(0xFFA8A9B2)))
                }
                if (plan.badge != null && plan.badgeGrad != null) {
                    Box(
                        modifier = Modifier.align(Alignment.TopCenter)
                            .clip(RoundedCornerShape(103.dp)).background(plan.badgeGrad)
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(text = plan.badge, style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = (20f * (width.value / 400f)).coerceAtLeast(10f).sp,
                            fontWeight = FontWeight.SemiBold, color = Color.White))
                    }
                }
            }
        }
    }
}
