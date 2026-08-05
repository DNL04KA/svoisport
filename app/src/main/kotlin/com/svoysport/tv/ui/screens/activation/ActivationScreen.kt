package com.svoysport.tv.ui.screens.activation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloat
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.tv.material3.*
import kotlinx.coroutines.delay
import com.svoysport.tv.R
import com.svoysport.tv.ui.theme.Background
import com.svoysport.tv.ui.theme.Gray3
import com.svoysport.tv.ui.theme.Success
import com.svoysport.tv.util.QrCodeGenerator

/**
 * Экран активации по QR: показывает код и опрашивает сервер; при успехе —
 * экран «Активация выполнена». Шаги 6/12 из флоу.
 */
@Composable
fun ActivationScreen(
    planId: String?,
    onFinished: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ActivationViewModel = hiltViewModel()
) {
    BackHandler(onBack = onBack)
    val state by viewModel.state.collectAsState()
    LaunchedEffect(planId) { viewModel.start(planId) }

    BoxWithConstraints(modifier = modifier.fillMaxSize().background(Background)) {
        val scale = minOf(maxWidth.value / 1920f, maxHeight.value / 1080f, 1f).coerceAtLeast(0.4f)

        when (val s = state) {
            is ActivationUi.Success -> SuccessContent(until = s.until, scale = scale, onFinished = onFinished)
            is ActivationUi.Error   -> ErrorContent(message = s.message, scale = scale, onRetry = viewModel::start, onBack = onBack)
            is ActivationUi.Loading -> CenterMessage("Готовим активацию…", scale)
            is ActivationUi.Qr      -> QrContent(qrUrl = s.qrUrl, planId = s.planId, scale = scale, onBack = onBack)
        }
    }
}

@Composable
private fun QrContent(qrUrl: String, planId: String?, scale: Float, onBack: () -> Unit) {
    val qrPx = (294f * scale).dp
    val qrBitmap: ImageBitmap? = remember(qrUrl) { QrCodeGenerator.generate(qrUrl, 512) }

    Box(
        modifier = Modifier.fillMaxSize().background(Color(0xFF050506)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Подписка",
            color = Color.White.copy(alpha = 0.12f),
            fontSize = (48f * scale).sp,
            modifier = Modifier.align(Alignment.TopCenter).padding(top = (62f * scale).dp)
        )

        Box(
            modifier = Modifier
                .size(width = (1320f * scale).dp, height = (828f * scale).dp)
                .clip(RoundedCornerShape((28f * scale).dp))
                .background(Color(0xFF202123))
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(
                    start = (182f * scale).dp,
                    top = (82f * scale).dp,
                    end = (182f * scale).dp
                ),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (planId == null) "Активация подписки по QR" else "Оформление подписки по QR",
                    color = Color.White,
                    fontSize = (36f * scale).sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height((30f * scale).dp))
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy((16f * scale).dp)
                ) {
                    val steps = if (planId == null) listOf(
                        "1. Наведите камеру телефона на QR-код",
                        "2. Авторизуйтесь и подтвердите активацию подписки",
                        "3. После подтверждения доступ на ТВ включится автоматически"
                    ) else listOf(
                        "1. Наведите камеру телефона на QR-код",
                        "2. На открывшейся странице выберите тариф и оплатите",
                        "3. После успешного оформления доступ на ТВ включится автоматически"
                    )
                    steps.forEach { step ->
                        Text(step, color = Color(0xFFD2D2D4), fontSize = (27f * scale).sp)
                    }
                }
                // Оставляем QR внутри карточки с заметным нижним воздухом:
                // прежние 112dp прижимали и обрезали блок у нижней границы.
                Spacer(Modifier.height((16f * scale).dp))
                Box(
                    modifier = Modifier
                        .size(width = (400f * scale).dp, height = (390f * scale).dp)
                        .clip(RoundedCornerShape((42f * scale).dp))
                        .background(Color(0xFF303239))
                        .border((4f * scale).dp, Color.White, RoundedCornerShape((42f * scale).dp)),
                    contentAlignment = Alignment.TopCenter
                ) {
                    if (qrBitmap != null) {
                        Image(
                            bitmap = qrBitmap,
                            contentDescription = "QR-код для оформления подписки",
                            modifier = Modifier.padding(top = (50f * scale).dp).size(qrPx)
                        )
                    } else {
                        Text("QR", color = Color.Black, fontSize = (40f * scale).sp)
                    }
                }
            }
            Button(
                onClick = onBack,
                modifier = Modifier.align(Alignment.TopEnd).padding((32f * scale).dp)
                    .size((80f * scale).dp),
                shape = ButtonDefaults.shape(RoundedCornerShape(50)),
                colors = ButtonDefaults.colors(containerColor = Color(0xFF4A4D68))
            ) {
                Text("×", color = Color(0xFFD2D2D4), fontSize = (38f * scale).sp)
            }
        }
    }
}

@Composable
private fun SuccessContent(until: String, scale: Float, onFinished: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(5_000)
        onFinished()
    }
    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.85f))) {
        val fr = remember { androidx.compose.ui.focus.FocusRequester() }
        LaunchedEffect(Unit) { runCatching { fr.requestFocus() } }
        Row(
            modifier = Modifier.align(Alignment.BottomCenter)
                .padding(bottom = (48f * scale).dp)
                .size(width = (1824f * scale).dp, height = (224f * scale).dp)
                .background(Color(0xFF1E1F20), RoundedCornerShape((32f * scale).dp))
                .padding(horizontal = (68f * scale).dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text("Подписка успешно оформлена", color = Color.White, fontSize = (48f * scale).sp, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height((12f * scale).dp))
                Text("Теперь вы можете смотреть любую трансляцию", color = Gray3, fontSize = (28f * scale).sp)
            }
            ActionButton(text = "Перейти на главную", icon = null, primary = true, scale = scale, focusRequester = fr, onClick = onFinished)
        }
    }
}

@Composable
private fun ErrorContent(message: String, scale: Float, onRetry: () -> Unit, onBack: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding((48f * scale).dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "Не удалось активировать",
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = (38f * scale).sp, fontWeight = FontWeight.SemiBold, color = Color.White
            )
        )
        Spacer(Modifier.height((12f * scale).dp))
        Text(message, color = Gray3, fontSize = (22f * scale).sp)
        Spacer(Modifier.height((32f * scale).dp))
        val fr = remember { androidx.compose.ui.focus.FocusRequester() }
        LaunchedEffect(Unit) { runCatching { fr.requestFocus() } }
        Row(horizontalArrangement = Arrangement.spacedBy((20f * scale).dp)) {
            ActionButton(text = "Попробовать снова", icon = null, primary = true, scale = scale, focusRequester = fr, onClick = onRetry)
            ActionButton(text = "Назад", icon = R.drawable.ic_arrow_left, primary = false, scale = scale, onClick = onBack)
        }
    }
}

@Composable
private fun CenterMessage(text: String, scale: Float) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy((14f * scale).dp)) {
            PulsingDot(scale)
            Text(text, color = Gray3, fontSize = (24f * scale).sp)
        }
    }
}

/** Простой индикатор ожидания (пульсирующая точка) — без зависимости material3. */
@Composable
private fun PulsingDot(scale: Float) {
    val transition = androidx.compose.animation.core.rememberInfiniteTransition(label = "dot")
    val alpha by transition.animateFloat(
        initialValue = 0.25f, targetValue = 1f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            animation = androidx.compose.animation.core.tween(650),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
        ),
        label = "dotAlpha"
    )
    Box(
        modifier = Modifier.size((14f * scale).dp)
            .background(Color(0xFF4556EB).copy(alpha = alpha), RoundedCornerShape(50))
    )
}
