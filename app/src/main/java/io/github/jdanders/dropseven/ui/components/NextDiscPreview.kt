package io.github.jdanders.dropseven.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import io.github.jdanders.dropseven.model.Disc
import io.github.jdanders.dropseven.ui.theme.*

@Composable
fun NextDiscPreview(
    disc: Disc,
    size: Dp,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    
    Canvas(
        modifier = modifier.size(size)
    ) {
        val center = Offset(this.size.width / 2, this.size.height / 2)
        val radius = this.size.width * 0.4f
        
        when (disc) {
            is Disc.Numbered -> {
                val color = getDiscColor(disc.value)
                
                // Draw colored circle
                drawCircle(
                    color = color,
                    radius = radius,
                    center = center
                )
                
                // Draw number
                val textLayoutResult = textMeasurer.measure(
                    text = disc.value.toString(),
                    style = TextStyle(
                        color = Color.White,
                        fontSize = (radius * 0.8f).sp
                    )
                )
                drawText(
                    textLayoutResult = textLayoutResult,
                    topLeft = Offset(
                        center.x - textLayoutResult.size.width / 2,
                        center.y - textLayoutResult.size.height / 2
                    )
                )
            }
            is Disc.Solid -> {
                val color = SolidDiscColor
                
                // Draw solid circle
                drawCircle(
                    color = color,
                    radius = radius,
                    center = center
                )
                
                // Draw circle outline
                drawCircle(
                    color = Color.Black.copy(alpha = 0.3f),
                    radius = radius,
                    center = center,
                    style = Stroke(width = 2f)
                )
            }
        }
    }
}

private fun getDiscColor(value: Int): Color {
    return when (value) {
        1 -> DiscColor1
        2 -> DiscColor2
        3 -> DiscColor3
        4 -> DiscColor4
        5 -> DiscColor5
        6 -> DiscColor6
        7 -> DiscColor7
        else -> Color.Gray
    }
}

