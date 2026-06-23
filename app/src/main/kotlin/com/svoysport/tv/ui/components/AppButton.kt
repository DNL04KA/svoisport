package com.svoysport.tv.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Border
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.svoysport.tv.ui.theme.Blue100
import com.svoysport.tv.ui.theme.Gray4
import com.svoysport.tv.ui.theme.Primary
import com.svoysport.tv.ui.theme.PrimaryDisabled
import com.svoysport.tv.ui.theme.PrimaryPressed

/**
 * Варианты кнопок из Figma UI kit (лист BUTTONS):
 *  - FILLED  — тёмная заливка Blue/100 по умолчанию
 *  - OUTLINE — прозрачная с accent-обводкой
 *  - TEXT    — без фона, только текст
 *
 * Во всех вариантах: focused → accent-заливка, pressed → тёмный accent,
 * disabled → серый. Каждый поддерживает опциональную иконку слева.
 */
enum class AppButtonVariant { FILLED, OUTLINE, TEXT }

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun AppButton(
    text     : String,
    onClick  : () -> Unit,
    modifier : Modifier        = Modifier,
    variant  : AppButtonVariant = AppButtonVariant.FILLED,
    icon     : ImageVector?    = null,
    enabled  : Boolean         = true,
    scale    : Float           = 1f
) {
    var isFocused by remember { mutableStateOf(false) }
    val btnScale by animateFloatAsState(
        targetValue   = if (isFocused) 1.06f else 1.0f,
        animationSpec = tween(150),
        label         = "appBtnScale"
    )
    val shape = RoundedCornerShape((24f * scale).dp)

    // Заливка по варианту: default отличается, focused/pressed/disabled общие
    val containerColor = when (variant) {
        AppButtonVariant.FILLED -> Blue100
        AppButtonVariant.OUTLINE, AppButtonVariant.TEXT -> Color.Transparent
    }

    // Цвет контента: accent для outline/text в покое, белый при фокусе/filled
    val contentColor = when {
        !enabled                              -> Gray4
        isFocused                             -> Color.White
        variant == AppButtonVariant.FILLED    -> Color.White
        else                                  -> Primary
    }

    val border = if (variant == AppButtonVariant.OUTLINE) {
        ButtonDefaults.border(
            border        = Border(BorderStroke((2f * scale).dp, if (enabled) Primary else Gray4), shape = shape),
            focusedBorder = Border(BorderStroke(0.dp, Color.Transparent), shape = shape)
        )
    } else {
        ButtonDefaults.border(border = Border.None, focusedBorder = Border.None)
    }

    Button(
        onClick  = onClick,
        enabled  = enabled,
        modifier = modifier.onFocusChanged { isFocused = it.isFocused }.scale(btnScale),
        shape    = ButtonDefaults.shape(shape = shape),
        scale    = ButtonDefaults.scale(scale = 1f, focusedScale = 1f),
        border   = border,
        colors   = ButtonDefaults.colors(
            containerColor         = containerColor,
            focusedContainerColor  = Primary,
            pressedContainerColor  = PrimaryPressed,
            disabledContainerColor = if (variant == AppButtonVariant.FILLED) PrimaryDisabled else Color.Transparent
        )
    ) {
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy((8f * scale).dp),
            modifier              = Modifier.padding(horizontal = (20f * scale).dp, vertical = (6f * scale).dp)
        ) {
            if (icon != null) {
                Icon(
                    imageVector        = icon,
                    contentDescription = null,
                    tint               = contentColor,
                    modifier           = Modifier.size((18f * scale).dp)
                )
            }
            Text(
                text  = text,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontSize   = (16f * scale).sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = contentColor
                )
            )
        }
    }
}
