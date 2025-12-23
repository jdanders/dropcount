package io.github.jdanders.dropseven.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.jdanders.dropseven.model.GameStatus
import io.github.jdanders.dropseven.ui.components.GameGrid
import io.github.jdanders.dropseven.ui.components.NextDiscPreview
import io.github.jdanders.dropseven.ui.theme.BackgroundDark
import io.github.jdanders.dropseven.ui.theme.ScoreColor
import io.github.jdanders.dropseven.viewmodel.GameViewModel

@Composable
fun GameScreen(
    viewModel: GameViewModel = viewModel(),
    onNavigateToMenu: () -> Unit = {}
) {
    val gameState by viewModel.gameState.collectAsState()
    val highScore by viewModel.highScore.collectAsState()
    val highlightedPositions by viewModel.highlightedPositions.collectAsState()
    
    // Track whether to show the game over overlay
    var showGameOverOverlay by remember { mutableStateOf(false) }
    
    // Update overlay visibility when game status changes
    androidx.compose.runtime.LaunchedEffect(gameState.status) {
        if (gameState.status == GameStatus.GameOver) {
            showGameOverOverlay = true
        } else {
            showGameOverOverlay = false
        }
    }
    
    // Handle back button - intercept it when game over overlay is showing
    BackHandler(enabled = gameState.status == GameStatus.GameOver && showGameOverOverlay) {
        showGameOverOverlay = false
    }
    
    // Handle back button for normal gameplay
    BackHandler(enabled = gameState.status != GameStatus.GameOver || !showGameOverOverlay) {
        if (gameState.status == GameStatus.Playing) {
            viewModel.pauseGame()
        }
        onNavigateToMenu()
    }
    
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
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top bar with score, high score, and level
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Score
                Column {
                    Text(
                        text = "SCORE",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                    Text(
                        text = gameState.score.toString(),
                        color = ScoreColor,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                // High Score
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "HIGH SCORE",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                    Text(
                        text = highScore.toString(),
                        color = Color(0xFFFFD700),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                // Level
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "LEVEL",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                    Text(
                        text = gameState.level.toString(),
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            // Drops until new row indicator
            Text(
                text = "Next row in: ${gameState.dropsUntilNewRow} drops",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            // Chain indicator (always reserve space to prevent layout shift)
            Box(
                modifier = Modifier
                    .height(32.dp)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                if (gameState.currentChain > 1) {
                    Text(
                        text = "CHAIN x${gameState.currentChain}!",
                        color = Color(0xFFFF4444),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Game grid
            GameGrid(
                gameState = gameState,
                onColumnTap = { column ->
                    if (gameState.status == GameStatus.Playing) {
                        viewModel.dropDisc(column)
                    }
                },
                modifier = Modifier.weight(1f),
                highlightedPositions = highlightedPositions
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Next disc preview
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Text(
                    text = "NEXT",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                NextDiscPreview(
                    disc = gameState.nextDisc,
                    size = 80.dp
                )
            }
            
            // Control buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = { viewModel.pauseGame() },
                    enabled = gameState.status == GameStatus.Playing,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2D2D44)
                    )
                ) {
                    Text("PAUSE")
                }
                
                Button(
                    onClick = { viewModel.restartGame() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2D2D44)
                    )
                ) {
                    Text("RESTART")
                }
                
                Button(
                    onClick = onNavigateToMenu,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2D2D44)
                    )
                ) {
                    Text("MENU")
                }
            }
        }
        
        // Game Over overlay (only show if flag is true)
        if (gameState.status == GameStatus.GameOver && showGameOverOverlay) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.8f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null // No ripple effect
                    ) {
                        showGameOverOverlay = false
                    },
                contentAlignment = Alignment.BottomCenter // Position at bottom to keep top visible
            ) {
                Card(
                    modifier = Modifier
                        .padding(32.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            // Prevent clicks on card from dismissing
                        },
                    colors = CardDefaults.cardColors(
                        containerColor = BackgroundDark
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "GAME OVER",
                            color = Color.Red,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "Final Score",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 16.sp
                        )
                        Text(
                            text = gameState.score.toString(),
                            color = ScoreColor,
                            fontSize = 40.sp,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Button(
                                onClick = { viewModel.restartGame() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF4ECDC4)
                                )
                            ) {
                                Text("PLAY AGAIN")
                            }
                            
                            Button(
                                onClick = onNavigateToMenu,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF2D2D44)
                                )
                            ) {
                                Text("MENU")
                            }
                        }
                    }
                }
            }
        }
        
        // Paused overlay
        if (gameState.status == GameStatus.Paused) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.8f)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.padding(32.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = BackgroundDark
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "PAUSED",
                            color = Color.White,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Button(
                                onClick = { viewModel.resumeGame() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF4ECDC4)
                                )
                            ) {
                                Text("RESUME")
                            }
                            
                            Button(
                                onClick = onNavigateToMenu,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF2D2D44)
                                )
                            ) {
                                Text("MENU")
                            }
                        }
                    }
                }
            }
        }
    }
}

