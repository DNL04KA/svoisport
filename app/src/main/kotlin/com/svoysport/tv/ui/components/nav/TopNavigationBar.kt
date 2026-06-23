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
            .height(64.dp)
            .padding(horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Логотип: иконка всегда, текст «СВОЙ СПОРТ» появляется при раскрытии сайдбара
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter            = painterResource(R.drawable.logo_icon),
                contentDescription = "Свой Спорт",
                modifier           = Modifier.size(36.dp)
            )
            androidx.compose.animation.AnimatedVisibility(
                visible = logoExpanded,
                enter   = androidx.compose.animation.fadeIn(tween(200)) +
                          androidx.compose.animation.expandHorizontally(tween(250)),
                exit    = androidx.compose.animation.fadeOut(tween(150)) +
                          androidx.compose.animation.shrinkHorizontally(tween(200))
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text  = "СВОЙ СПОРТ",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold, fontSize = 18.sp,
                            letterSpacing = 1.5.sp, color = Color.White
                        ),
                        maxLines = 1
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Центральные табы
        Row(
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
                    .size(40.dp)
                    .onFocusChanged { isFocused = it.isFocused },
                shape  = ClickableSurfaceDefaults.shape(RoundedCornerShape(20.dp)),
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
                        modifier           = Modifier.size(20.dp)
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
        // Figma Tabs: активный таб и фокус — accent (синий)
        colors   = ClickableSurfaceDefaults.colors(
            containerColor        = if (selected) Primary else Color.Transparent,
            focusedContainerColor = Primary,
            pressedContainerColor = PrimaryPressed
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.0f)
    ) {
        Text(
            text  = title,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = if (isHighlighted) FontWeight.SemiBold else FontWeight.Normal,
                fontSize   = 18.sp,
                color      = if (isHighlighted) Color.White else Color.White.copy(alpha = 0.45f)
            ),
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 10.dp)
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
        // Figma: «Войти» — accent (синяя)
        colors   = ClickableSurfaceDefaults.colors(
            containerColor        = Primary,
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
