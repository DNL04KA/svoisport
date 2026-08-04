package com.svoysport.tv.ui.screens.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.svoysport.tv.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull

internal object SplashTimeline {
    const val initialFrameMs = 600L
    const val backgroundRevealMs = 1_000
    const val logoFrameMs = 200L
    const val wordmarkRevealMs = 1_000
    const val loaderRevealMs = 500
    const val minimumDurationMs = initialFrameMs + backgroundRevealMs + logoFrameMs +
        wordmarkRevealMs + loaderRevealMs
}

internal object SplashGeometry {
    const val logoSizeDp = 100
    const val loaderSizeDp = 40
    const val groupOffsetXDp = -15
    const val groupOffsetYDp = 35
}

private val EaseOut = Easing { fraction -> 1f - (1f - fraction) * (1f - fraction) }

private val EaseInOutBack = Easing { fraction ->
    val overshoot = 1.70158f
    val adjusted = overshoot * 1.525f
    if (fraction < 0.5f) {
        val value = 2f * fraction
        (value * value * ((adjusted + 1f) * value - adjusted)) / 2f
    } else {
        val value = 2f * fraction - 2f
        (value * value * ((adjusted + 1f) * value + adjusted) + 2f) / 2f
    }
}

@Composable
fun SplashScreen(
    onTimeout: () -> Unit,
    viewModel: SplashViewModel = hiltViewModel()
) {
    val validationComplete by viewModel.validationComplete.collectAsState()
    val logoAndBackground = remember { Animatable(0f) }
    val wordmark = remember { Animatable(0f) }
    val loader = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        delay(SplashTimeline.initialFrameMs)
        logoAndBackground.animateTo(
            targetValue = 1f,
            animationSpec = tween(SplashTimeline.backgroundRevealMs, easing = EaseOut)
        )
        delay(SplashTimeline.logoFrameMs)
        wordmark.animateTo(
            targetValue = 1f,
            animationSpec = tween(SplashTimeline.wordmarkRevealMs, easing = EaseInOutBack)
        )
        loader.animateTo(
            targetValue = 1f,
            animationSpec = tween(SplashTimeline.loaderRevealMs, easing = LinearEasing)
        )

        withTimeoutOrNull(5_000) {
            snapshotFlow { validationComplete }.first { it }
        }
        onTimeout()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0E0E0F))
    ) {
        Image(
            painter = painterResource(R.drawable.bg_app),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .alpha(logoAndBackground.value)
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = SplashGeometry.groupOffsetYDp.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.offset(x = SplashGeometry.groupOffsetXDp.dp)
            ) {
                Image(
                    painter = painterResource(R.drawable.logo_icon),
                    contentDescription = null,
                    modifier = Modifier
                        .size(SplashGeometry.logoSizeDp.dp)
                        .alpha(logoAndBackground.value)
                )

                Spacer(Modifier.width(20.dp))

                Text(
                    text = "СВОЙ СПОРТ",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 42.sp,
                        letterSpacing = 1.sp,
                        color = Color.White
                    ),
                    modifier = Modifier
                        .offset(x = (-40f * (1f - wordmark.value)).dp)
                        .alpha(wordmark.value.coerceIn(0f, 1f))
                )
            }

            Spacer(Modifier.height(48.dp))

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .height(SplashGeometry.loaderSizeDp.dp)
                    .alpha(loader.value)
            ) {
                SpinnerIndicator(
                    color = Color.White.copy(alpha = 0.55f),
                    size = SplashGeometry.loaderSizeDp.dp,
                    strokeWidth = 3.dp
                )
            }
        }
    }
}

@Composable
private fun SpinnerIndicator(
    color: Color,
    size: Dp,
    strokeWidth: Dp
) {
    val transition = rememberInfiniteTransition(label = "splash_spinner")
    val rotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1_000, easing = LinearEasing)
        ),
        label = "spinnerAngle"
    )

    Canvas(modifier = Modifier.size(size)) {
        val stroke = strokeWidth.toPx()
        val inset = stroke / 2f
        drawArc(
            color = color,
            startAngle = rotation - 90f,
            sweepAngle = 260f,
            useCenter = false,
            topLeft = Offset(inset, inset),
            size = Size(this.size.width - stroke, this.size.height - stroke),
            style = Stroke(width = stroke, cap = StrokeCap.Round)
        )
    }
}
