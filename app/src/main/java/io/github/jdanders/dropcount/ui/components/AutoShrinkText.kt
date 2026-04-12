package io.github.jdanders.dropcount.ui.components

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

/**
 * A Text composable that automatically shrinks its font size to fit on one line,
 * down to [minFontSize]. Stays invisible until the final size is determined to
 * avoid a visible flash at the oversized font.
 *
 * @param initialFontSize Optional pre-calculated font size to use as the starting point.
 *   Providing a good estimate (e.g. from [UIConfig.calculateTitleFontSize]) reduces the
 *   number of layout passes needed before the text is revealed.
 */
@Composable
fun AutoShrinkText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    style: TextStyle = LocalTextStyle.current,
    textAlign: TextAlign? = null,
    minFontSize: TextUnit = 8.sp,
    initialFontSize: TextUnit = style.fontSize
) {
    var displaySize by remember(text, initialFontSize) { mutableStateOf(initialFontSize) }
    var readyToDraw by remember(text, initialFontSize) { mutableStateOf(false) }

    Text(
        text = text,
        style = style.copy(fontSize = displaySize),
        color = if (readyToDraw) color else Color.Transparent,
        textAlign = textAlign,
        maxLines = 1,
        softWrap = false,
        overflow = TextOverflow.Visible,
        modifier = modifier,
        onTextLayout = { result ->
            if (result.hasVisualOverflow && displaySize > minFontSize) {
                displaySize = maxOf(minFontSize.value, displaySize.value * 0.9f).sp
            } else {
                readyToDraw = true
            }
        }
    )
}
