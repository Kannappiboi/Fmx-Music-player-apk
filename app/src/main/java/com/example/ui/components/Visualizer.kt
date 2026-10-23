package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AudiophileGold
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.SurfaceBorder

@Composable
fun SpectrumVisualizer(
    levels: FloatArray,
    modifier: Modifier = Modifier,
    barCount: Int = 32,
    isPlaying: Boolean = true
) {
    val gradientBrush = remember {
        Brush.verticalGradient(
            colors = listOf(
                AudiophileGold,
                ElectricCyan,
                SurfaceBorder
            )
        )
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
    ) {
        val width = size.width
        val height = size.height
        val totalBars = barCount.coerceAtMost(levels.size)
        val spacing = 3.dp.toPx()
        val totalSpacing = spacing * (totalBars - 1)
        val barWidth = ((width - totalSpacing) / totalBars).coerceAtLeast(2.dp.toPx())

        for (i in 0 until totalBars) {
            val level = levels[i].coerceIn(0.04f, 1.0f)
            val barHeight = (height * level).coerceAtLeast(3.dp.toPx())
            val x = i * (barWidth + spacing)
            val y = height - barHeight

            drawRoundRect(
                brush = gradientBrush,
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
            )
        }
    }
}
