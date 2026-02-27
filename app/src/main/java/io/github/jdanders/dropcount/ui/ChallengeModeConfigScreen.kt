package io.github.jdanders.dropcount.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
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
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .systemBarsPadding()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
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

            Spacer(modifier = Modifier.height(8.dp))

            // Difficulty Selection
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
                verticalArrangement = Arrangement.spacedBy(12.dp)
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

            Spacer(modifier = Modifier.height(24.dp))

            // Start Button
            Button(
                onClick = {
                    onStartGame(GameMode.Challenge(selectedDifficulty))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isFoundry) IndustrialOrange else ButtonPrimary,
                    contentColor = Color.Black
                ),
                shape = if (isFoundry) RoundedCornerShape(0.dp) else RoundedCornerShape(16.dp),
                border = if (isFoundry) BorderStroke(2.dp, ShadowBlack) else null,
                elevation = if (isFoundry) null else ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Text(
                    text = stringResource(R.string.start_challenge).uppercase(),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = if (isFoundry) 2.sp else 1.sp
                )
            }

            // Back Button
            TextButton(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.action_back).uppercase(),
                    color = when {
                        visualTheme == VisualTheme.WOODBLOCK -> WoodblockInk.copy(alpha = 0.8f)
                        isFoundry -> StarkWhite.copy(alpha = 0.5f)
                        else -> Color.White.copy(alpha = 0.6f)
                    },
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = if (isFoundry) 2.sp else 1.sp
                )
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
                .padding(20.dp),
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
