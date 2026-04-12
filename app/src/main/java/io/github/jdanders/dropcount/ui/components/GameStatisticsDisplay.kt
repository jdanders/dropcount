package io.github.jdanders.dropcount.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.NumberFormat
import io.github.jdanders.dropcount.R
import io.github.jdanders.dropcount.data.ModeGameStatistics
import io.github.jdanders.dropcount.model.VisualTheme
import io.github.jdanders.dropcount.ui.theme.*
import kotlin.math.roundToInt

/**
 * Displays game statistics at the end of a game.
 * Shows statistics for the specific game mode that was just played.
 */
@Composable
fun GameStatisticsDisplay(
    statistics: ModeGameStatistics,
    currentScore: Int,
    currentLevel: Int,
    currentChainLength: Int,
    currentSingleMove: Int,
    modifier: Modifier = Modifier,
    visualTheme: VisualTheme = VisualTheme.CLASSIC
) {
    val isNewHighScore = statistics.totalGamesPlayed > 0 && currentScore > statistics.highestScore
    val isNewLevelRecord = statistics.totalGamesPlayed > 0 && currentLevel > statistics.highestLevel
    val isNewChainRecord = statistics.totalGamesPlayed > 0 && currentChainLength > statistics.longestChain
    val isNewDropRecord = statistics.totalGamesPlayed > 0 && currentSingleMove > statistics.highestSingleMove

    val isFoundry = visualTheme == VisualTheme.FOUNDRY
    val cardShape = if (isFoundry) RoundedCornerShape(0.dp) else RoundedCornerShape(12.dp)
    val cardColor = if (isFoundry) ConcreteMid else MaterialTheme.colorScheme.surfaceVariant
    val cardBorder = if (isFoundry) BorderStroke(2.dp, GridStroke) else null

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = cardColor
        ),
        shape = cardShape,
        border = cardBorder
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.stats_title).let { if (isFoundry) it.uppercase() else it },
                style = if (isFoundry) MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                ) else MaterialTheme.typography.titleLarge,
                color = if (isFoundry) StarkWhite else Color.Unspecified
            )

            if (isFoundry) {
                HorizontalDivider(color = IndustrialOrange, thickness = 2.dp)
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Current game stats
            val fmt = NumberFormat.getNumberInstance()
            StatRow(
                label = stringResource(R.string.label_score),
                value = fmt.format(currentScore),
                highlight = true,
                isNewHighScore = isNewHighScore,
                visualTheme = visualTheme
            )

            StatRow(
                label = stringResource(R.string.stats_highest_level),
                value = fmt.format(currentLevel),
                isNewHighScore = isNewLevelRecord,
                visualTheme = visualTheme
            )

            StatRow(
                label = stringResource(R.string.stats_longest_chain),
                value = fmt.format(currentChainLength),
                isNewHighScore = isNewChainRecord,
                visualTheme = visualTheme
            )

            StatRow(
                label = stringResource(R.string.stats_best_move),
                value = fmt.format(currentSingleMove),
                isNewHighScore = isNewDropRecord,
                visualTheme = visualTheme
            )

            Spacer(modifier = Modifier.height(8.dp))



            // Only show statistics table if we have data
            if (statistics.totalGamesPlayed > 0) {
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(R.string.stats_all_time).let { if (isFoundry) it.uppercase() else it },
                    style = if (isFoundry) MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    ) else MaterialTheme.typography.titleMedium,
                    color = if (isFoundry) StarkWhite.copy(alpha = 0.8f) else Color.Unspecified
                )

                // Statistics table
                StatisticsTable(statistics = statistics, visualTheme = visualTheme)
            }
        }
    }
}

@Composable
private fun StatRow(
    label: String,
    value: String,
    highlight: Boolean = false,
    isNewHighScore: Boolean = false,
    modifier: Modifier = Modifier,
    visualTheme: VisualTheme = VisualTheme.CLASSIC
) {
    val isFoundry = visualTheme == VisualTheme.FOUNDRY
    val labelColor = if (isFoundry) StarkWhite.copy(alpha = 0.7f) else Color.Unspecified
    val valueColor = if (highlight) {
        when (visualTheme) {
            VisualTheme.NEON -> MaterialTheme.colorScheme.primary
            VisualTheme.FOUNDRY -> IndustrialOrange
            else -> MaterialTheme.colorScheme.onSurfaceVariant
        }
    } else {
        if (isFoundry) StarkWhite else MaterialTheme.colorScheme.onSurfaceVariant
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label.let { if (isFoundry) it.uppercase() else it },
            style = if (highlight) {
                MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = if (isFoundry) FontWeight.Black else FontWeight.SemiBold,
                    letterSpacing = if (isFoundry) 1.sp else 0.sp
                )
            } else {
                MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (isFoundry) FontWeight.Bold else FontWeight.Medium
                )
            },
            color = labelColor
        )
        val baseStyle = if (highlight) {
            MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = if (isFoundry) 1.sp else 0.sp
            )
        } else {
            MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
        }
        AutoShrinkText(
            text = if (isNewHighScore && !isFoundry) "🌟 $value" else value,
            style = baseStyle,
            color = valueColor
        )
    }
}

@Composable
private fun StatisticsTable(
    statistics: ModeGameStatistics,
    visualTheme: VisualTheme,
    modifier: Modifier = Modifier
) {
    val isFoundry = visualTheme == VisualTheme.FOUNDRY
    val fmt = NumberFormat.getNumberInstance()
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        // Header row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TableHeaderCell("", visualTheme = visualTheme, modifier = Modifier.weight(1f))
            TableHeaderCell(stringResource(R.string.label_score), visualTheme = visualTheme, modifier = Modifier.weight(1f))
            TableHeaderCell(stringResource(R.string.label_level), visualTheme = visualTheme, modifier = Modifier.weight(1f))
            TableHeaderCell(stringResource(R.string.label_chain_header), visualTheme = visualTheme, modifier = Modifier.weight(1f))
            TableHeaderCell(stringResource(R.string.label_drop_header), visualTheme = visualTheme, modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Best row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TableLabelCell(stringResource(R.string.label_best), visualTheme = visualTheme, modifier = Modifier.weight(1f))
            TableDataCell(fmt.format(statistics.highestScore), visualTheme = visualTheme, modifier = Modifier.weight(1f))
            TableDataCell(fmt.format(statistics.highestLevel), visualTheme = visualTheme, modifier = Modifier.weight(1f))
            TableDataCell(fmt.format(statistics.longestChain), visualTheme = visualTheme, modifier = Modifier.weight(1f))
            TableDataCell(fmt.format(statistics.highestSingleMove), visualTheme = visualTheme, modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Average row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TableLabelCell(stringResource(R.string.label_avg), visualTheme = visualTheme, modifier = Modifier.weight(1f))
            TableDataCell(fmt.format(statistics.averageScore.roundToInt()), visualTheme = visualTheme, modifier = Modifier.weight(1f))
            TableDataCell(String.format("%.1f", statistics.averageLevel), visualTheme = visualTheme, modifier = Modifier.weight(1f))
            TableDataCell(String.format("%.1f", statistics.averageChainLength), visualTheme = visualTheme, modifier = Modifier.weight(1f))
            TableDataCell(statistics.averageSingleMoveScore.roundToInt().let { fmt.format(it) }, visualTheme = visualTheme, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun TableHeaderCell(
    text: String,
    visualTheme: VisualTheme,
    modifier: Modifier = Modifier
) {
    val isFoundry = visualTheme == VisualTheme.FOUNDRY
    Text(
        text = text.let { if (isFoundry) it.uppercase() else it },
        style = MaterialTheme.typography.bodySmall.copy(
            fontWeight = if (isFoundry) FontWeight.Black else FontWeight.Bold,
            letterSpacing = if (isFoundry) 1.sp else 0.sp
        ),
        color = if (isFoundry) StarkWhite.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier
    )
}

@Composable
private fun TableLabelCell(
    text: String,
    visualTheme: VisualTheme,
    modifier: Modifier = Modifier
) {
    val isFoundry = visualTheme == VisualTheme.FOUNDRY
    Text(
        text = text.let { if (isFoundry) it.uppercase() else it },
        style = MaterialTheme.typography.bodyMedium.copy(
            fontWeight = if (isFoundry) FontWeight.Black else FontWeight.SemiBold,
            letterSpacing = if (isFoundry) 0.5.sp else 0.sp
        ),
        color = if (isFoundry) StarkWhite.copy(alpha = 0.8f) else Color.Unspecified,
        modifier = modifier
    )
}

@Composable
private fun TableDataCell(
    text: String,
    visualTheme: VisualTheme,
    modifier: Modifier = Modifier
) {
    val isFoundry = visualTheme == VisualTheme.FOUNDRY
    AutoShrinkText(
        text = text,
        style = MaterialTheme.typography.bodyMedium.copy(
            fontWeight = if (isFoundry) FontWeight.Bold else FontWeight.Normal
        ),
        color = if (isFoundry) StarkWhite else MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier
    )
}


