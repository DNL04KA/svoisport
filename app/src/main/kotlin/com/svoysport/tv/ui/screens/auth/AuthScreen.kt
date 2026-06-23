package com.svoysport.tv.ui.screens.auth

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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

fun isValidEmail(s: String) = s.contains('@') && s.contains('.')

// ─── AuthScreen ───────────────────────────────────────────────────────────────
// Figma 523:17964 — 1920×1080
// Единый scale = min(screenW/1920, screenH/1080) чтобы всё влезало на любом TV

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun AuthScreen(
    onClose:       () -> Unit,
    onEmailSubmit: (String) -> Unit
) {
    var email        by remember { mutableStateOf("") }
    var isRussian    by remember { mutableStateOf(false) }
    var isUpperCase  by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val firstKeyFr = remember { FocusRequester() }
    LaunchedEffect(Unit) { runCatching { firstKeyFr.requestFocus() } }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0F10))
    ) {
        val sw = maxWidth.value
        val sh = maxHeight.value

        // Масштаб по высоте — ключевой: контент должен ВЛЕЗТЬ по вертикали.
        // Полная высота контента на Figma (1080dp):
        //   title≈70 + gap32 + field96 + gap32 + button80 + spacer89 + 4 kbRows×70 + 3 kbGaps×14 = ~761dp
        // Оставляем 6% запас сверху и снизу → делим на 0.88
        val scaleH = (sh * 0.88f) / 761f
        // Масштаб по ширине — клавиатура EN: 9×70+8×14+32+3×70+2×14 = 1012dp; RU: 11×70+10×14+32+3×70+2×14 = 1180dp
        // Ориентируемся на RU (максимальная ширина), оставляем 4% полей с каждой стороны
        val scaleW = (sw * 0.92f) / 1180f
        // Берём минимум — но не больше 1.0 (Figma-max)
        val scale = minOf(scaleH, scaleW, 1f).coerceAtLeast(0.35f)

        // Масштабированные размеры с минимальными порогами читабельности
        val titleSp:  TextUnit = (54f * scale).coerceAtLeast(18f).sp
        val fieldH:   Dp = (96f  * scale).dp.coerceAtLeast(40.dp)
        val buttonH:  Dp = (80f  * scale).dp.coerceAtLeast(36.dp)
        val formGap:  Dp = (32f  * scale).dp
        val keySize:  Dp = (70f  * scale).dp.coerceAtLeast(28.dp)
        val keyGap:   Dp = (14f  * scale).dp.coerceAtLeast(4.dp)
        val blockGap: Dp = (32f  * scale).dp.coerceAtLeast(8.dp)
        val spacer:   Dp = (89f  * scale).dp
        // Ширина формы = ширина клавиатуры EN: 9×keySize + 8×keyGap + blockGap + 3×keySize + 2×keyGap
        val kSz = (70f * scale).coerceAtLeast(28f)
        val kGp = (14f * scale).coerceAtLeast(4f)
        val bGp = (32f * scale).coerceAtLeast(8f)
        val formW: Dp = (9 * kSz + 8 * kGp + bGp + 3 * kSz + 2 * kGp).dp
        val closeSz:  Dp = (80f  * scale).dp.coerceAtLeast(32.dp)
        val closePad: Dp = (60f  * scale).dp.coerceAtLeast(12.dp)

        // Фоновые свечения
        Box(modifier = Modifier.fillMaxSize().background(
            Brush.radialGradient(
                colors = listOf(Color(0x554556EB), Color.Transparent),
                center = androidx.compose.ui.geometry.Offset(sw * 0.15f, sh * 0.85f),
                radius = sw * 0.37f
            )
        ))
        Box(modifier = Modifier.fillMaxSize().background(
            Brush.radialGradient(
                colors = listOf(Color(0x334556EB), Color.Transparent),
                center = androidx.compose.ui.geometry.Offset(sw * 0.83f, sh * 0.17f),
                radius = sw * 0.31f
            )
        ))

        // Форма + клавиатура — истинное центрирование по всему экрану
        // Кнопка × плавает поверх через Alignment.TopEnd, не влияет на центр
        Column(
            modifier            = Modifier
                .fillMaxSize()
                .padding(horizontal = (sw * 0.04f).dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Форма: title + email + button
            Column(
                modifier            = Modifier.width(formW),
                verticalArrangement = Arrangement.spacedBy(formGap),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text  = "Вход в аккаунт",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize   = titleSp,
                        color      = Color(0xFFE2E2E2)
                    )
                )
                AuthEmailField(
                    value    = email,
                    hasError = errorMessage != null,
                    height   = fieldH,
                    fontSize = (28f * scale).sp
                )
                AuthContinueButton(
                    emailFilled = email.isNotEmpty(),
                    height      = buttonH,
                    fontSize    = (28f * scale).sp,
                    onClick = {
                        when {
                            email.isEmpty()      -> errorMessage = "Введите адрес электронной почты"
                            !isValidEmail(email) -> errorMessage = "Проверьте адрес электронной почты"
                            else                 -> { errorMessage = null; onEmailSubmit(email) }
                        }
                    }
                )
            }

            Spacer(Modifier.height(spacer))

            TvKeyboard(
                isRussian    = isRussian,
                isUpperCase  = isUpperCase,
                firstKeyFr   = firstKeyFr,
                keySize      = keySize,
                keyGap       = keyGap,
                blockGap     = blockGap,
                onChar       = { ch ->
                    email += if (isUpperCase) ch.uppercase() else ch.lowercase()
                    errorMessage = null
                },
                onDelete     = { if (email.isNotEmpty()) email = email.dropLast(1) },
                onClear      = { email = "" },
                onToggleLang = { isRussian = !isRussian },
                onToggleCase = { isUpperCase = !isUpperCase }
            )
        }

        // Close button — поверх всего
        AuthCloseButton(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = closePad, end = closePad),
            size    = closeSz,
            onClick = onClose
        )
    }
}

// ─── AuthEmailField ───────────────────────────────────────────────────────────

@Composable
private fun AuthEmailField(
    value:    String,
    hasError: Boolean,
    height:   Dp       = 96.dp,
    fontSize: TextUnit = 28.sp
) {
    val cursorAlpha by rememberInfiniteTransition(label = "cur").animateFloat(
        initialValue  = 1f,
        targetValue   = 0f,
        animationSpec = infiniteRepeatable(
            animation  = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "curA"
    )

    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .background(
                    if (hasError) Color(0xFF1F1010) else Color(0xFF343B4B).copy(alpha = 0.50f),
                    RoundedCornerShape(12.dp)
                )
                .border(
                    width = 1.5.dp,
                    color = if (hasError) Color(0xFFF08686) else Color.White.copy(alpha = 0.20f),
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 20.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            if (value.isEmpty()) {
                Text(
                    text  = "example@email.com",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = fontSize,
                        color    = Color(0xFFA8A9B2)
                    )
                )
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text  = value,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = fontSize,
                            color    = Color(0xFFE2E2E2)
                        )
                    )
                    Box(
                        modifier = Modifier
                            .width(2.dp)
                            .height(height * 0.4f)
                            .background(Primary.copy(alpha = cursorAlpha), RoundedCornerShape(1.dp))
                    )
                }
            }
        }

        if (hasError) {
            Text(
                text  = "Проверьте адрес электронной почты",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize   = (fontSize.value * 0.86f).sp,
                    fontWeight = FontWeight.Medium,
                    color      = Color(0xFFF08686)
                )
            )
        }
    }
}

// ─── AuthContinueButton ───────────────────────────────────────────────────────

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun AuthContinueButton(
    emailFilled: Boolean,
    height:      Dp       = 80.dp,
    fontSize:    TextUnit = 28.sp,
    onClick:     () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        if (isFocused) 1.08f else 1.0f, tween(150), label = "btn"
    )
    val gradientBrush = Brush.horizontalGradient(listOf(Color(0xFF4556EB), Color(0xFF6B78F0)))

    Surface(
        onClick  = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .onFocusChanged { isFocused = it.isFocused }
            .graphicsLayer(scaleX = scale, scaleY = scale),
        shape  = ClickableSurfaceDefaults.shape(RoundedCornerShape(20.dp)),
        colors = ClickableSurfaceDefaults.colors(
            containerColor        = if (emailFilled) Color.Transparent else Color(0xFF343B4B),
            focusedContainerColor = if (emailFilled) Color.Transparent else Color(0xFF3F4760)
        ),
        scale = ClickableSurfaceDefaults.scale(scale = 1.0f, focusedScale = 1.0f)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(if (emailFilled) Modifier.background(gradientBrush, RoundedCornerShape(20.dp)) else Modifier),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text  = "Продолжить",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize   = fontSize,
                    fontWeight = FontWeight.Medium,
                    color      = Color(0xFFE2E2E2)
                )
            )
        }
    }
}

// ─── AuthCloseButton ──────────────────────────────────────────────────────────

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun AuthCloseButton(
    modifier: Modifier = Modifier,
    size:     Dp       = 80.dp,
    onClick:  () -> Unit
) {
    Surface(
        onClick  = onClick,
        modifier = modifier.size(size),
        shape    = ClickableSurfaceDefaults.shape(RoundedCornerShape(200.dp)),
        colors   = ClickableSurfaceDefaults.colors(
            containerColor        = Color(0x33565A80),
            focusedContainerColor = Primary
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.08f)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Icon(
                imageVector        = ImageVector.vectorResource(R.drawable.ic_close),
                contentDescription = "Закрыть",
                tint               = Color.White,
                modifier           = Modifier.size(size * 0.25f)
            )
        }
    }
}

// ─── TvKeyboard ───────────────────────────────────────────────────────────────

@Composable
fun TvKeyboard(
    isRussian:    Boolean,
    isUpperCase:  Boolean         = true,
    firstKeyFr:   FocusRequester? = null,
    keySize:      Dp              = 70.dp,
    keyGap:       Dp              = 14.dp,
    blockGap:     Dp              = 32.dp,
    onChar:       (String) -> Unit,
    onDelete:     () -> Unit,
    onClear:      () -> Unit,
    onToggleLang: () -> Unit,
    onToggleCase: () -> Unit      = {},
    modifier:     Modifier        = Modifier
) {
    val enRows = listOf(
        listOf("Q","W","E","R","T","Y","U","I","O"),
        listOf("A","S","D","F","G","H","J","K","L"),
        listOf("Z","X","C","V","B","N","M","P",".")
    )
    val ruRows = listOf(
        listOf("Й","Ц","У","К","Е","Н","Г","Ш","Щ","З","Х"),
        listOf("Ф","Ы","В","А","П","Р","О","Л","Д","Ж","Э"),
        listOf("Я","Ч","С","М","И","Т","Ь","Б","Ю","Ъ","Ё")
    )
    val numRows  = listOf(listOf("1","2","3"), listOf("4","5","6"), listOf("7","8","9"))
    val rows     = if (isRussian) ruRows else enRows

    val fontSize   = (keySize.value * 36f / 70f).sp
    val fontSizeSm = (keySize.value * 26f / 70f).sp

    // Ширина пробела: letterBlock - (?!# + РУС + ↑ + ⌫ + Очистить + 4 gaps)
    val clearW   = keySize * 2.2f
    val letterW  = keySize * rows[0].size + keyGap * (rows[0].size - 1)
    // строка 4: ?!# + РУС + @ + shift + backspace + Очистить + 5 gaps = 5 кнопок + clearW
    val otherW   = keySize * 5 + keyGap * 5 + clearW
    val spaceW   = maxOf(letterW - otherW, keySize)
    val numBlockW = keySize * 3 + keyGap * 2

    Column(
        modifier            = modifier,
        verticalArrangement = Arrangement.spacedBy(keyGap)
    ) {
        // Строки 1–3
        rows.forEachIndexed { rowIdx, rowKeys ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Row(horizontalArrangement = Arrangement.spacedBy(keyGap)) {
                    rowKeys.forEachIndexed { ki, ch ->
                        KeyboardKey(
                            label      = if (isUpperCase) ch.uppercase() else ch.lowercase(),
                            size       = keySize,
                            fontSize   = fontSize,
                            onClick    = { onChar(ch) },
                            firstKeyFr = if (rowIdx == 0 && ki == 0) firstKeyFr else null
                        )
                    }
                }
                Spacer(Modifier.width(blockGap))
                Row(horizontalArrangement = Arrangement.spacedBy(keyGap)) {
                    numRows[rowIdx].forEach { d ->
                        KeyboardKey(label = d, size = keySize, fontSize = fontSize, onClick = { onChar(d) })
                    }
                }
            }
        }

        // Строка 4
        Row(verticalAlignment = Alignment.CenterVertically) {
            Row(horizontalArrangement = Arrangement.spacedBy(keyGap)) {
                KeyboardKey(label = "?!#", size = keySize, fontSize = fontSizeSm, onClick = { onChar("?") })
                KeyboardKey(label = if (isRussian) "ENG" else "РУС", size = keySize, fontSize = fontSizeSm, onClick = onToggleLang)
                KeyboardKey(label = "@", size = keySize, fontSize = fontSize, onClick = { onChar("@") })
                KeyboardKey(label = " ", size = keySize, width = spaceW, fontSize = fontSize, onClick = { onChar(" ") })
                KeyboardIconKey(
                    iconRes  = R.drawable.ic_keyboard_shift,
                    size     = keySize,
                    bgColor  = if (isUpperCase) Color(0xFF343B4B) else Color(0xFF4556EB),
                    onClick  = onToggleCase
                )
                KeyboardIconKey(
                    iconRes = R.drawable.ic_keyboard_backspace,
                    size    = keySize,
                    onClick = onDelete
                )
                KeyboardKey(label = "Очистить", size = keySize, width = clearW, fontSize = fontSizeSm, onClick = onClear)
            }
            Spacer(Modifier.width(blockGap))
            KeyboardKey(label = "0", size = keySize, width = numBlockW, fontSize = fontSize, onClick = { onChar("0") })
        }
    }
}

// ─── KeyboardIconKey ──────────────────────────────────────────────────────────

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun KeyboardIconKey(
    iconRes:    Int,
    size:       Dp    = 70.dp,
    bgColor:    Color = Color(0xFF343B4B),
    onClick:    () -> Unit
) {
    Surface(
        onClick  = onClick,
        modifier = Modifier.size(size),
        shape    = ClickableSurfaceDefaults.shape(RoundedCornerShape(12.dp)),
        colors   = ClickableSurfaceDefaults.colors(
            containerColor        = bgColor,
            focusedContainerColor = Primary
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.06f)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Icon(
                imageVector        = ImageVector.vectorResource(iconRes),
                contentDescription = null,
                tint               = Color(0xFFE2E2E2),
                modifier           = Modifier.size(size * 0.4f)
            )
        }
    }
}

// ─── KeyboardKey ─────────────────────────────────────────────────────────────

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun KeyboardKey(
    label:      String,
    size:       Dp              = 70.dp,
    width:      Dp              = size,
    fontSize:   TextUnit        = 36.sp,
    bgColor:    Color           = Color(0xFF343B4B),
    firstKeyFr: FocusRequester? = null,
    onClick:    () -> Unit
) {
    Surface(
        onClick  = onClick,
        modifier = Modifier
            .width(width)
            .height(size)
            .then(if (firstKeyFr != null) Modifier.focusRequester(firstKeyFr) else Modifier),
        shape  = ClickableSurfaceDefaults.shape(RoundedCornerShape(12.dp)),
        colors = ClickableSurfaceDefaults.colors(
            containerColor        = bgColor,
            focusedContainerColor = Primary
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.06f)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Text(
                text      = label,
                style     = MaterialTheme.typography.bodyLarge.copy(
                    fontSize   = fontSize,
                    fontWeight = FontWeight.SemiBold,
                    color      = Color(0xFFE2E2E2)
                ),
                textAlign = TextAlign.Center,
                maxLines  = 1
            )
        }
    }
}
