package com.svoysport.tv.ui.screens.profile

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.*
import com.svoysport.tv.R
import com.svoysport.tv.session.SessionManager
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

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
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
        Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0F0F10))) {
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
                    onClick  = {
                        devicesViewModel.disconnectCurrent {
                            SessionManager.isLoggedIn.value = false
                            com.svoysport.tv.session.SubscriptionManager.clear()
                            onLogout()
                        }
                    }
                )
        }

        // Шапка как на главной: лого по центру рельсы сайдбара (60dp)
        androidx.compose.foundation.Image(
            painter            = androidx.compose.ui.res.painterResource(R.drawable.logo_icon),
            contentDescription = "Свой Спорт",
            modifier           = Modifier
                .align(Alignment.TopStart)
                .padding(start = (40f * scale).dp, top = (30f * scale).dp)
                .size((80f * scale).dp)
        )

        // Боковое меню поверх контента — на профиле оно тоже есть (Figma)
        com.svoysport.tv.ui.components.nav.LeftSidebar(
            selectedItem   = null,
            onItemSelected = onSidebarItem,
            modifier       = Modifier.align(Alignment.TopStart).padding(top = (160f * scale).dp)
        )
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
