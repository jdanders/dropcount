package io.github.jdanders.dropseven.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.jdanders.dropseven.model.ChallengeDifficulty
import io.github.jdanders.dropseven.model.GameMode
import io.github.jdanders.dropseven.ui.theme.BackgroundDark

@Composable
fun ChallengeModeConfigScreen(
    onStartGame: (GameMode.Challenge) -> Unit,
    onBack: () -> Unit
) {
    var selectedDifficulty by remember { mutableStateOf(ChallengeDifficulty.HARD) }
    var isDecreasing by remember { mutableStateOf(false) }
    
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Title
            Text(
                text = "Challenge Mode",
                color = Color.White,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Difficulty Selection
            Text(
                text = "Select Difficulty",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 18.sp
            )
            
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ChallengeDifficulty.entries.forEach { difficulty ->
                    DifficultyCard(
                        difficulty = difficulty,
                        isSelected = selectedDifficulty == difficulty,
                        onClick = { selectedDifficulty = difficulty }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Row Timing Toggle
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF2D2D44)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = "Row Timing",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isDecreasing) "Decreasing" else "Fixed",
                                color = Color.White,
                                fontSize = 16.sp
                            )
                            Text(
                                text = if (isDecreasing)
                                    "Count decreases each level"
                                else
                                    "Same count throughout game",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 12.sp
                            )
                        }
                        
                        Switch(
                            checked = isDecreasing,
                            onCheckedChange = { isDecreasing = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFF4ECDC4),
                                checkedTrackColor = Color(0xFF4ECDC4).copy(alpha = 0.5f)
                            )
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Start Button
            Button(
                onClick = {
                    onStartGame(GameMode.Challenge(selectedDifficulty, isDecreasing))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4ECDC4)
                )
            ) {
                Text(
                    text = "START GAME",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            // Back Button
            TextButton(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "BACK",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
private fun DifficultyCard(
    difficulty: ChallengeDifficulty,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFF4ECDC4) else Color(0xFF2D2D44)
        ),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = difficulty.displayName,
                    color = if (isSelected) Color.Black else Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${difficulty.dropsPerRow} drops per row",
                    color = if (isSelected) Color.Black.copy(alpha = 0.7f) else Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp
                )
            }
            
            if (isSelected) {
                Text(
                    text = "✓",
                    color = Color.Black,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

