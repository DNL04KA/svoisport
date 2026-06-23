package com.svoysport.tv.ui.screens.auth

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.*
import com.svoysport.tv.R
import com.svoysport.tv.ui.theme.*
import kotlinx.coroutines.delay

private const val CODE_LENGTH        = 6
private const val RESEND_COUNTDOWN_S = 59
private const val VERIFY_DELAY_MS   = 1_200L

private enum class VerifyState { IDLE, LOADING, ERROR_EXPIRED }

@Composable
fun CodeVerificationScreen(
    email:          String,
    onClose:        () -> Unit,
    onCodeComplete: (String) -> Unit
) {
    var code        by remember { mutableStateOf("") }
    var countdown   by remember { mutableStateOf(RESEND_COUNTDOWN_S) }
    var verifyState by remember { mutableStateOf(VerifyState.IDLE) }
    val firstNumFr  = remember { FocusRequester() }

    LaunchedEffect(Unit) { runCatching { firstNumFr.requestFocus() } }
    LaunchedEffect(Unit) { while (countdown > 0) { delay(1_000); countdown-- } }
    LaunchedEffect(code) {
        if (code.length == CODE_LENGTH) {
            verifyState = VerifyState.LOADING
            delay(VERIFY_DELAY_MS)
            verifyState = VerifyState.IDLE
            onCodeComplete(code)
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0F10))
            .drawBehind {
                drawCircle(
                    brush  = Brush.radialGradient(
                        colors = listOf(Color(0x404556EB), Color.Transparent),
                        center = Offset(size.width * 0.08f, size.height * 0.92f),
                        radius = size.minDimension * 0.80f
                    )
                )
                drawCircle(
                    brush  = Brush.radialGradient(
                        colors = listOf(Color(0x204556EB), Color.Transparent),
                        center = Offset(size.width * 0.88f, size.height * 0.06f),
                        radius = size.minDimension * 0.60f
                    )
                )
            }
    ) {
        val sw = maxWidth.value
        val sh = maxHeight.value
        val scale = minOf(sw / 1920f, sh / 1080f, 1f).coerceAtLeast(0.35f)

        val closeSz        : Dp       = (80f  * scale).dp
        val closePad       : Dp       = (60f  * scale).dp
        val titleSp        : TextUnit = (54f  * scale).coerceAtLeast(18f).sp
        val subtitleSp     : TextUnit = (32f  * scale).coerceAtLeast(14f).sp
        val cellSz         : Dp       = (96f  * scale).dp
        val cellGap        : Dp       = (12f  * scale).dp
        val numpadKeyH     : Dp       = (70f  * scale).dp
        val numpadKeyW     : Dp       = (70f  * scale).dp
        val numpadGap      : Dp       = (14f  * scale).dp
        val deleteW        : Dp       = (154f * scale).dp
        val clearW         : Dp       = (238f * scale).dp
        val spacerHdrCells : Dp       = (50f  * scale).dp
        val spacerCellsNp  : Dp       = (90f  * scale).dp
        val spacerNpResend : Dp       = (80f  * scale).dp
        val resendH        : Dp       = (80f  * scale).dp
        val resendW        : Dp       = (571f * scale).dp
        val resendActiveW  : Dp       = (636f * scale).dp

        // Close button
        CodeCloseButton(
            modifier  = Modifier
                .align(Alignment.TopEnd)
                .padding(top = closePad, end = closePad),
            size      = closeSz,
            onClick   = onClose
        )

        // Main content — centered
        Column(
            modifier            = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text  = "Введите код из письма",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontSize   = titleSp,
                    fontWeight = FontWeight.W500,
                    color      = Color(0xFFE2E2E2)
                )
            )
            Spacer(Modifier.height(closePad * 0.2f))
            Text(
                text  = "Письмо с кодом отправлено на почту $email",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize   = subtitleSp,
                    fontWeight = FontWeight.Normal,
                    color      = Color(0xFFE2E2E2)
                )
            )
            Spacer(Modifier.height(spacerHdrCells))
            CodeDigitRow(code = code, verifyState = verifyState, cellSz = cellSz, cellGap = cellGap)

            AnimatedVisibility(
                visible = verifyState == VerifyState.ERROR_EXPIRED,
                enter   = slideInVertically(tween(200)) { it } + fadeIn(tween(200)),
                exit    = fadeOut(tween(150))
            ) {
                Spacer(Modifier.height((10f * scale).dp))
                Text(
                    text  = "Код истёк — запросите новый",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontSize = (20f * scale).sp,
                        color    = Color(0xFFF08686)
                    )
                )
            }

            Spacer(Modifier.height(spacerCellsNp))

            CodeNumpad(
                isEnabled  = verifyState != VerifyState.LOADING && code.length < CODE_LENGTH,
                firstKeyFr = firstNumFr,
                keyH       = numpadKeyH,
                keyW       = numpadKeyW,
                gap        = numpadGap,
                deleteW    = deleteW,
                clearW     = clearW,
                fontSize   = (36f * scale).sp,
                fontSizeSm = (26f * scale).sp,
                onDigit    = { d -> if (code.length < CODE_LENGTH) code += d },
                onDelete   = { if (code.isNotEmpty()) code = code.dropLast(1) },
                onClear    = { code = "" }
            )

            Spacer(Modifier.height(spacerNpResend))

            ResendButton(
                countdown     = countdown,
                height        = resendH,
                ghostWidth    = resendW,
                activeWidth   = resendActiveW,
                fontSize      = (28f * scale).sp,
                iconSz        = (36f * scale).dp,
                onResend      = {
                    code        = ""
                    countdown   = RESEND_COUNTDOWN_S
                    verifyState = VerifyState.IDLE
                }
            )
        }

        AnimatedVisibility(
            visible  = verifyState == VerifyState.LOADING,
            enter    = fadeIn(tween(200)),
            exit     = fadeOut(tween(200)),
            modifier = Modifier.align(Alignment.Center)
        ) { VerifyingSpinner() }
    }
}

// ─── CodeCloseButton ─────────────────────────────────────────────────────────

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun CodeCloseButton(modifier: Modifier = Modifier, size: Dp = 80.dp, onClick: () -> Unit) {
    var isFocused by remember { mutableStateOf(false) }
    val sc by animateFloatAsState(if (isFocused) 1.08f else 1.0f, tween(150), label = "close")
    Surface(
        onClick  = onClick,
        modifier = modifier.size(size).onFocusChanged { isFocused = it.isFocused }.graphicsLayer(scaleX = sc, scaleY = sc),
        shape  = ClickableSurfaceDefaults.shape(CircleShape),
        colors = ClickableSurfaceDefaults.colors(
            containerColor        = Color(0xFF565A80).copy(alpha = 0.70f),
            focusedContainerColor = Color(0xFF565A80)
        ),
        border = ClickableSurfaceDefaults.border(
            focusedBorder = Border(
                border = androidx.compose.foundation.BorderStroke(2.dp, Primary),
                shape  = CircleShape
            )
        ),
        scale = ClickableSurfaceDefaults.scale(scale = 1.0f, focusedScale = 1.0f)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Icon(
                imageVector        = ImageVector.vectorResource(R.drawable.ic_close),
                contentDescription = "Закрыть",
                tint               = Color(0xFFA8A9B2),
                modifier           = Modifier.size(size * 0.5f)
            )
        }
    }
}

// ─── CodeDigitRow ─────────────────────────────────────────────────────────────

@Composable
private fun CodeDigitRow(code: String, verifyState: VerifyState, cellSz: Dp = 96.dp, cellGap: Dp = 12.dp) {
    val cursorAlpha by rememberInfiniteTransition(label = "cur").animateFloat(
        initialValue  = 1f,
        targetValue   = 0f,
        animationSpec = infiniteRepeatable(tween(520, easing = LinearEasing), RepeatMode.Reverse),
        label         = "ca"
    )
    Row(horizontalArrangement = Arrangement.spacedBy(cellGap)) {
        repeat(CODE_LENGTH) { idx ->
            val digit    = code.getOrNull(idx)?.toString() ?: ""
            val isActive = idx == code.length && verifyState == VerifyState.IDLE
            val isFilled = idx < code.length
            CodeDigitBox(
                digit       = digit,
                isActive    = isActive,
                isFilled    = isFilled,
                cursorAlpha = if (isActive) cursorAlpha else 0f,
                size        = cellSz
            )
        }
    }
}

// ─── CodeDigitBox ─────────────────────────────────────────────────────────────

@Composable
private fun CodeDigitBox(
    digit:       String,
    isActive:    Boolean,
    isFilled:    Boolean,
    cursorAlpha: Float,
    size:        Dp = 96.dp
) {
    val borderColor = when {
        isActive -> Primary
        isFilled -> Color.White.copy(alpha = 0.35f)
        else     -> Color(0xFFA8A9B2).copy(alpha = 0.35f)
    }
    Box(
        modifier = Modifier
            .size(size)
            .drawBehind {
                if (isActive) {
                    drawRoundRect(
                        color        = Primary.copy(alpha = 0.18f),
                        topLeft      = Offset(-5f, -5f),
                        size         = androidx.compose.ui.geometry.Size(this.size.width + 10f, this.size.height + 10f),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(16f),
                        style        = Stroke(width = 4f)
                    )
                }
            }
            .background(
                if (isActive) Primary.copy(alpha = 0.10f) else Color.Transparent,
                RoundedCornerShape(12.dp)
            )
            .border(2.dp, borderColor, RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (isFilled) {
            Text(
                text  = digit,
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontSize   = (size.value * 0.375f).sp,
                    fontWeight = FontWeight.W600,
                    color      = Color(0xFFE2E2E2)
                )
            )
        } else if (isActive) {
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(size * 0.33f)
                    .background(Primary.copy(alpha = cursorAlpha), RoundedCornerShape(1.dp))
            )
        }
    }
}

// ─── CodeNumpad ───────────────────────────────────────────────────────────────

@Composable
private fun CodeNumpad(
    isEnabled:  Boolean,
    firstKeyFr: FocusRequester,
    keyH:       Dp = 70.dp,
    keyW:       Dp = 70.dp,
    gap:        Dp = 14.dp,
    deleteW:    Dp = 154.dp,
    clearW:     Dp = 238.dp,
    fontSize:   TextUnit = 36.sp,
    fontSizeSm: TextUnit = 26.sp,
    onDigit:    (String) -> Unit,
    onDelete:   () -> Unit,
    onClear:    () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(gap)) {
        Row(horizontalArrangement = Arrangement.spacedBy(gap)) {
            listOf("1","2","3","4","5").forEachIndexed { idx, label ->
                NumpadKey(label = label, width = keyW, height = keyH, isEnabled = isEnabled,
                    fontSize = fontSize, firstKeyFr = if (idx == 0) firstKeyFr else null,
                    onClick = { onDigit(label) })
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(gap)) {
            listOf("6","7","8","9","0").forEach { label ->
                NumpadKey(label = label, width = keyW, height = keyH, isEnabled = isEnabled,
                    fontSize = fontSize, onClick = { onDigit(label) })
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(gap)) {
            NumpadKey(label = "⌫", width = deleteW, height = keyH, isEnabled = isEnabled,
                fontSize = fontSize, fontWeight = FontWeight.W400, onClick = onDelete)
            NumpadKey(label = "Очистить", width = clearW, height = keyH, isEnabled = isEnabled,
                fontSize = fontSizeSm, onClick = onClear)
        }
    }
}

// ─── NumpadKey ────────────────────────────────────────────────────────────────

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun NumpadKey(
    label:      String,
    width:      Dp,
    height:     Dp           = 70.dp,
    isEnabled:  Boolean      = true,
    fontSize:   TextUnit     = 36.sp,
    fontWeight: FontWeight   = FontWeight.W600,
    firstKeyFr: FocusRequester? = null,
    onClick:    () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    val sc by animateFloatAsState(if (isFocused && isEnabled) 1.08f else 1.0f, tween(150), label = "nk")
    Surface(
        onClick  = { if (isEnabled) onClick() },
        modifier = Modifier
            .width(width).height(height)
            .then(if (firstKeyFr != null) Modifier.focusRequester(firstKeyFr) else Modifier)
            .onFocusChanged { isFocused = it.isFocused }
            .graphicsLayer(scaleX = sc, scaleY = sc),
        shape  = ClickableSurfaceDefaults.shape(RoundedCornerShape(12.dp)),
        colors = ClickableSurfaceDefaults.colors(
            containerColor        = Color(0xFF343B4B),
            focusedContainerColor = if (isEnabled) Primary else Color(0xFF343B4B)
        ),
        border = ClickableSurfaceDefaults.border(
            focusedBorder = Border(
                border = androidx.compose.foundation.BorderStroke(2.dp, if (isEnabled) Primary else Color(0xFF343B4B)),
                shape  = RoundedCornerShape(12.dp)
            )
        ),
        scale = ClickableSurfaceDefaults.scale(scale = 1.0f, focusedScale = 1.0f)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize().padding(10.dp)) {
            Text(
                text      = label,
                style     = MaterialTheme.typography.bodyLarge.copy(
                    fontSize   = fontSize,
                    fontWeight = fontWeight,
                    color      = Color(0xFFE2E2E2).copy(if (isEnabled) 1f else 0.40f)
                ),
                textAlign = TextAlign.Center,
                maxLines  = 1
            )
        }
    }
}

// ─── ResendButton ─────────────────────────────────────────────────────────────

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun ResendButton(
    countdown:   Int,
    height:      Dp       = 80.dp,
    ghostWidth:  Dp       = 571.dp,
    activeWidth: Dp       = 636.dp,
    fontSize:    TextUnit = 28.sp,
    iconSz:      Dp       = 36.dp,
    onResend:    () -> Unit
) {
    if (countdown > 0) {
        Box(
            modifier         = Modifier.width(ghostWidth).height(height).graphicsLayer(alpha = 0.40f),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text  = "Выслать код повторно через $countdown сек",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontSize = fontSize, fontWeight = FontWeight.W500, color = Color(0xFFA8A9B2)
                )
            )
        }
    } else {
        var isFocused by remember { mutableStateOf(false) }
        val sc by animateFloatAsState(if (isFocused) 1.08f else 1.0f, tween(150), label = "resend")
        Surface(
            onClick  = onResend,
            modifier = Modifier
                .width(activeWidth).height(height)
                .onFocusChanged { isFocused = it.isFocused }
                .graphicsLayer(scaleX = sc, scaleY = sc),
            shape  = ClickableSurfaceDefaults.shape(RoundedCornerShape(20.dp)),
            colors = ClickableSurfaceDefaults.colors(
                containerColor        = Primary.copy(alpha = 0.75f),
                focusedContainerColor = Primary
            ),
            border = ClickableSurfaceDefaults.border(
                focusedBorder = Border(
                    border = androidx.compose.foundation.BorderStroke(2.dp, Color.White.copy(alpha = 0.4f)),
                    shape  = RoundedCornerShape(20.dp)
                )
            ),
            scale = ClickableSurfaceDefaults.scale(scale = 1.0f, focusedScale = 1.0f)
        ) {
            Row(
                modifier              = Modifier.fillMaxSize().padding(horizontal = 32.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector        = ImageVector.vectorResource(R.drawable.ic_arrow_right),
                    contentDescription = null,
                    tint               = Color(0xFFE2E2E2),
                    modifier           = Modifier.size(iconSz)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text  = "Выслать код повторно",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontSize = fontSize, fontWeight = FontWeight.W500, color = Color(0xFFE2E2E2)
                    )
                )
            }
        }
    }
}

// ─── VerifyingSpinner ────────────────────────────────────────────────────────

@Composable
private fun VerifyingSpinner() {
    val angle by rememberInfiniteTransition(label = "spin").animateFloat(
        initialValue  = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(900, easing = LinearEasing)),
        label         = "ang"
    )
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        androidx.compose.foundation.Canvas(Modifier.size(36.dp)) {
            drawArc(color = Primary.copy(alpha = 0.20f), startAngle = 0f, sweepAngle = 360f,
                useCenter = false, style = Stroke(width = 3.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round))
            drawArc(color = Primary, startAngle = angle, sweepAngle = 260f,
                useCenter = false, style = Stroke(width = 3.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round))
        }
        Text(
            text  = "Проверяем код…",
            style = MaterialTheme.typography.labelMedium.copy(fontSize = 14.sp, color = Gray3)
        )
    }
}
