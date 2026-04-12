package io.github.jdanders.dropcount.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.jdanders.dropcount.model.ChallengeDifficulty
import io.github.jdanders.dropcount.model.GameMode
import io.github.jdanders.dropcount.model.VisualTheme
import io.github.jdanders.dropcount.ui.theme.*
import androidx.activity.compose.BackHandler

import androidx.compose.ui.res.stringResource
import io.github.jdanders.dropcount.R
import io.github.jdanders.dropcount.ui.components.AutoShrinkText

@Composable
fun ChallengeModeConfigScreen(
    onStartGame: (GameMode.Challenge) -> Unit,
    onBack: () -> Unit,
    visualTheme: VisualTheme = VisualTheme.CLASSIC
) {
    BackHandler(onBack = onBack)
    var selectedDifficulty by remember { mutableStateOf(ChallengeDifficulty.HARD) }
    val renderer = remember(visualTheme) { visualTheme.createRenderer() }
    val isFoundry = visualTheme == VisualTheme.FOUNDRY

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = renderer.getBackgroundGradient()),
        contentAlignment = Alignment.Center
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val screenH = maxHeight
            val screenW = maxWidth
            val contentPadding = (screenW.value * 0.08f).coerceIn(16f, 32f).dp
            val itemSpacing = (screenH.value * 0.025f).coerceIn(6f, 20f).dp
            val startButtonHeight = (screenH.value * 0.1f).coerceIn(48f, 64f).dp

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding()
                    .padding(horizontal = contentPadding, vertical = contentPadding / 2)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(itemSpacing)
            ) {
                // Title
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = if (isFoundry) Alignment.Start else Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.challenge_config_title).let { if (isFoundry) it.uppercase() else it },
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = if (isFoundry) 2.sp else 1.sp,
                            shadow = if (isFoundry) null else Shadow(
                                color = Color.Black.copy(alpha = 0.5f),
                                offset = Offset(4f, 4f),
                                blurRadius = 8f
                            )
                        ),
                        color = renderer.getLabelTextColor()
                    )
                    if (isFoundry) {
                        Spacer(modifier = Modifier.height(4.dp))
                        HorizontalDivider(color = IndustrialOrange, thickness = 2.dp)
                    }
                }

                // Difficulty Selection label
                Text(
                    text = stringResource(R.string.label_select_difficulty),
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = if (isFoundry) 2.sp else 1.sp
                    ),
                    color = if (isFoundry) StarkWhite.copy(alpha = 0.6f) else renderer.getLabelTextColor()
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(itemSpacing / 2)
                ) {
                    ChallengeDifficulty.entries.forEach { difficulty ->
                        DifficultyCard(
                            difficulty = difficulty,
                            isSelected = selectedDifficulty == difficulty,
                            renderer = renderer,
                            onClick = { selectedDifficulty = difficulty }
                        )
                    }
                }

                // Start Button
                Button(
                    onClick = { onStartGame(GameMode.Challenge(selectedDifficulty)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(startButtonHeight),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isFoundry) IndustrialOrange else ButtonPrimary,
                        contentColor = Color.Black
                    ),
                    shape = if (isFoundry) RoundedCornerShape(0.dp) else RoundedCornerShape(16.dp),
                    border = if (isFoundry) BorderStroke(2.dp, ShadowBlack) else null,
                    elevation = if (isFoundry) null else ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    AutoShrinkText(
                        text = stringResource(R.string.start_challenge).uppercase(),
                        style = LocalTextStyle.current.copy(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = if (isFoundry) 2.sp else 1.sp
                        ),
                        color = Color.Black
                    )
                }

                // Back Button
                TextButton(
                    onClick = onBack,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    AutoShrinkText(
                        text = stringResource(R.string.action_back).uppercase(),
                        style = LocalTextStyle.current.copy(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = if (isFoundry) 2.sp else 1.sp
                        ),
                        color = when {
                            visualTheme == VisualTheme.WOODBLOCK -> WoodblockInk.copy(alpha = 0.8f)
                            isFoundry -> StarkWhite.copy(alpha = 0.5f)
                            else -> Color.White.copy(alpha = 0.6f)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun DifficultyCard(
    difficulty: ChallengeDifficulty,
    isSelected: Boolean,
    renderer: ThemeRenderer,
    onClick: () -> Unit
) {
    val isFoundry = renderer is FoundryThemeRenderer
    val selectionColor = if (isFoundry) IndustrialOrange else ButtonPrimary
    val containerColor = if (isSelected) selectionColor else renderer.getCardBackgroundColor()
    val contentColor = if (isSelected) Color.Black else renderer.getLabelTextColor()
    val borderColor = if (isSelected) selectionColor else renderer.getCardBorderColor()
    val shape = if (isFoundry) RoundedCornerShape(0.dp) else RoundedCornerShape(16.dp)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        ),
        border = BorderStroke(if (isFoundry && isSelected) 3.dp else if (isFoundry) 2.dp else 1.dp, borderColor),
        shape = shape,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = when(difficulty) {
                        ChallengeDifficulty.EASY -> stringResource(R.string.difficulty_easy)
                        ChallengeDifficulty.MEDIUM -> stringResource(R.string.difficulty_medium)
                        ChallengeDifficulty.HARD -> stringResource(R.string.difficulty_hard)
                        ChallengeDifficulty.EXTREME -> stringResource(R.string.difficulty_extreme)
                    }.uppercase(),
                    color = contentColor,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = stringResource(R.string.drops_per_row, difficulty.dropsPerRow).uppercase(),
                    color = contentColor.copy(alpha = 0.7f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }

            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(Color.Black, if (isFoundry) RoundedCornerShape(0.dp) else CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "✓",
                        color = selectionColor,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}
