package com.svoysport.tv.ui.components.nav

import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.*
import com.svoysport.tv.R
import com.svoysport.tv.ui.theme.Blue100
import com.svoysport.tv.ui.theme.Primary
import com.svoysport.tv.ui.theme.PrimaryPressed
import com.svoysport.tv.ui.theme.SurfaceVariant
import com.svoysport.tv.ui.theme.White20

enum class NavTab { HOME, SCHEDULE, ARCHIVE }

// Активный таб — тёмный непрозрачный чип (как на Figma: #1E2030)
private val ActiveTabBg = Color(0xFF1E2235)

internal fun topNavigationLeadingSpaceDp(sidebarExpanded: Boolean): Float = 36f

internal fun topTabContainerColor(selected: Boolean, focused: Boolean): Color = when {
    focused -> Primary
    selected -> Color(0xFF565B68)
    else -> Color.Transparent
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun TopNavigationBar(
    selectedTab: NavTab,
    onTabSelected: (NavTab) -> Unit,
    isLoggedIn: Boolean = false,
    onAuthClick: () -> Unit = {},
    logoExpanded: Boolean = false,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(start = 12.dp, end = 24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.width(topNavigationLeadingSpaceDp(logoExpanded).dp))

        Spacer(modifier = Modifier.weight(1f))

        // Центральные табы — на полупрозрачной полосе-подложке (Figma)
        Row(
            modifier = Modifier
                .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(14.dp))
                .padding(3.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment     = Alignment.CenterVertically
        ) {
            NavTabItem(
                title    = "Главная",
                selected = selectedTab == NavTab.HOME,
                onClick  = { onTabSelected(NavTab.HOME) }
            )
            NavTabItem(
                title    = "Расписание",
                selected = selectedTab == NavTab.SCHEDULE,
                onClick  = { onTabSelected(NavTab.SCHEDULE) }
            )
            NavTabItem(
                title    = "Архив",
                selected = selectedTab == NavTab.ARCHIVE,
                onClick  = { onTabSelected(NavTab.ARCHIVE) }
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Кнопка авторизации / аватар
        if (isLoggedIn) {
            var isFocused by remember { mutableStateOf(false) }
            Surface(
                onClick  = onAuthClick,
                modifier = Modifier
                    .size(36.dp)
                    .onFocusChanged { isFocused = it.isFocused },
                shape  = ClickableSurfaceDefaults.shape(RoundedCornerShape(18.dp)),
                colors = ClickableSurfaceDefaults.colors(
                    containerColor        = White20,
                    focusedContainerColor = Primary
                ),
                scale = ClickableSurfaceDefaults.scale(focusedScale = 1.08f)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        imageVector        = ImageVector.vectorResource(R.drawable.ic_user),
                        contentDescription = "Профиль",
                        tint               = Color.White,
                        modifier           = Modifier.size(18.dp)
                    )
                }
            }
        } else {
            AuthButton(onClick = onAuthClick)
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun NavTabItem(
    title: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    val isHighlighted = selected || isFocused

    Surface(
        onClick  = onClick,
        modifier = Modifier.onFocusChanged { isFocused = it.isFocused },
        shape    = ClickableSurfaceDefaults.shape(RoundedCornerShape(12.dp)),
        // Figma BUTTONS/Tabs: selected (не в фокусе) — серый чип «мы на этой
        // странице», фокус — accent (синий), нажатие — тёмный accent
        colors   = ClickableSurfaceDefaults.colors(
            containerColor        = topTabContainerColor(selected, false),
            focusedContainerColor = topTabContainerColor(selected, true),
            pressedContainerColor = PrimaryPressed
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.0f)
    ) {
        Text(
            text  = title,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = if (isHighlighted) FontWeight.SemiBold else FontWeight.Normal,
                fontSize   = 16.sp,
                color      = if (isHighlighted) Color.White else Color.White.copy(alpha = 0.45f)
            ),
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
        )
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun AuthButton(onClick: () -> Unit) {
    var isFocused by remember { mutableStateOf(false) }
    Surface(
        onClick  = onClick,
        modifier = Modifier.onFocusChanged { isFocused = it.isFocused },
        shape    = ClickableSurfaceDefaults.shape(RoundedCornerShape(12.dp)),
        // Figma Final UI: «Войти» — серый чип в покое, accent в фокусе
        colors   = ClickableSurfaceDefaults.colors(
            containerColor        = Color(0xFF343B4B),
            focusedContainerColor = Primary,
            pressedContainerColor = PrimaryPressed
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.08f)
    ) {
        Text(
            text  = "Войти",
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize   = 16.sp,
                color      = Color.White
            ),
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
        )
    }
}
