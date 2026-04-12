package io.github.jdanders.dropcount.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import io.github.jdanders.dropcount.config.GameConfig
import io.github.jdanders.dropcount.config.UIConfig
import io.github.jdanders.dropcount.model.Disc
import io.github.jdanders.dropcount.model.VisualTheme
import io.github.jdanders.dropcount.ui.theme.ThemeRenderer

import androidx.compose.ui.res.stringResource
import io.github.jdanders.dropcount.R

@Composable
fun HowToPlayDialog(
    onDismiss: () -> Unit,
    visualTheme: VisualTheme = VisualTheme.CLASSIC
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .fillMaxHeight(0.9f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            HowToPlayContent(onDismiss, visualTheme)
        }
    }
}

@Composable
fun HowToPlayContent(
    onDismiss: () -> Unit,
    visualTheme: VisualTheme
) {
    val pages = listOf(
        TutorialPage(
            title = stringResource(R.string.how_to_play_title),
            description = stringResource(R.string.tutorial_step_1),
            content = { TutorialGridPreview(visualTheme, type = PreviewType.BASIC) }
        ),
        TutorialPage(
            title = stringResource(R.string.menu_challenge_mode),
            description = stringResource(R.string.tutorial_step_2),
            content = { TutorialGridPreview(visualTheme, type = PreviewType.MATCH) }
        ),
        TutorialPage(
            title = stringResource(R.string.theme_classic), // Reusing strings where possible or using step titles
            description = stringResource(R.string.tutorial_step_3),
            content = { TutorialGridPreview(visualTheme, type = PreviewType.SOLID) }
        ),
        TutorialPage(
            title = stringResource(R.string.tutorial_chain_reactions_title),
            description = stringResource(R.string.tutorial_chain_reactions_desc),
            content = { TutorialGridPreview(visualTheme, type = PreviewType.CHAIN) }
        ),
        TutorialPage(
            title = stringResource(R.string.tutorial_new_row_title),
            description = stringResource(R.string.tutorial_step_4),
            content = { TutorialGridPreview(visualTheme, type = PreviewType.NEW_ROW) }
        )
    )

    val pagerState = rememberPagerState(pageCount = { pages.size })

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        val pagerHeight = (maxHeight * 0.5f).coerceIn(200.dp, 320.dp)

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.how_to_play_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(12.dp))

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(pagerHeight)
        ) { page ->
            TutorialPageContent(pages[page])
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Page Indicator
        Row(
            modifier = Modifier.wrapContentHeight(),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(pages.size) { iteration ->
                val color = if (pagerState.currentPage == iteration)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                Box(
                    modifier = Modifier
                        .padding(4.dp)
                        .clip(CircleShape)
                        .background(color)
                        .size(if (pagerState.currentPage == iteration) 12.dp else 8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(if (pagerState.currentPage == pages.size - 1) stringResource(R.string.tutorial_got_it) else stringResource(R.string.action_close))
        }
    }
    } // end BoxWithConstraints
}

@Composable
fun TutorialPageContent(page: TutorialPage) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            page.content()
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = page.title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = page.description,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )
    }
}

enum class PreviewType { BASIC, MATCH, SOLID, CHAIN, NEW_ROW }

@Composable
fun TutorialGridPreview(visualTheme: VisualTheme, type: PreviewType) {
    val themeRenderer = remember(visualTheme) { visualTheme.createRenderer() }
    val textMeasurer = rememberTextMeasurer()

    Canvas(
        modifier = Modifier
            .size(160.dp)
            .padding(8.dp)
    ) {
        val cellSize = size.width / 4
        val radius = cellSize * GameConfig.DISC_RADIUS_FRACTION

        with(themeRenderer) {
            // Background
            drawRoundRect(
                color = Color.Black.copy(alpha = 0.1f),
                size = size,
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(12f)
            )

            when (type) {
                PreviewType.BASIC -> {
                    // Draw a few scattered discs
                    drawDisc(Disc.Numbered(4), Offset(cellSize * 1.5f, cellSize * 1.5f), radius, textMeasurer)
                    drawDisc(Disc.Numbered(2), Offset(cellSize * 2.5f, cellSize * 2.5f), radius, textMeasurer)
                }
                PreviewType.MATCH -> {
                    // Context discs
                    drawDisc(Disc.Numbered(4), Offset(cellSize * 1.5f, cellSize * 1.5f), radius, textMeasurer)

                    // Row of 3 with a "3" in it
                    val rowY = cellSize * 2.5f
                    val discValues = listOf(3, 1, 5)
                    val discValues2 = listOf(7, 6, 6, 2)

                    // Draw standard highlight box for the row of 3
                    drawRect(
                        color = Color.White.copy(alpha = 0.25f),
                        topLeft = Offset(0f, rowY - cellSize / 2 + UIConfig.GRID_CELL_PADDING),
                        size = Size(cellSize * 3, cellSize - UIConfig.GRID_CELL_PADDING * 2)
                    )

                    discValues.forEachIndexed { index, value ->
                        drawDisc(Disc.Numbered(value), Offset(cellSize * (index + 0.5f), rowY), radius, textMeasurer)
                    }
                    discValues2.forEachIndexed { index, value ->
                        drawDisc(Disc.Numbered(value), Offset(cellSize * (index + 0.5f), rowY + cellSize), radius, textMeasurer)
                    }

                    // Highlight the 3 that will break (contiguous count = 3)
                    drawCircle(
                        color = Color.White.copy(alpha = 0.6f),
                        radius = radius * GameConfig.HIGHLIGHT_RADIUS_MULTIPLIER,
                        center = Offset(cellSize * 0.5f, rowY)
                    )
                }
                PreviewType.SOLID -> {
                    drawDisc(Disc.Solid(cracks = 0, crackSeed = 1, hiddenValue = 3), Offset(cellSize * 1.5f, cellSize * 1.5f), radius, textMeasurer)
                    drawDisc(Disc.Solid(cracks = 1, crackSeed = 1, hiddenValue = 3), Offset(cellSize * 2.5f, cellSize * 1.5f), radius, textMeasurer)
                }
                PreviewType.CHAIN -> {
                    drawDisc(Disc.Numbered(2), Offset(cellSize * 1.5f, cellSize * 0.5f), radius, textMeasurer)
                    drawDisc(Disc.Numbered(2), Offset(cellSize * 1.5f, cellSize * 2.5f), radius, textMeasurer)

                    // Arrow indicating fall
                    drawLine(
                        Color.White,
                        Offset(cellSize * 1.5f, cellSize * 1.0f),
                        Offset(cellSize * 1.5f, cellSize * 2.0f),
                        strokeWidth = 4f
                    )
                }
                PreviewType.NEW_ROW -> {
                    // Bottom row discs
                    drawDisc(Disc.Solid(cracks = 0, crackSeed = 1, hiddenValue = 4), Offset(cellSize * 0.5f, cellSize * 3.5f), radius, textMeasurer)
                    drawDisc(Disc.Solid(cracks = 0, crackSeed = 2, hiddenValue = 4), Offset(cellSize * 1.5f, cellSize * 3.5f), radius, textMeasurer)
                    drawDisc(Disc.Solid(cracks = 0, crackSeed = 3, hiddenValue = 4), Offset(cellSize * 2.5f, cellSize * 3.5f), radius, textMeasurer)
                    drawDisc(Disc.Solid(cracks = 0, crackSeed = 4, hiddenValue = 4), Offset(cellSize * 3.5f, cellSize * 3.5f), radius, textMeasurer)
                }
            }
        }
    }
}

data class TutorialPage(
    val title: String,
    val description: String,
    val content: @Composable () -> Unit
)
