@file:OptIn(ExperimentalLayoutApi::class)
package io.github.jdanders.dropcount.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.jdanders.dropcount.data.AllGameStatistics
import io.github.jdanders.dropcount.data.ModeGameStatistics
import io.github.jdanders.dropcount.model.VisualTheme
import io.github.jdanders.dropcount.ui.theme.*
import androidx.compose.ui.res.stringResource
import io.github.jdanders.dropcount.R
import androidx.annotation.StringRes
import io.github.jdanders.dropcount.ui.components.AutoShrinkText
import java.text.NumberFormat
import kotlin.math.roundToInt

import androidx.compose.foundation.layout.ExperimentalLayoutApi

data class GameModeInfo(
    val id: String,
    @param:StringRes val nameRes: Int,
    val getStats: (AllGameStatistics) -> ModeGameStatistics
)

data class ComparisonStat(
    val id: String,
    @param:StringRes val labelRes: Int,
    val getBest: (ModeGameStatistics) -> String,
    val getAverage: ((ModeGameStatistics) -> String)? = null
)

@Composable
fun StatisticsDialog(
    allGameStatistics: AllGameStatistics,
    visualTheme: VisualTheme,
    onDismiss: () -> Unit
) {
    val renderer = remember(visualTheme) { visualTheme.createRenderer() }
    val modes = remember {
        listOf(
            GameModeInfo("normal", R.string.menu_normal_mode) { it.normalMode },
            GameModeInfo("easy", R.string.difficulty_easy) { it.challengeEasy },
            GameModeInfo("medium", R.string.difficulty_medium) { it.challengeMedium },
            GameModeInfo("hard", R.string.difficulty_hard) { it.challengeHard },
            GameModeInfo("extreme", R.string.difficulty_extreme) { it.challengeExtreme },
            GameModeInfo("sequence", R.string.menu_sequence_mode) { it.sequenceMode }
        )
    }

    val comparisonStats = remember {
        val fmt = NumberFormat.getNumberInstance()
        listOf(
            ComparisonStat(
                id = "score",
                labelRes = R.string.label_score,
                getBest = { fmt.format(it.highestScore) },
                getAverage = { fmt.format(it.averageScore.roundToInt()) }
            ),
            ComparisonStat(
                id = "level",
                labelRes = R.string.label_level,
                getBest = { fmt.format(it.highestLevel) },
                getAverage = { "%.1f".format(it.averageLevel) }
            ),
            ComparisonStat(
                id = "chain",
                labelRes = R.string.label_chain_header,
                getBest = { fmt.format(it.longestChain) },
                getAverage = { "%.1f".format(it.averageChainLength) }
            ),
            ComparisonStat(
                id = "move",
                labelRes = R.string.label_drop_header,
                getBest = { fmt.format(it.highestSingleMove) },
                getAverage = { fmt.format(it.averageSingleMoveScore.roundToInt()) }
            ),
            ComparisonStat(
                id = "games",
                labelRes = R.string.stats_total_games,
                getBest = { fmt.format(it.totalGamesPlayed) },
                getAverage = null
            )
        )
    }

    var selectedMode by remember { mutableStateOf("normal") }
    var selectedComparisonStat by remember { mutableStateOf("score") }
    var isComparisonExpanded by remember { mutableStateOf(false) }

    val currentModeInfo = modes.find { it.id == selectedMode } ?: modes[0]
    val currentStats = currentModeInfo.getStats(allGameStatistics)
    val currentComparisonStat = comparisonStats.find { it.id == selectedComparisonStat } ?: comparisonStats[0]

    val isFoundry = visualTheme == VisualTheme.FOUNDRY
    val dialogShape = if (isFoundry) RoundedCornerShape(0.dp) else RoundedCornerShape(24.dp)
    val buttonShape = if (isFoundry) RoundedCornerShape(0.dp) else RoundedCornerShape(12.dp)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = if (isFoundry) Alignment.Start else Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.stats_title).let { if (isFoundry) it.uppercase() else it },
                    fontWeight = FontWeight.Black,
                    fontSize = if (isFoundry) 28.sp else 32.sp,
                    letterSpacing = if (isFoundry) 2.sp else 1.sp,
                    color = renderer.getLabelTextColor()
                )
                if (isFoundry) {
                    Spacer(modifier = Modifier.height(4.dp))
                    HorizontalDivider(color = IndustrialOrange, thickness = 2.dp)
                }
                Text(
                    text = stringResource(R.string.stats_subtitle).let { if (isFoundry) it.uppercase() else it },
                    fontSize = if (isFoundry) 11.sp else 14.sp,
                    fontWeight = if (isFoundry) FontWeight.Bold else FontWeight.Normal,
                    color = renderer.getLabelTextColor().copy(alpha = 0.6f)
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Mode Chips - Wrapped, no scrolling
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    maxItemsInEachRow = 3
                ) {
                    modes.forEach { mode ->
                        ModeChip(
                            nameRes = mode.nameRes,
                            isSelected = selectedMode == mode.id,
                            renderer = renderer,
                            onClick = { selectedMode = mode.id }
                        )
                    }
                }

                // Stats Display
                if (currentStats.totalGamesPlayed > 0) {
                    // Games Played Banner
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isFoundry) ConcreteMid else ButtonPrimary
                        ),
                        shape = if (isFoundry) RoundedCornerShape(0.dp) else RoundedCornerShape(16.dp),
                        border = if (isFoundry) BorderStroke(2.dp, IndustrialOrange) else null
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = NumberFormat.getNumberInstance().format(currentStats.totalGamesPlayed),
                                fontSize = 40.sp,
                                fontWeight = FontWeight.Black,
                                color = if (isFoundry) IndustrialOrange else Color.Black
                            )
                            Text(
                                text = stringResource(R.string.stats_games_played_label),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp,
                                color = if (isFoundry) StarkWhite.copy(alpha = 0.7f) else Color.Black.copy(alpha = 0.7f)
                            )
                        }
                    }

                    // Stats Grid
                    val fmt = NumberFormat.getNumberInstance()
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatCard(
                            modifier = Modifier.weight(1f),
                            label = stringResource(R.string.stats_high_score),
                            value = fmt.format(currentStats.highestScore),
                            subValue = "${stringResource(R.string.label_avg).lowercase()}: ${fmt.format(currentStats.averageScore.roundToInt())}",
                            color = when {
                                visualTheme == VisualTheme.NEON -> renderer.getScoreColor()
                                visualTheme == VisualTheme.FOUNDRY -> IndustrialOrange
                                else -> renderer.getLabelTextColor()
                            },
                            renderer = renderer
                        )
                        StatCard(
                            modifier = Modifier.weight(1f),
                            label = stringResource(R.string.stats_highest_level),
                            value = fmt.format(currentStats.highestLevel),
                            subValue = "${stringResource(R.string.label_avg).lowercase()}: ${"%.1f".format(currentStats.averageLevel)}",
                            color = when {
                                visualTheme == VisualTheme.NEON -> renderer.getHighScoreColor()
                                visualTheme == VisualTheme.FOUNDRY -> IndustrialOrange
                                else -> renderer.getLabelTextColor()
                            },
                            renderer = renderer
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatCard(
                            modifier = Modifier.weight(1f),
                            label = stringResource(R.string.stats_longest_chain),
                            value = fmt.format(currentStats.longestChain),
                            subValue = "${stringResource(R.string.label_avg).lowercase()}: ${"%.1f".format(currentStats.averageChainLength)}",
                            color = when {
                                visualTheme == VisualTheme.NEON -> renderer.getDiscColor(6)
                                visualTheme == VisualTheme.FOUNDRY -> IndustrialOrange
                                else -> renderer.getLabelTextColor()
                            },
                            renderer = renderer
                        )
                        StatCard(
                            modifier = Modifier.weight(1f),
                            label = stringResource(R.string.stats_best_move),
                            value = fmt.format(currentStats.highestSingleMove),
                            subValue = "${stringResource(R.string.label_avg).lowercase()}: ${fmt.format(currentStats.averageSingleMoveScore.roundToInt())}",
                            color = when {
                                visualTheme == VisualTheme.NEON -> renderer.getDiscColor(4)
                                visualTheme == VisualTheme.FOUNDRY -> IndustrialOrange
                                else -> renderer.getLabelTextColor()
                            },
                            renderer = renderer
                        )
                    }

                    // Comparison Section
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = renderer.getCardBackgroundColor()
                        ),
                        border = BorderStroke(1.dp, renderer.getCardBorderColor()),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            // Header
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { isComparisonExpanded = !isComparisonExpanded }
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = stringResource(R.string.stats_compare_modes),
                                    color = renderer.getLabelTextColor(),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = if (isComparisonExpanded) "▲" else "▼",
                                    color = renderer.getLabelTextColor().copy(alpha = 0.5f)
                                )
                            }

                            if (isComparisonExpanded) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 0.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    // Stat Selection Chips - Wrapped
                                    FlowRow(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalArrangement = Arrangement.spacedBy(6.dp),
                                        maxItemsInEachRow = 3
                                    ) {
                                        comparisonStats.forEach { stat ->
                                            ComparisonChip(
                                                labelRes = stat.labelRes,
                                                isSelected = selectedComparisonStat == stat.id,
                                                renderer = renderer,
                                                onClick = { selectedComparisonStat = stat.id }
                                            )
                                        }
                                    }

                                    // Comparison Table
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 16.dp)
                                    ) {
                                        // Table Header
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(bottom = 8.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = stringResource(R.string.stats_table_mode),
                                                color = renderer.getLabelTextColor(),
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.weight(1f)
                                            )
                                            AutoShrinkText(
                                                text = stringResource(R.string.label_best),
                                                color = renderer.getLabelTextColor(),
                                                style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold),
                                                modifier = Modifier.weight(1f)
                                            )
                                            if (currentComparisonStat.getAverage != null) {
                                                AutoShrinkText(
                                                    text = stringResource(R.string.label_avg),
                                                    color = renderer.getLabelTextColor(),
                                                    style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold),
                                                    modifier = Modifier.weight(1f)
                                                )
                                            }
                                        }

                                        HorizontalDivider(color = renderer.getLabelTextColor().copy(alpha = 0.1f))

                                        // Table Rows
                                        modes.forEach { mode ->
                                            val modeStats = mode.getStats(allGameStatistics)
                                            if (modeStats.totalGamesPlayed > 0) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(vertical = 8.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Text(
                                                        text = stringResource(mode.nameRes),
                                                        color = renderer.getLabelTextColor(),
                                                        fontSize = 14.sp,
                                                        fontWeight = FontWeight.Medium,
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                    AutoShrinkText(
                                                        text = currentComparisonStat.getBest(modeStats),
                                                        color = renderer.getLabelTextColor(),
                                                        style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold),
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                    if (currentComparisonStat.getAverage != null) {
                                                        AutoShrinkText(
                                                            text = currentComparisonStat.getAverage.invoke(modeStats),
                                                            color = renderer.getLabelTextColor().copy(alpha = 0.6f),
                                                            style = TextStyle(fontSize = 14.sp),
                                                            modifier = Modifier.weight(1f)
                                                        )
                                                    }
                                                }
                                                HorizontalDivider(color = renderer.getLabelTextColor().copy(alpha = 0.05f))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // No games played
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = renderer.getCardBackgroundColor()
                        ),
                        border = BorderStroke(1.dp, renderer.getCardBorderColor()),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(48.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = stringResource(id = R.string.stats_no_games_mode, stringResource(id = currentModeInfo.nameRes)),
                                color = renderer.getLabelTextColor().copy(alpha = 0.6f),
                                fontSize = 16.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = stringResource(R.string.stats_start_playing_desc),
                                color = renderer.getLabelTextColor().copy(alpha = 0.4f),
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isFoundry) IndustrialOrange else ButtonPrimary
                ),
                shape = buttonShape,
                border = if (isFoundry) BorderStroke(2.dp, IndustrialOrange) else null
            ) {
                Text(
                    text = stringResource(id = R.string.action_close).let { if (isFoundry) it.uppercase() else it },
                    fontWeight = FontWeight.Bold,
                    color = if (isFoundry) Color.Black else Color.Unspecified
                )
            }
        },
        containerColor = renderer.getOverlayBackgroundColor(),
        shape = dialogShape
    )
}

@Composable
private fun ModeChip(
    @StringRes nameRes: Int,
    isSelected: Boolean,
    renderer: ThemeRenderer,
    onClick: () -> Unit
) {
    val isFoundry = renderer is FoundryThemeRenderer
    val selectionColor = if (isFoundry) IndustrialOrange else ButtonPrimary
    val shape = if (isFoundry) RoundedCornerShape(0.dp) else RoundedCornerShape(8.dp)

    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = {
            Text(
                text = stringResource(id = nameRes).uppercase(),
                fontSize = if (isFoundry) 11.sp else 12.sp,
                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                letterSpacing = if (isFoundry) 1.5.sp else 0.5.sp
            )
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = selectionColor,
            selectedLabelColor = Color.Black,
            containerColor = renderer.getCardBackgroundColor(),
            labelColor = renderer.getLabelTextColor().copy(alpha = 0.7f)
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = isSelected,
            borderColor = renderer.getCardBorderColor(),
            selectedBorderColor = selectionColor,
            borderWidth = if (isFoundry) 2.dp else 1.dp
        ),
        shape = shape
    )
}

@Composable
private fun ComparisonChip(
    @StringRes labelRes: Int,
    isSelected: Boolean,
    renderer: ThemeRenderer,
    onClick: () -> Unit
) {
    val isFoundry = renderer is FoundryThemeRenderer
    val selectionColor = if (isFoundry) IndustrialOrange else Color(0xFF00BCD4)
    val shape = if (isFoundry) RoundedCornerShape(0.dp) else RoundedCornerShape(6.dp)

    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = {
            Text(
                text = stringResource(id = labelRes).uppercase(),
                fontSize = if (isFoundry) 9.sp else 10.sp,
                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                letterSpacing = if (isFoundry) 1.sp else 0.5.sp
            )
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = selectionColor,
            selectedLabelColor = Color.Black,
            containerColor = renderer.getCardBackgroundColor().copy(alpha = 0.3f),
            labelColor = renderer.getLabelTextColor().copy(alpha = 0.7f)
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = isSelected,
            borderColor = renderer.getCardBorderColor(),
            selectedBorderColor = selectionColor,
            borderWidth = if (isFoundry) 1.5.dp else 1.dp
        ),
        shape = shape
    )
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    subValue: String,
    color: Color,
    renderer: ThemeRenderer
) {
    val isFoundry = renderer is FoundryThemeRenderer
    val shape = if (isFoundry) RoundedCornerShape(0.dp) else RoundedCornerShape(12.dp)

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = renderer.getCardBackgroundColor()
        ),
        border = BorderStroke(if (isFoundry) 2.dp else 1.dp, renderer.getCardBorderColor()),
        shape = shape
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Column(horizontalAlignment = Alignment.End) {
                    AutoShrinkText(
                        text = value,
                        style = androidx.compose.ui.text.TextStyle(
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black
                        ),
                        color = color
                    )
                    Text(
                        text = subValue,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = renderer.getLabelTextColor().copy(alpha = 0.5f)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label.uppercase(),
                fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.5.sp,
                color = renderer.getLabelTextColor()
            )
        }
    }
}
