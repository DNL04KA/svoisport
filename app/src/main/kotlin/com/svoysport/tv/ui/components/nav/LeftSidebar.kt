package com.svoysport.tv.ui.components.nav

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.*
import com.svoysport.tv.R
import com.svoysport.tv.ui.theme.Gray3
import com.svoysport.tv.ui.theme.Primary
import com.svoysport.tv.ui.theme.SidebarBg

enum class SidebarItem {
    SEARCH, BOOKMARKS,
    FOOTBALL, HOCKEY, HANDBALL, BASKETBALL, VOLLEYBALL, OTHER
}

// Figma NAVBAR: контурная иконка (iconRes) в покое, заливка (iconActive) в
// фокусе/выбранном. (ic_sport_other_active был экспортирован с лишним белым
// прямоугольником на весь кадр — починен, заливка теперь корректна.)
private data class SidebarIconData(
    val type:       SidebarItem,
    val iconRes:    Int,
    val iconActive: Int,
    val label:      String
)

private val SIDEBAR_COLLAPSED = 60.dp
private val SIDEBAR_EXPANDED  = 220.dp
private val ICON_SIZE         = 22.dp
private val ITEM_HEIGHT       = 44.dp

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun LeftSidebar(
    selectedItem: SidebarItem?,
    onItemSelected: (SidebarItem) -> Unit,
    onExpandedChange: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val topItems = listOf(
        SidebarIconData(SidebarItem.SEARCH,    R.drawable.ic_search,    R.drawable.ic_search_active,    "Поиск"),
        SidebarIconData(SidebarItem.BOOKMARKS, R.drawable.ic_bookmark,  R.drawable.ic_bookmark_active,  "Избранное"),
    )
    val sportItems = listOf(
        SidebarIconData(SidebarItem.FOOTBALL,   R.drawable.ic_sport_football,   R.drawable.ic_sport_football_active,   "Футбол"),
        SidebarIconData(SidebarItem.HOCKEY,     R.drawable.ic_sport_hockey,     R.drawable.ic_sport_hockey_active,     "Хоккей"),
        SidebarIconData(SidebarItem.HANDBALL,   R.drawable.ic_sport_handball,   R.drawable.ic_sport_handball_active,   "Гандбол"),
        SidebarIconData(SidebarItem.BASKETBALL, R.drawable.ic_sport_basketball, R.drawable.ic_sport_basketball_active, "Баскетбол"),
        SidebarIconData(SidebarItem.VOLLEYBALL, R.drawable.ic_sport_volleyball, R.drawable.ic_sport_volleyball_active, "Волейбол"),
        SidebarIconData(SidebarItem.OTHER,      R.drawable.ic_sport_other,      R.drawable.ic_sport_other_active,      "Другой спорт"),
    )

    var sidebarFocused by remember { mutableStateOf(false) }
    // Figma: открытие — 300ms ease-out, закрытие — 200ms ease-in
    val sidebarWidth by animateDpAsState(
        targetValue   = if (sidebarFocused) SIDEBAR_EXPANDED else SIDEBAR_COLLAPSED,
        animationSpec = tween(
            durationMillis = if (sidebarFocused) 300 else 200,
            easing         = if (sidebarFocused) androidx.compose.animation.core.FastOutSlowInEasing
                             else androidx.compose.animation.core.FastOutLinearInEasing
        ),
        label = "sidebarWidth"
    )

    Column(
        modifier = modifier
            .width(sidebarWidth)
            .fillMaxHeight()
            .background(SidebarBg)
            .padding(vertical = 12.dp, horizontal = 8.dp)
            .onFocusChanged {
                sidebarFocused = it.hasFocus
                onExpandedChange(it.hasFocus)
            },
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        topItems.forEach { item ->
            SidebarRow(
                item      = item,
                isSelected = selectedItem == item.type,
                showLabel  = sidebarFocused,
                onClick    = { onItemSelected(item.type) }
            )
        }

        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .padding(horizontal = 4.dp)
                .background(Color.White.copy(alpha = 0.10f))
        )
        Spacer(modifier = Modifier.height(6.dp))

        sportItems.forEach { item ->
            SidebarRow(
                item       = item,
                isSelected = selectedItem == item.type,
                showLabel  = sidebarFocused,
                onClick    = { onItemSelected(item.type) }
            )
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun SidebarRow(
    item: SidebarIconData,
    isSelected: Boolean,
    showLabel: Boolean,
    onClick: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    val isActive = isSelected || isFocused
    // Figma NAVBAR: покой — серый контур; фокус — светлая плашка + тёмный контент;
    // выбран — синяя плашка + белый контент.
    val contentColor = when {
        isFocused  -> Color(0xFF171717)
        isSelected -> Color.White
        else       -> Gray3
    }

    Surface(
        onClick  = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(ITEM_HEIGHT)
            .onFocusChanged { isFocused = it.isFocused },
        shape  = ClickableSurfaceDefaults.shape(RoundedCornerShape(10.dp)),
        colors = ClickableSurfaceDefaults.colors(
            containerColor        = if (isSelected) Primary else Color.Transparent,
            focusedContainerColor = Color(0xFFE2E2E2)
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.05f)
    ) {
        Row(
            modifier              = Modifier
                .fillMaxSize()
                .padding(horizontal = 11.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector        = ImageVector.vectorResource(if (isActive) item.iconActive else item.iconRes),
                contentDescription = item.label,
                tint               = contentColor,
                modifier           = Modifier.size(ICON_SIZE)
            )

            AnimatedVisibility(
                visible = showLabel,
                enter   = fadeIn(animationSpec = tween(150)),
                exit    = fadeOut(animationSpec = tween(100))
            ) {
                Text(
                    text     = item.label,
                    style    = MaterialTheme.typography.labelSmall.copy(
                        fontSize   = 14.sp,
                        fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
                        color      = contentColor
                    ),
                    maxLines = 1,
                    softWrap = false
                )
            }
        }
    }
}
