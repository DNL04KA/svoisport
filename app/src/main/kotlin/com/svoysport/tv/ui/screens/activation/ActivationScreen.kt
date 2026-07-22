package com.svoysport.tv.ui.screens.activation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloat
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
    onFinished: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ActivationViewModel = hiltViewModel()
) {
    BackHandler(onBack = onBack)
    val state by viewModel.state.collectAsState()

    BoxWithConstraints(modifier = modifier.fillMaxSize().background(Background)) {
        val scale = minOf(maxWidth.value / 1920f, maxHeight.value / 1080f, 1f).coerceAtLeast(0.4f)

        when (val s = state) {
            is ActivationUi.Success -> SuccessContent(until = s.until, scale = scale, onFinished = onFinished)
            is ActivationUi.Error   -> ErrorContent(message = s.message, scale = scale, onRetry = viewModel::start, onBack = onBack)
            is ActivationUi.Loading -> CenterMessage("Готовим активацию…", scale)
            is ActivationUi.Qr      -> QrContent(qrUrl = s.qrUrl, scale = scale, onBack = onBack)
        }
    }
}

@Composable
private fun QrContent(qrUrl: String, scale: Float, onBack: () -> Unit) {
    val qrPx = (360f * scale).dp
    val qrBitmap: ImageBitmap? = remember(qrUrl) { QrCodeGenerator.generate(qrUrl, 512) }

    Row(
        modifier = Modifier.fillMaxSize().padding((56f * scale).dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy((56f * scale).dp)
    ) {
        // QR на белой карточке (нужна светлая «тихая зона»)
        Box(
            modifier = Modifier.size(qrPx + (32f * scale).dp)
                .clip(RoundedCornerShape((20f * scale).dp)).background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            if (qrBitmap != null) {
                Image(bitmap = qrBitmap, contentDescription = "QR-код активации", modifier = Modifier.size(qrPx))
            } else {
                Text("QR", color = Color.Black, fontSize = (40f * scale).sp)
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Активация устройства",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = (42f * scale).sp, fontWeight = FontWeight.SemiBold, color = Color.White
                )
            )
            Spacer(Modifier.height((24f * scale).dp))
            listOf(
                "Отсканируйте QR-код камерой телефона",
                "Введите e-mail, на который оформлена подписка",
                "Вам придёт код — введите его на сайте",
                "Телевизор активируется автоматически"
            ).forEachIndexed { i, step ->
                Row(
                    modifier = Modifier.padding(vertical = (7f * scale).dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy((14f * scale).dp)
                ) {
                    Box(
                        modifier = Modifier.size((30f * scale).dp)
                            .background(Color(0xFF4556EB), RoundedCornerShape(50)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("${i + 1}", color = Color.White, fontSize = (16f * scale).sp, fontWeight = FontWeight.Bold)
                    }
                    Text(step, color = Color(0xFFE2E2E2), fontSize = (22f * scale).sp)
                }
            }
            Spacer(Modifier.height((20f * scale).dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy((10f * scale).dp)) {
                PulsingDot(scale)
                Text("Ожидаем подтверждение…", color = Gray3, fontSize = (18f * scale).sp)
            }
            Spacer(Modifier.height((28f * scale).dp))
            ActionButton(text = "Назад", icon = R.drawable.ic_arrow_left, primary = false, scale = scale, onClick = onBack)
        }
    }
}

@Composable
private fun SuccessContent(until: String, scale: Float, onFinished: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding((48f * scale).dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size((96f * scale).dp).background(Success, RoundedCornerShape(50)),
            contentAlignment = Alignment.Center
        ) {
            Text("✓", color = Color.White, fontSize = (56f * scale).sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height((28f * scale).dp))
        Text(
            "Активация выполнена",
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = (44f * scale).sp, fontWeight = FontWeight.SemiBold, color = Color.White
            )
        )
        Spacer(Modifier.height((12f * scale).dp))
        Text("Ваша подписка активна до $until", color = Gray3, fontSize = (24f * scale).sp)
        Spacer(Modifier.height((36f * scale).dp))
        val fr = remember { androidx.compose.ui.focus.FocusRequester() }
        LaunchedEffect(Unit) { runCatching { fr.requestFocus() } }
        ActionButton(text = "На главную", icon = null, primary = true, scale = scale, focusRequester = fr, onClick = onFinished)
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
