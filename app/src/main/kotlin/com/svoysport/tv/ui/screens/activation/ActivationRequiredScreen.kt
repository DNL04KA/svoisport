package com.svoysport.tv.ui.screens.activation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.*
import com.svoysport.tv.R
import com.svoysport.tv.ui.theme.Background
import com.svoysport.tv.ui.theme.Gray3

/**
 * «Подписка не найдена» — предлагает купить подписку или активировать
 * уже существующую (по QR). Шаг 5 из флоу активации.
 */
@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun ActivationRequiredScreen(
    onBuy: () -> Unit,
    onActivate: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBack)

    BoxWithConstraints(modifier = modifier.fillMaxSize().background(Background)) {
        val scale = minOf(maxWidth.value / 1920f, maxHeight.value / 1080f, 1f).coerceAtLeast(0.4f)
        val activateFr = remember { FocusRequester() }
        LaunchedEffect(Unit) { runCatching { activateFr.requestFocus() } }

        Column(
            modifier = Modifier.fillMaxSize().padding((48f * scale).dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier.size((84f * scale).dp)
                    .background(Color(0xFF272C38), RoundedCornerShape(50)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_info),
                    contentDescription = null, tint = Color(0xFF4556EB),
                    modifier = Modifier.size((44f * scale).dp)
                )
            }
            Spacer(Modifier.height((28f * scale).dp))
            Text(
                text = "Подписка не найдена",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = (44f * scale).sp, fontWeight = FontWeight.SemiBold, color = Color.White
                ),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height((14f * scale).dp))
            Text(
                text = "Если у вас уже есть подписка на сайте sport-tv.by,\nактивируйте её на этом телевизоре.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = (24f * scale).sp, color = Gray3
                ),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height((40f * scale).dp))
            Row(horizontalArrangement = Arrangement.spacedBy((20f * scale).dp)) {
                ActionButton(
                    text = "Активировать подписку",
                    icon = R.drawable.ic_play,
                    primary = true,
                    scale = scale,
                    focusRequester = activateFr,
                    onClick = onActivate
                )
                ActionButton(
                    text = "Купить подписку",
                    icon = R.drawable.ic_card,
                    primary = false,
                    scale = scale,
                    onClick = onBuy
                )
            }
            Spacer(Modifier.height((28f * scale).dp))
            ActionButton(
                text = "Назад",
                icon = R.drawable.ic_arrow_left,
                primary = false,
                scale = scale,
                onClick = onBack
            )
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
internal fun ActionButton(
    text: String,
    icon: Int?,
    primary: Boolean,
    scale: Float,
    modifier: Modifier = Modifier,
    focusRequester: FocusRequester? = null,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape((14f * scale).dp)
    Surface(
        onClick = onClick,
        modifier = modifier.height((64f * scale).dp)
            .then(if (focusRequester != null) Modifier.focusRequester(focusRequester) else Modifier),
        shape = ClickableSurfaceDefaults.shape(shape),
        colors = ClickableSurfaceDefaults.colors(
            containerColor        = if (primary) Color(0xFF272C38) else Color.Transparent,
            focusedContainerColor = Color(0xFF4556EB),
            contentColor          = Color.White,
            focusedContentColor   = Color.White
        ),
        border = ClickableSurfaceDefaults.border(
            border = if (primary) Border.None else Border(
                border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF464968)),
                shape = shape
            )
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.05f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = (28f * scale).dp).fillMaxHeight(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy((10f * scale).dp)
        ) {
            if (icon != null) {
                Icon(
                    imageVector = ImageVector.vectorResource(icon),
                    contentDescription = null, tint = Color.White,
                    modifier = Modifier.size((22f * scale).dp)
                )
            }
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = (22f * scale).sp, fontWeight = FontWeight.SemiBold, color = Color.White
                )
            )
        }
    }
}
