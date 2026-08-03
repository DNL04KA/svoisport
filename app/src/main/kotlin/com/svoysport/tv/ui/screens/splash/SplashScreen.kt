package com.svoysport.tv.ui.screens.splash

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.svoysport.tv.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull
import androidx.hilt.navigation.compose.hiltViewModel

// ─── Константы ───────────────────────────────────────────────────────────────

/** Фон из спецификации — тёмно-синий */
private val BgColor = Color(0xFF0D0F1C)

/** Цвет радиального свечения из Figma (fill_2KBVK7 → #4556EB) */
private val GlowColor = Color(0xFF4556EB)

// Тайминги (из спецификации)
private const val DELAY_LOGO_MS     = 300L
private const val DELAY_SPINNER_MS  = 700L
private const val DELAY_NAVIGATE_MS = 1500L

// Длительности анимаций (из Figma-комментариев)
private const val ANIM_LOGO_MS    = 600   // fade + scale логотипа
private const val ANIM_TEXT_MS    = 800   // slide текста после логотипа
private const val ANIM_SPINNER_MS = 500   // fade спиннера (linear)

@Composable
fun SplashScreen(
    onTimeout: () -> Unit,
    viewModel: SplashViewModel = hiltViewModel()
) {

    var logoVisible    by remember { mutableStateOf(false) }
    var textVisible    by remember { mutableStateOf(false) }
    var spinnerVisible by remember { mutableStateOf(false) }
    val validationComplete by viewModel.validationComplete.collectAsState()

    // delay(300) показать лого → delay(700) показать спиннер → delay(1500) onTimeout()
    LaunchedEffect(Unit) {
        delay(DELAY_LOGO_MS)
        logoVisible = true
        textVisible = true
        delay(DELAY_SPINNER_MS)
        spinnerVisible = true
        delay(DELAY_NAVIGATE_MS)
        withTimeoutOrNull(5_000) {
            snapshotFlow { validationComplete }.first { it }
        }
        onTimeout()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgColor)
            // Радиальное свечение:
            //   Figma: Ellipse 1440×1440, radial #4556EB→transparent, blur(600px), opacity 0.5
            //   В Compose blur не поддерживается напрямую — эмулируем многослойным градиентом
            .drawBehind {
                val cx = size.width  / 2f
                val cy = size.height / 2f
                // Внешний широкий слой (имитация Gaussian blur 600px)
                drawCircle(
                    brush = Brush.radialGradient(
                        0.00f to GlowColor.copy(alpha = 0.30f),
                        0.35f to GlowColor.copy(alpha = 0.14f),
                        0.65f to GlowColor.copy(alpha = 0.05f),
                        1.00f to Color.Transparent,
                        center = Offset(cx, cy),
                        radius = size.minDimension * 0.90f
                    )
                )
                // Внутренний яркий центр
                drawCircle(
                    brush = Brush.radialGradient(
                        0.0f to GlowColor.copy(alpha = 0.22f),
                        1.0f to Color.Transparent,
                        center = Offset(cx, cy),
                        radius = size.minDimension * 0.45f
                    )
                )
            },
        contentAlignment = Alignment.Center
    ) {

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            // ── Лого + текст ────────────────────────────────────────────────
            // Figma frame 2→3: логотип появляется fade+scale, затем текст слайдится справа

            Row(
                verticalAlignment    = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {

                // Иконка лого: 200×200dp, borderRadius 52.5dp (из Figma layout_45QJYF)
                // AnimatedVisibility: fadeIn + scaleIn(0.88f, FastOutSlowInEasing)
                AnimatedVisibility(
                    visible = logoVisible,
                    enter = fadeIn(
                        animationSpec = tween(ANIM_LOGO_MS, easing = FastOutSlowInEasing)
                    ) + scaleIn(
                        initialScale  = 0.88f,
                        animationSpec = tween(ANIM_LOGO_MS, easing = FastOutSlowInEasing)
                    )
                ) {
                    Image(
                        painter            = painterResource(id = R.drawable.logo_icon),
                        contentDescription = null,
                        modifier           = Modifier.size(72.dp)
                    )
                }

                // Текст: Figma frame 3 — slides X=740→780 (≈40dp slide-in слева)
                AnimatedVisibility(
                    visible = textVisible,
                    enter = fadeIn(
                        animationSpec = tween(ANIM_TEXT_MS, delayMillis = 200, easing = FastOutSlowInEasing)
                    ) + slideInHorizontally(
                        initialOffsetX = { -48 },
                        animationSpec  = tween(ANIM_TEXT_MS, delayMillis = 200, easing = FastOutSlowInEasing)
                    )
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Spacer(modifier = Modifier.width(20.dp))
                        Text(
                            text  = "СВОЙ СПОРТ",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight    = FontWeight.Bold,
                                fontSize      = 42.sp,
                                letterSpacing = 3.sp,
                                color         = Color.White
                            )
                        )
                    }
                }
            }

            // ── Спиннер ─────────────────────────────────────────────────────
            // Figma frame 4: loader 80×80, ниже лого по центру, linear fade 500ms

            Spacer(modifier = Modifier.height(56.dp))

            AnimatedVisibility(
                visible = spinnerVisible,
                enter = fadeIn(
                    animationSpec = tween(ANIM_SPINNER_MS, easing = LinearEasing)
                )
            ) {
                // Figma layout_ZOTZ39: 80×80dp
                SpinnerIndicator(
                    color       = Color.White.copy(alpha = 0.65f),
                    size        = 32.dp,
                    strokeWidth = 2.5.dp
                )
            }
        }
    }
}

// ─── SpinnerIndicator ─────────────────────────────────────────────────────────
// Canvas + InfiniteTransition, 0→360°, 1000мс, LinearEasing, sweepAngle=260°, StrokeCap.Round
@Composable
private fun SpinnerIndicator(
    color: Color,
    size: Dp,
    strokeWidth: Dp
) {
    val transition = rememberInfiniteTransition(label = "splash_spinner")

    val rotation by transition.animateFloat(
        initialValue  = 0f,
        targetValue   = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing)
        ),
        label = "spinnerAngle"
    )

    Canvas(modifier = Modifier.size(size)) {
        val stroke = strokeWidth.toPx()
        val inset  = stroke / 2f
        drawArc(
            color      = color,
            startAngle = rotation - 90f,
            sweepAngle = 260f,
            useCenter  = false,
            topLeft    = Offset(inset, inset),
            size       = Size(this.size.width - stroke, this.size.height - stroke),
            style      = Stroke(width = stroke, cap = StrokeCap.Round)
        )
    }
}
