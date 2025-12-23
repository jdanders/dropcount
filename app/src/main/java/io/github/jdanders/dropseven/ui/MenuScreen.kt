package io.github.jdanders.dropseven.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.jdanders.dropseven.model.GameMode
import io.github.jdanders.dropseven.ui.theme.BackgroundDark

@Composable
fun MenuScreen(
    onStartGame: (GameMode) -> Unit,
    onResumeGame: (() -> Unit)? = null
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        BackgroundDark,
                        Color(0xFF0F0F1E)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Title
            Text(
                text = "DROP SEVEN",
                color = Color.White,
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Resume button (if game is paused)
            if (onResumeGame != null) {
                Button(
                    onClick = onResumeGame,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4ECDC4)
                    )
                ) {
                    Text(
                        text = "RESUME GAME",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            } else {
                Spacer(modifier = Modifier.height(32.dp))
            }
            
            // Mode selection text
            Text(
                text = if (onResumeGame != null) "Or Start New Game" else "Select Game Mode",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 18.sp
            )
            
            // Normal Mode Button
            ModeButton(
                title = "Normal Mode",
                description = "30→29→28... drops per row\nMixed colored & gray discs",
                onClick = { onStartGame(GameMode.Normal) }
            )
            
            // Challenge Mode Button
            ModeButton(
                title = "Challenge Mode",
                description = "Configure difficulty\nColored discs only",
                onClick = { onStartGame(GameMode.Challenge(
                    difficulty = io.github.jdanders.dropseven.model.ChallengeDifficulty.HARD,
                    isDecreasing = false
                )) }
            )
            
            // Sequence Mode Button
            ModeButton(
                title = "Sequence Mode",
                description = "Deterministic puzzle mode\nSame as Normal but predictable",
                onClick = { onStartGame(GameMode.Sequence()) }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Help text
            Text(
                text = "Tap a column to drop a disc\nMatch the number with row/column count to break",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}

@Composable
private fun ModeButton(
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2D2D44)
        ),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = description,
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 14.sp
            )
        }
    }
}

