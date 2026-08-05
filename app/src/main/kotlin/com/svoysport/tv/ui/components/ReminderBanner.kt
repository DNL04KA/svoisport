package com.svoysport.tv.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.Icon
import androidx.tv.material3.Text
import com.svoysport.tv.R
import com.svoysport.tv.reminder.MatchReminderManager
import com.svoysport.tv.ui.theme.Gray3
import com.svoysport.tv.ui.theme.Primary

@Composable
fun GlobalReminderBanner(onWatch: (String) -> Unit) {
    val alert by MatchReminderManager.activeAlert
    val current = alert ?: return
    val watchFocus = remember(current.matchId) { FocusRequester() }

    BackHandler { MatchReminderManager.dismissAlert() }
    LaunchedEffect(current.matchId) { watchFocus.requestFocus() }

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val scale = minOf(maxWidth.value / 1920f, maxHeight.value / 1080f, 1f).coerceAtLeast(0.5f)
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = (48f * scale).dp, vertical = (48f * scale).dp)
                .fillMaxWidth()
                .height((248f * scale).dp)
                .background(Color(0xFA1D1E20), RoundedCornerShape((28f * scale).dp))
                .padding(horizontal = (68f * scale).dp, vertical = (42f * scale).dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy((14f * scale).dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy((14f * scale).dp)
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(R.drawable.ic_bell),
                        contentDescription = null,
                        tint = Color(0xFFE3E3E3),
                        modifier = Modifier.size((30f * scale).dp)
                    )
                    Text(
                        "Трансляция скоро начнётся",
                        color = Color(0xFFE3E3E3),
                        fontSize = (30f * scale).sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Text(current.category, color = Gray3, fontSize = (26f * scale).sp)
                Text(
                    current.title,
                    color = Color(0xFFE3E3E3),
                    fontSize = (36f * scale).sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy((20f * scale).dp)) {
                ReminderActionButton(
                    text = "Смотреть",
                    primary = true,
                    modifier = Modifier.focusRequester(watchFocus),
                    scale = scale,
                    onClick = {
                        MatchReminderManager.dismissAlert()
                        onWatch(current.matchId)
                    }
                )
                ReminderActionButton(
                    text = "Закрыть",
                    primary = false,
                    scale = scale,
                    onClick = MatchReminderManager::dismissAlert
                )
            }
        }
    }
}

@Composable
private fun ReminderActionButton(
    text: String,
    primary: Boolean,
    scale: Float,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.size(width = (210f * scale).dp, height = (88f * scale).dp),
        shape = ButtonDefaults.shape(RoundedCornerShape((18f * scale).dp)),
        colors = ButtonDefaults.colors(
            containerColor = if (primary) Primary else Color(0xFF2D3240),
            focusedContainerColor = if (primary) Color(0xFF5968F5) else Color(0xFF41485A)
        ),
        scale = ButtonDefaults.scale(focusedScale = 1.05f)
    ) {
        Text(text, color = Color(0xFFE3E3E3), fontSize = (30f * scale).sp)
    }
}
