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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.tv.material3.*
import com.svoysport.tv.R
import com.svoysport.tv.ui.components.AppBackground
import com.svoysport.tv.ui.theme.Primary
import com.svoysport.tv.ui.theme.PrimaryPressed
import com.svoysport.tv.session.SettingsManager

// ─── SettingsScreen ───────────────────────────────────────────────────────────
// Figma 584:20564 — 1920×1080 full-screen (без сайдбара)
// Было: offset(200,215), offset(976,357) → теперь Row + Column с padding

private val _SettingsPanelBg = Color(0x33565A80)
private val _SettingsKeyBg   = Color(0xFF343B4B)
private val _SettingsTextMain = Color(0xFFE2E2E2)
private val _SettingsPrimaryGrad = Brush.horizontalGradient(listOf(Color(0xFF4556EB), Color(0xFF273085)))

private val langOptions    = listOf("Русский", "English")
private val qualityOptions = listOf("Авто", "1080", "720", "360")

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit = {}, modifier: Modifier = Modifier) {
    var savedLang     by remember { mutableStateOf(SettingsManager.language()) }
    var savedQuality  by remember { mutableStateOf(SettingsManager.quality()) }
    var currentLang    by remember { mutableStateOf(savedLang) }
    var currentQuality by remember { mutableStateOf(savedQuality) }
    val hasChanges = currentLang != savedLang || currentQuality != savedQuality
    var openMenu by remember { mutableIntStateOf(-1) }
    val menuFocusRequester = remember { FocusRequester() }

    LaunchedEffect(openMenu) {
        if (openMenu >= 0) menuFocusRequester.requestFocus()
    }

    BackHandler { if (openMenu >= 0) openMenu = -1 else onBack() }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        AppBackground()
        val sw = maxWidth.value
        val sh = maxHeight.value
        val scale = minOf(sw / 1920f, sh / 1080f, 1f).coerceAtLeast(0.35f)

        val pad        : Dp       = (60f  * scale).dp
        val closeSz    : Dp       = (80f  * scale).dp
        val titleSp    : TextUnit = (54f  * scale).coerceAtLeast(18f).sp
        val rowH       : Dp       = (102f * scale).dp
        val rowW       : Dp       = (753f * scale).dp
        val rowTextSp  : TextUnit = (36f  * scale).coerceAtLeast(14f).sp
        val chevronSz  : Dp       = (54f  * scale).dp
        val saveH      : Dp       = (80f  * scale).dp
        val saveFontSp : TextUnit = (28f  * scale).coerceAtLeast(12f).sp
        val rowGap     : Dp       = (40f  * scale).dp
        val dropW      : Dp       = (432f * scale).dp
        val dropItemH  : Dp       = (80f  * scale).dp
        val dropTextSp : TextUnit = (28f  * scale).coerceAtLeast(12f).sp

        // Main layout
        Column(
            modifier = Modifier.fillMaxSize()
                .focusProperties { canFocus = openMenu < 0 }
                .padding(horizontal = pad, vertical = pad)
        ) {
            // ── Back button + Title row ──────────────────────────────────────
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy((20f * scale).dp)) {
                var backFocused by remember { mutableStateOf(false) }
                val backSc by animateFloatAsState(if (backFocused) 1.08f else 1f, tween(150), label = "back")
                Surface(
                    onClick   = onBack,
                    modifier  = Modifier.size(closeSz).onFocusChanged { backFocused = it.isFocused }.scale(backSc),
                    shape     = ClickableSurfaceDefaults.shape(RoundedCornerShape(200.dp)),
                    colors    = ClickableSurfaceDefaults.colors(
                        containerColor        = _SettingsPanelBg,
                        focusedContainerColor = Primary,
                        pressedContainerColor = PrimaryPressed
                    ),
                    scale     = ClickableSurfaceDefaults.scale(scale = 1f, focusedScale = 1f)
                ) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Icon(imageVector = ImageVector.vectorResource(R.drawable.ic_arrow_left),
                                contentDescription = "Назад",
                                tint = if (backFocused) Color.White else _SettingsTextMain,
                                modifier = Modifier.size((24f * scale).dp))
                        }
                }
                Text(text = "Настройки", style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = titleSp, fontWeight = FontWeight.Medium, color = _SettingsTextMain))
            }

            Spacer(Modifier.height((90f * scale).dp))

            // ── Settings rows ────────────────────────────────────────────────
            Column(verticalArrangement = Arrangement.spacedBy(rowGap)) {
                SettingsRow(
                    label = "Язык", selectedValue = currentLang, isOpen = openMenu == 0,
                    width = rowW, height = rowH, textSp = rowTextSp, chevronSz = chevronSz,
                    onOpen = { openMenu = if (openMenu == 0) -1 else 0 }
                )
                SettingsRow(
                    label = "Разрешение видео", selectedValue = currentQuality, isOpen = openMenu == 1,
                    width = rowW, height = rowH, textSp = rowTextSp, chevronSz = chevronSz,
                    onOpen = { openMenu = if (openMenu == 1) -1 else 1 }
                )
            }

            Spacer(Modifier.height((40f * scale).dp))

            // ── Save button ──────────────────────────────────────────────────
            var saveFocused by remember { mutableStateOf(false) }
            val saveSc by animateFloatAsState(if (saveFocused && hasChanges) 1.08f else 1f, tween(150), label = "save")
            Box(
                modifier = Modifier.width(rowW).height(saveH)
                    .onFocusChanged { saveFocused = it.isFocused }.scale(saveSc)
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (hasChanges) _SettingsPrimaryGrad else Brush.horizontalGradient(listOf(_SettingsKeyBg, _SettingsKeyBg)))
            ) {
                Surface(
                    onClick  = { if (hasChanges) { SettingsManager.save(currentLang, currentQuality); savedLang = currentLang; savedQuality = currentQuality } },
                    modifier = Modifier.fillMaxSize(),
                    shape    = ClickableSurfaceDefaults.shape(RoundedCornerShape(20.dp)),
                    colors   = ClickableSurfaceDefaults.colors(containerColor = Color.Transparent, focusedContainerColor = Color.Transparent),
                    scale    = ClickableSurfaceDefaults.scale(scale = 1f, focusedScale = 1f)
                ) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = "Сохранить", style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = saveFontSp, fontWeight = FontWeight.Medium,
                            color = _SettingsTextMain.copy(alpha = if (hasChanges) 1f else 0.45f)))
                    }
                }
            }
        }

        // ── Dropdown overlay ─────────────────────────────────────────────────
        // Позиционируем справа от строк настроек — как в Figma (x=976)
        if (openMenu >= 0) {
            val options  = if (openMenu == 0) langOptions else qualityOptions
            val selected = if (openMenu == 0) currentLang else currentQuality

            Box(
                modifier = Modifier
                    .padding(
                        start = pad + (753f * scale).dp + (20f * scale).dp,
                        top   = pad + closeSz + (90f * scale).dp + (rowH + rowGap) * openMenu
                    )
                    .width(dropW)
                    .wrapContentHeight()
                    .clip(RoundedCornerShape(20.dp))
                    .background(_SettingsPanelBg)
                    .zIndex(10f)
            ) {
                Column(modifier = Modifier.padding(horizontal = (24f * scale).dp, vertical = (16f * scale).dp)) {
                    options.forEachIndexed { index, option ->
                        SettingsDropdownItem(
                            label      = option,
                            isSelected = option == selected,
                            height     = dropItemH,
                            textSp     = dropTextSp,
                            modifier   = if (index == 0) Modifier.focusRequester(menuFocusRequester) else Modifier,
                            onClick    = {
                                if (openMenu == 0) currentLang = option else currentQuality = option
                                openMenu = -1
                            }
                        )
                    }
                }
            }
        }
    }
}

// ─── SettingsRow ─────────────────────────────────────────────────────────────

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun SettingsRow(
    label        : String,
    selectedValue: String,
    isOpen       : Boolean,
    width        : Dp       = 753.dp,
    height       : Dp       = 102.dp,
    textSp       : TextUnit = 36.sp,
    chevronSz    : Dp       = 54.dp,
    onOpen       : () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    val sc by animateFloatAsState(if (isFocused) 1.08f else 1f, tween(150), label = "rowSc")
    Surface(
        onClick  = onOpen,
        modifier = Modifier.width(width).height(height).onFocusChanged { isFocused = it.isFocused }.scale(sc),
        shape  = ClickableSurfaceDefaults.shape(RoundedCornerShape(20.dp)),
        colors = ClickableSurfaceDefaults.colors(containerColor = _SettingsPanelBg, focusedContainerColor = _SettingsPanelBg),
        border = ClickableSurfaceDefaults.border(
            focusedBorder = Border(border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF4556EB)),
                shape = RoundedCornerShape(20.dp))
        ),
        scale = ClickableSurfaceDefaults.scale(scale = 1f, focusedScale = 1f)
    ) {
        Row(
            modifier              = Modifier.fillMaxSize().padding(horizontal = 32.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = textSp, fontWeight = FontWeight.SemiBold, color = _SettingsTextMain))
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text = selectedValue, style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = textSp, fontWeight = FontWeight.SemiBold, color = _SettingsTextMain))
                Icon(
                    imageVector        = ImageVector.vectorResource(
                        if (isOpen) R.drawable.ic_chevron_down else R.drawable.ic_chevron_right),
                    contentDescription = null, tint = _SettingsTextMain,
                    modifier           = Modifier.size(chevronSz)
                )
            }
        }
    }
}

// ─── SettingsDropdownItem ─────────────────────────────────────────────────────

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun SettingsDropdownItem(
    label     : String,
    isSelected: Boolean,
    height    : Dp       = 80.dp,
    textSp    : TextUnit = 28.sp,
    modifier  : Modifier = Modifier,
    onClick   : () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    val sc by animateFloatAsState(if (isFocused) 1.08f else 1f, tween(150), label = "drop")
    val bgBrush: Brush = if (isSelected || isFocused) _SettingsPrimaryGrad
        else Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent))
    Box(
        modifier = modifier.fillMaxWidth().height(height)
            .onFocusChanged { isFocused = it.isFocused }.scale(sc)
            .clip(RoundedCornerShape(16.dp)).background(bgBrush)
    ) {
        Surface(
            onClick  = onClick,
            modifier = Modifier.fillMaxSize(),
            shape    = ClickableSurfaceDefaults.shape(RoundedCornerShape(16.dp)),
            colors   = ClickableSurfaceDefaults.colors(containerColor = Color.Transparent, focusedContainerColor = Color.Transparent),
            scale    = ClickableSurfaceDefaults.scale(scale = 1f, focusedScale = 1f)
        ) {
            Box(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp), contentAlignment = Alignment.CenterStart) {
                Text(text = label, style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = textSp, fontWeight = FontWeight.Medium, color = _SettingsTextMain))
            }
        }
    }
}
