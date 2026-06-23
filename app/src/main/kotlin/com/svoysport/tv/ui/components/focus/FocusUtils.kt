package com.svoysport.tv.ui.components.focus

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.svoysport.tv.ui.theme.Primary

/** D-pad focus scale — 1.08f по спецификации */
private const val FOCUSED_SCALE   = 1.08f
private const val UNFOCUSED_SCALE = 1.00f

@Composable
fun Modifier.tvFocusScale(
    isFocused: Boolean,
    focusedScale: Float = FOCUSED_SCALE
): Modifier {
    val scale by animateFloatAsState(
        targetValue   = if (isFocused) focusedScale else UNFOCUSED_SCALE,
        animationSpec = tween(durationMillis = 150),
        label         = "tvFocusScale"
    )
    return this.scale(scale)
}

@Composable
fun Modifier.tvFocusBorder(
    isFocused: Boolean,
    shape: Shape         = RectangleShape,
    focusedColor: Color  = Primary,
    strokeDp: Float      = 2f
): Modifier = if (isFocused) border(strokeDp.dp, focusedColor, shape) else this

@Composable
fun Modifier.tvFocusGradientBorder(
    isFocused: Boolean,
    shape: Shape = RectangleShape,
    colors: List<Color> = listOf(Color(0xFF4556EB), Color(0xFF273185)),
    strokeDp: Float = 2f
): Modifier = if (isFocused) border(strokeDp.dp, Brush.linearGradient(colors), shape) else this
