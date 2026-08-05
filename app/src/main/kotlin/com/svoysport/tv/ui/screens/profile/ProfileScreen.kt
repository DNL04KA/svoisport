package com.svoysport.tv.ui.screens.profile

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
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
import com.svoysport.tv.session.SessionManager
import com.svoysport.tv.ui.components.AppBackground
import com.svoysport.tv.ui.theme.Primary
import androidx.hilt.navigation.compose.hiltViewModel

private val PanelBg  = Color(0x33565A80)
private val KeyBg    = Color(0xFF343B4B)
private val TextMain = Color(0xFFE2E2E2)
private val TextSub  = Color(0xFFFFFFFF)
private val ExitRed  = Color(0xFFEE3232)

// ─── ProfileScreen ────────────────────────────────────────────────────────────
// Figma 538:16526 — контентная зона ~1760×1080 (сайдбар 160dp отдельно)
// Адаптация: все размеры масштабируются через BoxWithConstraints

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun ProfileScreen(
    onSubscriptionClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onDevicesClick: () -> Unit = {},
    onAboutClick: () -> Unit = {},
    onLogout: () -> Unit = {},
    onSidebarItem: (com.svoysport.tv.ui.components.nav.SidebarItem) -> Unit = {},
    modifier: Modifier = Modifier,
    devicesViewModel: DevicesViewModel = hiltViewModel()
) {
    val email by remember { derivedStateOf { SessionManager.userEmail.value } }
    val linkedDevices by devicesViewModel.devices.collectAsState()
    var sidebarExpanded by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    BackHandler(enabled = showLogoutDialog) { showLogoutDialog = false }
    val contentShift by animateDpAsState(
        targetValue = if (sidebarExpanded) 160.dp else 0.dp,
        animationSpec = tween(
            durationMillis = if (sidebarExpanded) 300 else 200,
            easing = if (sidebarExpanded) CubicBezierEasing(0f, 0f, 0.58f, 1f)
            else CubicBezierEasing(0.42f, 0f, 1f, 1f)
        ),
        label = "profileSidebarContentShift"
    )

    BoxWithConstraints(
        modifier = modifier.fillMaxSize().focusProperties { canFocus = !showLogoutDialog }
    ) {
        val sw = maxWidth.value
        val sh = maxHeight.value
        val scale = minOf(sw / 1920f, sh / 1080f, 1f).coerceAtLeast(0.35f)

        val itemW      : Dp       = (800f * scale).dp
        val wideItemH  : Dp       = (126f * scale).dp
        val tallItemH  : Dp       = (232f * scale).dp
        val tallItemW  : Dp       = (390f * scale).dp
        val avatarSz   : Dp       = (86f  * scale).dp
        val iconSz     : Dp       = (54f  * scale).dp
        val exitBtnH   : Dp       = (80f  * scale).dp
        val titleSp    : TextUnit = (36f  * scale).coerceAtLeast(14f).sp
        val subtitleSp : TextUnit = (20f  * scale).coerceAtLeast(12f).sp
        val itemGap    : Dp       = (20f  * scale).dp
        AppBackground()
        com.svoysport.tv.ui.components.nav.LeftSidebar(
            selectedItem   = null,
            onItemSelected = onSidebarItem,
            onExpandedChange = { sidebarExpanded = it },
            contentTopPadding = 0.dp,
            modifier       = Modifier.align(Alignment.TopStart)
        )
        Box(modifier = Modifier.fillMaxSize().offset(x = contentShift)) {
                // User info
                Column(
                    modifier = Modifier.offset(x = (847f * scale).dp, y = (100f * scale).dp).width((386f * scale).dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(itemGap)
                ) {
                    Box(
                        modifier = Modifier.size(avatarSz).background(PanelBg, RoundedCornerShape(20.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector        = ImageVector.vectorResource(R.drawable.ic_user),
                            contentDescription = null, tint = TextMain,
                            modifier           = Modifier.size(avatarSz * 0.49f)
                        )
                    }
                    Text(
                        text  = email,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = titleSp, fontWeight = FontWeight.SemiBold, color = TextMain
                        )
                    )
                }

                // Items
                Column(
                    modifier = Modifier.offset(x = (640f * scale).dp, y = (308f * scale).dp).width(itemW),
                    verticalArrangement = Arrangement.spacedBy(itemGap)
                ) {
                    // Состояние подписки — из QR-активации (SubscriptionManager)
                    val subActive = com.svoysport.tv.session.SubscriptionManager.isSubscribed.value
                    val subUntil  = com.svoysport.tv.session.SubscriptionManager.subscribedUntil.value
                    WideProfileItem(
                        title = "Подписка",
                        subtitle = if (subActive) "Активна до ${formatSubscriptionDate(subUntil)}" else "Не оформлена",
                        icon = R.drawable.ic_card, height = wideItemH, iconSz = iconSz,
                        titleSp = titleSp, subtitleSp = subtitleSp, onClick = onSubscriptionClick
                    )
                    Row(
                        modifier              = Modifier.width(itemW),
                        horizontalArrangement = Arrangement.spacedBy(itemGap)
                    ) {
                        TallProfileItem(
                            title = "Настройки", subtitle = "Язык и качество видео",
                            icon = R.drawable.ic_settings, iconSz = iconSz,
                            titleSp = titleSp, subtitleSp = subtitleSp,
                            scale = scale,
                            modifier = Modifier.width(tallItemW).height(tallItemH),
                            onClick  = onSettingsClick
                        )
                        TallProfileItem(
                            title = "Мои устройства", subtitle = "${linkedDevices.size} из 3 подключено",
                            icon = R.drawable.ic_monitor, iconSz = iconSz,
                            titleSp = titleSp, subtitleSp = subtitleSp,
                            scale = scale,
                            modifier = Modifier.width(tallItemW).height(tallItemH),
                            onClick  = onDevicesClick
                        )
                    }
                    WideProfileItem(
                        title = "О приложении", subtitle = "Версия 1.2.3",
                        icon = R.drawable.ic_info, height = wideItemH, iconSz = iconSz,
                        titleSp = titleSp, subtitleSp = subtitleSp, onClick = onAboutClick
                    )
                }

                ExitButton(
                    modifier = Modifier.offset(x = (640f * scale).dp, y = (940f * scale).dp).width(itemW).height(exitBtnH),
                    fontSize = (28f * scale).coerceAtLeast(12f).sp,
                    onClick  = { showLogoutDialog = true }
                )
        }

        if (showLogoutDialog) {
            ProfileLogoutDialog(
                scale = scale,
                onCancel = { showLogoutDialog = false },
                onConfirm = {
                    showLogoutDialog = false
                    devicesViewModel.disconnectCurrent {
                        SessionManager.isLoggedIn.value = false
                        com.svoysport.tv.session.SubscriptionManager.clear()
                        onLogout()
                    }
                }
            )
        }

    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun ProfileLogoutDialog(
    scale: Float,
    onCancel: () -> Unit,
    onConfirm: () -> Unit
) {
    val cancelFocusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { cancelFocusRequester.requestFocus() }

    Dialog(
        onDismissRequest = onCancel,
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
            Column(
                modifier = Modifier.width((760f * scale).dp)
                    .background(Color(0xFF1E1F20), RoundedCornerShape(28.dp))
                    .padding((44f * scale).dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Выйти из аккаунта?",
                    color = TextMain,
                    fontSize = (42f * scale).coerceAtLeast(20f).sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height((16f * scale).dp))
                Text(
                    "Для повторного входа потребуется снова активировать телевизор.",
                    color = TextMain.copy(alpha = 0.68f),
                    fontSize = (22f * scale).coerceAtLeast(14f).sp
                )
                Spacer(Modifier.height((36f * scale).dp))
                Row(horizontalArrangement = Arrangement.spacedBy((20f * scale).dp)) {
                    Button(
                        onClick = onCancel,
                        modifier = Modifier.focusRequester(cancelFocusRequester),
                        colors = ButtonDefaults.colors(
                            containerColor = Color(0xFF343B4B),
                            focusedContainerColor = Primary
                        )
                    ) { Text("Отмена", fontSize = (20f * scale).coerceAtLeast(14f).sp) }
                    Button(
                        onClick = onConfirm,
                        colors = ButtonDefaults.colors(
                            containerColor = Color(0xFF343B4B),
                            focusedContainerColor = ExitRed
                        )
                    ) { Text("Выйти", fontSize = (20f * scale).coerceAtLeast(14f).sp) }
                }
            }
        }
    }
}

private fun formatSubscriptionDate(value: String?): String {
    val date = value?.take(10)?.split('-') ?: return "—"
    return if (date.size == 3) "${date[2]}.${date[1]}.${date[0]}" else value
}

// ─── WideProfileItem ──────────────────────────────────────────────────────────

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun WideProfileItem(
    title     : String,
    subtitle  : String,
    icon      : Int,
    height    : Dp       = 126.dp,
    iconSz    : Dp       = 54.dp,
    titleSp   : TextUnit = 36.sp,
    subtitleSp: TextUnit = 20.sp,
    onClick   : () -> Unit,
    modifier  : Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }
    val sc by animateFloatAsState(if (isFocused) 1.08f else 1.0f, tween(150), label = "wi")
    Surface(
        onClick  = onClick,
        modifier = modifier.fillMaxWidth().height(height).onFocusChanged { isFocused = it.isFocused }.scale(sc),
        shape  = ClickableSurfaceDefaults.shape(RoundedCornerShape(20.dp)),
        colors = ClickableSurfaceDefaults.colors(containerColor = PanelBg, focusedContainerColor = Primary),
        scale  = ClickableSurfaceDefaults.scale(scale = 1.0f, focusedScale = 1.0f)
    ) {
        Row(
            modifier              = Modifier.fillMaxSize().padding(horizontal = 36.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text = title, style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = titleSp, fontWeight = FontWeight.SemiBold, color = TextMain))
                Text(text = subtitle, style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = subtitleSp, fontWeight = FontWeight.Medium, color = TextSub.copy(alpha = 0.70f)))
            }
            Icon(imageVector = ImageVector.vectorResource(icon), contentDescription = null,
                tint = TextMain, modifier = Modifier.size(iconSz))
        }
    }
}

// ─── TallProfileItem ─────────────────────────────────────────────────────────

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun TallProfileItem(
    title     : String,
    subtitle  : String,
    icon      : Int,
    iconSz    : Dp       = 54.dp,
    titleSp   : TextUnit = 36.sp,
    subtitleSp: TextUnit = 20.sp,
    scale     : Float = 1f,
    onClick   : () -> Unit,
    modifier  : Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }
    val sc by animateFloatAsState(if (isFocused) 1.08f else 1.0f, tween(150), label = "ti")
    Surface(
        onClick  = onClick,
        modifier = modifier.onFocusChanged { isFocused = it.isFocused }.scale(sc),
        shape  = ClickableSurfaceDefaults.shape(RoundedCornerShape(20.dp)),
        colors = ClickableSurfaceDefaults.colors(containerColor = PanelBg, focusedContainerColor = Primary),
        scale  = ClickableSurfaceDefaults.scale(scale = 1.0f, focusedScale = 1.0f)
    ) {
        Column(
            modifier            = Modifier.fillMaxSize().padding((24f * scale).dp),
            verticalArrangement = Arrangement.spacedBy((52f * scale).dp)
        ) {
            Icon(imageVector = ImageVector.vectorResource(icon), contentDescription = null,
                tint = TextMain, modifier = Modifier.size(iconSz))
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text = title, style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = titleSp, fontWeight = FontWeight.SemiBold, color = TextMain))
                Text(text = subtitle, style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = subtitleSp, fontWeight = FontWeight.Medium, color = TextSub.copy(alpha = 0.70f)))
            }
        }
    }
}

// ─── ExitButton ───────────────────────────────────────────────────────────────

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun ExitButton(
    onClick : () -> Unit,
    fontSize: TextUnit = 28.sp,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }
    val sc by animateFloatAsState(if (isFocused) 1.08f else 1.0f, tween(150), label = "exit")
    Surface(
        onClick  = onClick,
        modifier = modifier.onFocusChanged { isFocused = it.isFocused }.scale(sc),
        shape  = ClickableSurfaceDefaults.shape(RoundedCornerShape(20.dp)),
        colors = ClickableSurfaceDefaults.colors(containerColor = KeyBg, focusedContainerColor = KeyBg),
        scale  = ClickableSurfaceDefaults.scale(scale = 1.0f, focusedScale = 1.0f)
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = "Выйти", style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = fontSize, fontWeight = FontWeight.Medium, color = ExitRed.copy(alpha = 0.80f)))
        }
    }
}
