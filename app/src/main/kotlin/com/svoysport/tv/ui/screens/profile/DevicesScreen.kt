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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.tv.material3.*
import com.svoysport.tv.R
import com.svoysport.tv.ui.components.AppBackground
import com.svoysport.tv.ui.theme.Primary
import com.svoysport.tv.ui.theme.PrimaryPressed
import androidx.hilt.navigation.compose.hiltViewModel

private val _PanelBg  = Color(0x33565A80)
private val _KeyBg    = Color(0xFF343B4B)
private val _TextMain = Color(0xFFE2E2E2)
private val _Gray3    = Color(0xFFA8A9B2)
private val _ExitRed  = Color(0xFFEE3232)
private val _PrimaryGrad = Brush.horizontalGradient(listOf(Color(0xFF4556EB), Color(0xFF273085)))

data class DeviceItem(val id: String, val name: String, val lastLogin: String, val isCurrent: Boolean = false)

internal fun shouldShowExitAllDevices(deviceCount: Int): Boolean = deviceCount >= 3

// ─── DevicesScreen ────────────────────────────────────────────────────────────
// Figma 578:18689 — 1920×1080 full-screen (без сайдбара)
// Было: offset(965,151) и т.д. → теперь Row с padding

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun DevicesScreen(
    onBack: () -> Unit = {},
    onLogout: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: DevicesViewModel = hiltViewModel()
) {
    val devices by viewModel.devices.collectAsState()
    var dialogState by remember { mutableStateOf<String?>(null) }

    BackHandler { if (dialogState != null) dialogState = null else onBack() }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        AppBackground()
        val sw = maxWidth.value
        val sh = maxHeight.value
        val scale = minOf(sw / 1920f, sh / 1080f, 1f).coerceAtLeast(0.35f)

        val pad       : Dp       = (60f  * scale).dp
        val closeSz   : Dp       = (80f  * scale).dp
        val iconSz    : Dp       = (24f  * scale).dp
        val titleSp   : TextUnit = (54f  * scale).coerceAtLeast(18f).sp
        val textSp    : TextUnit = (26f  * scale).coerceAtLeast(12f).sp
        val deviceRowH: Dp       = (126f * scale).dp
        val exitBtnH  : Dp       = (80f  * scale).dp
        val exitBtnW  : Dp       = (500f * scale).dp
        val nameSp    : TextUnit = (36f  * scale).coerceAtLeast(14f).sp
        val subtitleSp: TextUnit = (20f  * scale).coerceAtLeast(11f).sp
        val rowGap    : Dp       = (40f  * scale).dp

        Box(modifier = Modifier.fillMaxSize().focusProperties { canFocus = dialogState == null }) {
            // ── Back button ──────────────────────────────────────────────────
            var backFocused by remember { mutableStateOf(false) }
            val backSc by animateFloatAsState(if (backFocused) 1.08f else 1f, tween(150), label = "back")
            Surface(
                onClick   = onBack,
                modifier  = Modifier.offset(x = pad, y = pad).size(closeSz)
                    .onFocusChanged { backFocused = it.isFocused }.scale(backSc),
                shape     = ClickableSurfaceDefaults.shape(RoundedCornerShape(200.dp)),
                colors    = ClickableSurfaceDefaults.colors(
                    containerColor        = _PanelBg,
                    focusedContainerColor = Primary,
                    pressedContainerColor = PrimaryPressed
                ),
                scale     = ClickableSurfaceDefaults.scale(scale = 1f, focusedScale = 1f)
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Icon(imageVector = ImageVector.vectorResource(R.drawable.ic_arrow_left),
                        contentDescription = "Назад",
                        tint = if (backFocused) Color.White else _TextMain,
                        modifier = Modifier.size(iconSz))
                }
            }

            Text(
                text  = "Мои устройства",
                modifier = Modifier.offset(x = (200f * scale).dp, y = (70f * scale).dp),
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = titleSp, fontWeight = FontWeight.Medium, color = _TextMain
                )
            )
            Column(
                modifier = Modifier.offset(x = (200f * scale).dp, y = (160f * scale).dp)
                    .width((625f * scale).dp),
                verticalArrangement = Arrangement.spacedBy((42f * scale).dp)
            ) {
                    Text(
                        text  = "Здесь отображаются все устройства, где\nвыполнен вход в аккаунт.\nОдновременно можно использовать до трёх\nустройств.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = textSp, fontWeight = FontWeight.Medium, color = _Gray3
                        )
                    )
                    Text(
                        text = "При необходимости из любого устройства\nможно выйти",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = textSp, fontWeight = FontWeight.Medium, color = _Gray3
                        )
                    )
            }
            Column(
                modifier = Modifier.offset(x = (960f * scale).dp, y = (147f * scale).dp)
                    .width((805f * scale).dp),
                verticalArrangement = Arrangement.spacedBy(rowGap)
            ) {
                    devices.forEach { device ->
                        DeviceRow(
                            device       = device,
                            height       = deviceRowH,
                            nameSp       = nameSp,
                            subtitleSp   = subtitleSp,
                            onDisconnect = { dialogState = "disconnect:${device.id}" }
                        )
                    }
            }

            // ── Exit all button — bottom center ──────────────────────────────
            if (shouldShowExitAllDevices(devices.size)) {
                Box(modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = pad)) {
                var exitFocused by remember { mutableStateOf(false) }
                val exitSc by animateFloatAsState(if (exitFocused) 1.08f else 1f, tween(150), label = "exitAll")
                Surface(
                    onClick  = { dialogState = "exit_all" },
                    modifier = Modifier.width(exitBtnW).height(exitBtnH)
                        .onFocusChanged { exitFocused = it.isFocused }.scale(exitSc),
                    shape  = ClickableSurfaceDefaults.shape(RoundedCornerShape(20.dp)),
                    colors = ClickableSurfaceDefaults.colors(containerColor = _KeyBg, focusedContainerColor = _KeyBg),
                    scale  = ClickableSurfaceDefaults.scale(scale = 1f, focusedScale = 1f)
                ) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = "Выйти на всех устройствах",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = (28f * scale).sp, fontWeight = FontWeight.Medium, color = _ExitRed))
                    }
                }
            }
        }

        // ── Dialogs ──────────────────────────────────────────────────────────
        if (dialogState != null) {
            val isExitAll    = dialogState == "exit_all"
            val targetDevice = if (!isExitAll) {
                val id = dialogState!!.removePrefix("disconnect:")
                devices.find { it.id == id }
            } else null

            val dlgW     : Dp       = (1056f * scale).dp
            val dlgTitleSp: TextUnit = (54f * scale).coerceAtLeast(18f).sp
            val dlgTextSp : TextUnit = (32f * scale).coerceAtLeast(14f).sp
            val dlgBtnH  : Dp       = (80f  * scale).dp
            val cancelFocusRequester = remember { FocusRequester() }
            val confirmFocusRequester = remember { FocusRequester() }

            LaunchedEffect(dialogState) {
                cancelFocusRequester.requestFocus()
            }

            Dialog(
                onDismissRequest = { dialogState = null },
                properties = DialogProperties(
                    dismissOnBackPress = true,
                    dismissOnClickOutside = false,
                    usePlatformDefaultWidth = false
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.82f)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .width(dlgW).wrapContentHeight()
                            .clip(RoundedCornerShape(32.dp))
                            .background(Color(0xFF1E1F20))
                            .padding(horizontal = (48f * scale).dp, vertical = (48f * scale).dp)
                    ) {
                        Column(
                            modifier            = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy((32f * scale).dp)
                        ) {
                            Text(
                                text  = if (isExitAll) "Выйти из аккаунта на всех устройствах?" else "Отключить устройство?",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontSize = dlgTitleSp, fontWeight = FontWeight.Medium, color = _TextMain)
                            )
                            Text(
                                text  = if (isExitAll)
                                    "Выход будет выполнен на всех устройствах, включая этот телевизор.\nЧтобы использовать аккаунт снова, потребуется повторная авторизация."
                                else
                                    "Вы уверены, что хотите выйти из аккаунта на устройстве ${targetDevice?.name ?: ""}?\nЕсли оно сейчас используется, просмотр прекратится.",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = dlgTextSp, fontWeight = FontWeight.Normal, color = Color(0xFFC4C7C5))
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy((24f * scale).dp)) {
                                DevDialogButton(
                                    label = "Отмена",
                                    width = (171f * scale).dp,
                                    height = dlgBtnH,
                                    useGrad = true,
                                    fontSize = (28f * scale).sp,
                                    focusRequester = cancelFocusRequester,
                                    rightFocusRequester = confirmFocusRequester,
                                    onClick = { dialogState = null }
                                )
                                DevDialogButton(
                                    label   = if (isExitAll) "Выйти на всех" else "Отключить",
                                    width   = if (isExitAll) (263f * scale).dp else (219f * scale).dp,
                                    height  = dlgBtnH, useGrad = false,
                                    fontSize = (28f * scale).sp,
                                    focusRequester = confirmFocusRequester,
                                    leftFocusRequester = cancelFocusRequester,
                                    onClick  = {
                                        if (isExitAll) {
                                            viewModel.disconnectAll {
                                                com.svoysport.tv.session.SubscriptionManager.clear()
                                                onLogout()
                                            }
                                        } else {
                                            viewModel.disconnect(target = dialogState!!.removePrefix("disconnect:"))
                                        }
                                        dialogState = null
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
}

// ─── DeviceRow ────────────────────────────────────────────────────────────────

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun DeviceRow(
    device      : DeviceItem,
    height      : Dp       = 126.dp,
    nameSp      : TextUnit = 36.sp,
    subtitleSp  : TextUnit = 20.sp,
    onDisconnect: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    val sc by animateFloatAsState(if (isFocused) 1.08f else 1f, tween(150), label = "dr")

    Surface(
        onClick  = { if (!device.isCurrent) onDisconnect() },
        modifier = Modifier.fillMaxWidth().height(height)
            .onFocusChanged { isFocused = it.isFocused }.scale(sc),
        shape  = ClickableSurfaceDefaults.shape(RoundedCornerShape(20.dp)),
        colors = ClickableSurfaceDefaults.colors(containerColor = _PanelBg, focusedContainerColor = _PanelBg),
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
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(text = device.name, style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = nameSp, fontWeight = FontWeight.SemiBold, color = _TextMain))
                    if (device.isCurrent) {
                        Text(text = "Этот телевизор", style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = subtitleSp, fontWeight = FontWeight.Medium, color = Color.White))
                    } else {
                        Text(text = device.lastLogin, style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = subtitleSp, fontWeight = FontWeight.Medium, color = _Gray3))
                    }
            }
            val indicatorSz = 42.dp
            Box(
                modifier = Modifier.size(indicatorSz).clip(RoundedCornerShape(indicatorSz / 2))
                    .background(Color(0x66565A80)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(indicatorSz * 0.48f)
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(_TextMain.copy(alpha = 0.82f))
                )
            }
        }
    }
}

// ─── DevDialogButton ─────────────────────────────────────────────────────────

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun DevDialogButton(
    label  : String,
    width  : Dp,
    height : Dp       = 80.dp,
    useGrad: Boolean,
    fontSize: TextUnit = 28.sp,
    focusRequester: FocusRequester,
    leftFocusRequester: FocusRequester? = null,
    rightFocusRequester: FocusRequester? = null,
    onClick: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    val sc by animateFloatAsState(if (isFocused) 1.08f else 1f, tween(150), label = "dlgBtn")
    Box(
        modifier = Modifier.width(width).height(height)
            .onFocusChanged { isFocused = it.isFocused }.scale(sc)
            .clip(RoundedCornerShape(20.dp))
            .background(if (useGrad) _PrimaryGrad else Brush.horizontalGradient(listOf(_KeyBg, _KeyBg)))
    ) {
        Surface(
            onClick  = onClick,
            modifier = Modifier.fillMaxSize()
                .focusRequester(focusRequester)
                .focusProperties {
                    leftFocusRequester?.let { left = it }
                    rightFocusRequester?.let { right = it }
                },
            shape    = ClickableSurfaceDefaults.shape(RoundedCornerShape(20.dp)),
            colors   = ClickableSurfaceDefaults.colors(
                containerColor = Color.Transparent, focusedContainerColor = Color.Transparent),
            scale = ClickableSurfaceDefaults.scale(scale = 1f, focusedScale = 1f)
        ) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = label, style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = fontSize, fontWeight = FontWeight.Medium, color = _TextMain))
            }
        }
    }
}
