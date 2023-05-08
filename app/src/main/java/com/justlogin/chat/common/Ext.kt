package com.justlogin.chat.common

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.toColor

fun Color.Companion.parse(colorString: String): Color =
    Color(color = android.graphics.Color.parseColor(colorString))

fun Color.adjustColor(percentage: Float): Color {
    val color = ColorUtils.blendARGB(this.toArgb(), 0xFFFFFFFF.toInt(), percentage)
    return Color(color)
}
