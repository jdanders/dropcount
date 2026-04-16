package io.github.jdanders.dropcount.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import io.github.jdanders.dropcount.model.VisualTheme
import io.github.jdanders.dropcount.ui.components.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.jdanders.dropcount.config.GameConfig
import io.github.jdanders.dropcount.config.ThemeConfig
import io.github.jdanders.dropcount.config.UIConfig
import io.github.jdanders.dropcount.model.AnimationState
import io.github.jdanders.dropcount.model.GameStatus
import io.github.jdanders.dropcount.ui.components.DropsRemainingIndicator
import io.github.jdanders.dropcount.ui.components.GameGrid
import io.github.jdanders.dropcount.ui.components.GameStatisticsDisplay
import io.github.jdanders.dropcount.ui.components.LevelUpAnimation
import io.github.jdanders.dropcount.ui.theme.*
import io.github.jdanders.dropcount.viewmodel.GameViewModel

import androidx.compose.ui.res.stringResource
import io.github.jdanders.dropcount.R
import java.text.NumberFormat

@Composable
fun GameScreen(
    viewModel: GameViewModel = viewModel(),
    onNavigateToMenu: () -> Unit = {}
) {
    val gameState by viewModel.gameState.collectAsState()
    val highScore by viewModel.highScore.collectAsState()
    val animationState by viewModel.animationState.collectAsState()
    val hoveredColumn by viewModel.hoveredColumn.collectAsState()
    val isAnimating by viewModel.isAnimating.collectAsState()
    val currentModeStatistics by viewModel.currentModeStatistics.collectAsState()
    val canUndo by viewModel.canUndo.collectAsState()
    val floatingPoints by viewModel.floatingPoints.collectAsState()
    val levelUpBonus by viewModel.levelUpBonus.collectAsState()
    val boardClearBonus by viewModel.boardClearBonus.collectAsState()
    val animationSpeed by viewModel.animationSpeed.collectAsState()
    val visualTheme by viewModel.visualTheme.collectAsState()
    val chainSummary by viewModel.chainSummary.collectAsState()

    var showGameOverOverlay by remember { mutableStateOf(false) }
    var showRestartConfirmation by remember { mutableStateOf(false) }
    val fmt = NumberFormat.getNumberInstance()

    androidx.compose.runtime.LaunchedEffect(gameState.status) {
        if (gameState.status == GameStatus.GameOver) {
            kotlinx.coroutines.delay(GameConfig.GAME_OVER_OVERLAY_DELAY_MS)
            showGameOverOverlay = true
        } else {
            showGameOverOverlay = false
        }
    }

    BackHandler(enabled = gameState.status == GameStatus.GameOver && showGameOverOverlay) {
        showGameOverOverlay = false
    }

    BackHandler(enabled = (gameState.status != GameStatus.GameOver || !showGameOverOverlay) && !isAnimating) {
        if (gameState.status == GameStatus.Playing) {
            viewModel.pauseGame()
        }
        onNavigateToMenu()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = visualTheme.createRenderer().getBackgroundGradient()
            )
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
        ) {
            val screenH = maxHeight
            val screenW = maxWidth
            // Scale padding and spacing with screen size
            val outerPadding = (screenW.value * 0.04f).coerceIn(8f, 16f).dp
            val hudBottomPadding = (screenH.value * 0.02f).coerceIn(4f, 16f).dp
            val chainIndicatorHeight = (screenH.value * 0.1f).coerceIn(40f, 64f).dp
            val belowGridSpacer = (screenH.value * 0.02f).coerceIn(4f, 24f).dp
            val hudFontScale = (screenW.value / 360f).coerceIn(0.6f, 1f)
            val hudLabelSp = (12f * hudFontScale).sp
            val hudScoreSp = (24f * hudFontScale).sp
            val hudHighScoreSp = (20f * hudFontScale).sp

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(outerPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = hudBottomPadding),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.label_score),
                        color = visualTheme.createRenderer().getLabelTextColor(),
                        fontSize = hudLabelSp
                    )
                    AutoShrinkText(
                        text = fmt.format(gameState.score),
                        color = if (visualTheme == VisualTheme.NEON) visualTheme.createRenderer().getScoreColor() else visualTheme.createRenderer().getLabelTextColor(),
                        style = LocalTextStyle.current.copy(fontSize = hudScoreSp, fontWeight = FontWeight.Bold)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stringResource(R.string.label_high_score),
                        color = visualTheme.createRenderer().getLabelTextColor(),
                        fontSize = hudLabelSp
                    )
                    AutoShrinkText(
                        text = fmt.format(highScore),
                        color = if (visualTheme == VisualTheme.NEON) visualTheme.createRenderer().getHighScoreColor() else visualTheme.createRenderer().getLabelTextColor(),
                        style = LocalTextStyle.current.copy(fontSize = hudHighScoreSp, fontWeight = FontWeight.Bold)
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = stringResource(R.string.label_level),
                        color = visualTheme.createRenderer().getLabelTextColor(),
                        fontSize = hudLabelSp
                    )
                    AutoShrinkText(
                        text = fmt.format(gameState.level),
                        color = if (visualTheme == VisualTheme.NEON) visualTheme.createRenderer().getScoreColor() else visualTheme.createRenderer().getLabelTextColor(),
                        style = LocalTextStyle.current.copy(fontSize = hudScoreSp, fontWeight = FontWeight.Bold)
                    )
                }
            }

            // Chain indicator
            Column(
                modifier = Modifier
                    .height(chainIndicatorHeight)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {

                // Temporary chain summary (shown after drop) with animations
                AnimatedVisibility(
                    visible = chainSummary != null,
                    enter = fadeIn(animationSpec = spring(stiffness = Spring.StiffnessLow)) + 
                            scaleIn(initialScale = 0.8f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)),
                    exit = fadeOut() + scaleOut(targetScale = 0.8f)
                ) {
                    chainSummary?.let { cs ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            // The "CHAIN xN" Part (Line 1)
                            Text(
                                text = stringResource(R.string.label_multiplier, cs.first),
                                color = AlertRed,
                                fontSize = (16f * hudFontScale).sp,
                                fontWeight = FontWeight.Bold
                            )
                            
                            // The Score Points (Line 2)
                            Text(
                                text = stringResource(R.string.label_score_increment, fmt.format(cs.second)),
                                color = ScoreColor,
                                fontSize = (24f * hudFontScale).sp,
                                fontWeight = FontWeight.Black,
                                style = LocalTextStyle.current.copy(
                                    shadow = androidx.compose.ui.graphics.Shadow(
                                        color = Color.Black.copy(alpha = 0.3f),
                                        offset = androidx.compose.ui.geometry.Offset(2f, 2f),
                                        blurRadius = 4f
                                    )
                                )
                            )
                        }
                    }
                }
            }

            // Touch-enabled area covering everything from grid down to buttons
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                val density = LocalDensity.current
                val availableWidth = maxWidth
                val availableHeight = maxHeight

                // Reserve space for indicator + level-up animation, scaled to screen
                val reservedHeight = (availableHeight * 0.18f).coerceIn(60.dp, 100.dp)

                // Grid is 7x7, plus 2 cells on top for preview = 7x9
                // We want the largest cellSize that fits both width and height
                val cellSize = remember(availableWidth, availableHeight) {
                    val widthBased = availableWidth / GameConfig.GRID_SIZE.toFloat()
                    val heightBased = (availableHeight - reservedHeight) / 9f
                    if (widthBased < heightBased) widthBased else heightBased
                }

                val gridWidth = cellSize * GameConfig.GRID_SIZE

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(animationState, gameState.status) {
                            awaitPointerEventScope {
                                // Local touch state per gesture - can't get stuck
                                var localIsTouching = false

                                while (true) {
                                    val event = awaitPointerEvent()

                                    // Check if we can process input
                                    val canProcessInput = animationState is AnimationState.Idle &&
                                                         gameState.status == GameStatus.Playing

                                    event.changes.forEach { change ->
                                        if (canProcessInput) {
                                            // Get touch position relative to the grid center
                                            // Since we are in a centered Column, we need to adjust
                                            val centerX = size.width / 2f
                                            val gridWidthPx = gridWidth.toPx()
                                            val gridLeft = centerX - gridWidthPx / 2f

                                            val touchX = change.position.x - gridLeft
                                            val cellSizePx = cellSize.toPx()

                                            val column = (touchX / cellSizePx).toInt()
                                                .coerceIn(0, GameConfig.GRID_SIZE - 1)

                                            when {
                                                change.pressed -> {
                                                    // Touch down - update hover
                                                    localIsTouching = true
                                                    viewModel.setHoveredColumn(column)
                                                }
                                                localIsTouching -> {
                                                    // Touch up while we were tracking - drop disc
                                                    viewModel.dropDisc(column)
                                                    localIsTouching = localIsTouching && change.pressed
                                                    viewModel.setHoveredColumn(null)
                                                    change.consume()
                                                }
                                            }
                                        } else if (!change.pressed) {
                                            // Can't process input and finger lifted - clear state
                                            localIsTouching = false
                                            viewModel.setHoveredColumn(null)
                                        }
                                    }
                                }
                            }
                        },
                    verticalArrangement = Arrangement.Center, // Center grid vertically
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Game grid - centered in the touch-enabled area
                    GameGrid(
                        gameState = gameState,
                        onColumnTap = { column -> viewModel.dropDisc(column) },
                        animationState = animationState,
                        hoveredColumn = hoveredColumn,
                        onHoveredColumnChange = { column ->
                            if (!isAnimating && gameState.status == GameStatus.Playing) {
                                viewModel.setHoveredColumn(column)
                            }
                        },
                        floatingPoints = floatingPoints,
                        animationSpeed = animationSpeed,
                        visualTheme = visualTheme,
                        modifier = Modifier.size(gridWidth, cellSize * 9)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    DropsRemainingIndicator(
                        dropsRemaining = gameState.dropsUntilNewRow,
                        maxDrops = gameState.baseDropsPerRow,
                        themeRenderer = visualTheme.createRenderer(),
                        modifier = Modifier.width(gridWidth)
                    )

                    // Level-up bonus animation (below grid)
                    Box(
                        modifier = Modifier
                            .width(gridWidth)
                            .height(60.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        levelUpBonus?.let { bonus ->
                            LevelUpAnimation(
                                bonusPoints = bonus,
                                onAnimationComplete = { }
                            )
                        }
                        
                        boardClearBonus?.let { bonus ->
                            LevelUpAnimation(
                                bonusPoints = bonus,
                                onAnimationComplete = { },
                                durationMillis = UIConfig.BOARD_CLEAR_ANIMATION_DURATION,
                                delayMillis = UIConfig.BOARD_CLEAR_ANIMATION_DELAY,
                                textFormatResId = R.string.board_clear_bonus_format
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(belowGridSpacer))

            // Control buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = { viewModel.undoLastMove() },
                    enabled = canUndo && (gameState.status == GameStatus.Playing || (gameState.status == GameStatus.GameOver && !showGameOverOverlay)) && !isAnimating,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentOrange,
                        contentColor = Color.White,
                        disabledContainerColor = AccentOrange,
                        disabledContentColor = Color.White
                    )
                ) {
                    AutoShrinkText(stringResource(R.string.action_undo), style = LocalTextStyle.current.copy(fontWeight = FontWeight.Bold, fontSize = 14.sp), color = Color.White)
                }

                Button(
                    onClick = { showRestartConfirmation = true },
                    enabled = !isAnimating,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AlertRedLight,
                        contentColor = Color.White,
                        disabledContainerColor = AlertRedLight,
                        disabledContentColor = Color.White
                    )
                ) {
                    AutoShrinkText(stringResource(R.string.action_restart), style = LocalTextStyle.current.copy(fontWeight = FontWeight.Bold, fontSize = 14.sp), color = Color.White)
                }

                Button(
                    onClick = {
                        if (gameState.status == GameStatus.Playing) {
                            viewModel.pauseGame()
                        }
                        onNavigateToMenu()
                    },
                    enabled = !isAnimating,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ButtonSecondary,
                        contentColor = Color.White,
                        disabledContainerColor = ButtonSecondary,
                        disabledContentColor = Color.White
                    )
                ) {
                    AutoShrinkText(stringResource(R.string.action_menu), style = LocalTextStyle.current.copy(fontWeight = FontWeight.Bold, fontSize = 14.sp), color = Color.White)
                }
            }
        }
        } // end BoxWithConstraints

        // Restart Confirmation Dialog
        if (showRestartConfirmation) {
            AlertDialog(
                onDismissRequest = { showRestartConfirmation = false },
                title = {
                    Text(
                        text = stringResource(R.string.restart_title),
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(stringResource(R.string.restart_message))
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showRestartConfirmation = false
                            viewModel.restartGame()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AlertRedLight
                        )
                    ) {
                        Text(stringResource(R.string.action_restart))
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showRestartConfirmation = false }
                    ) {
                        Text(stringResource(R.string.action_cancel))
                    }
                },
                containerColor = visualTheme.createRenderer().getOverlayBackgroundColor()
            )
        }

        // Game Over overlay
        if (gameState.status == GameStatus.GameOver && showGameOverOverlay) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = ThemeConfig.Overlay.ALPHA))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        showGameOverOverlay = false
                    },
                contentAlignment = Alignment.BottomCenter
            ) {
                Card(
                    modifier = Modifier
                        .padding(32.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { },
                    colors = CardDefaults.cardColors(
                        containerColor = visualTheme.createRenderer().getOverlayBackgroundColor()
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .verticalScroll(rememberScrollState())
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.game_over),
                            color = Color.Red,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = stringResource(R.string.final_score),
                            color = visualTheme.createRenderer().getLabelTextColor(),
                            fontSize = 16.sp
                        )
                        AutoShrinkText(
                            text = fmt.format(gameState.score),
                            color = if (visualTheme == VisualTheme.NEON) visualTheme.createRenderer().getScoreColor() else visualTheme.createRenderer().getLabelTextColor(),
                            style = LocalTextStyle.current.copy(fontSize = 40.sp, fontWeight = FontWeight.Bold)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        GameStatisticsDisplay(
                            statistics = currentModeStatistics,
                            currentScore = gameState.score,
                            currentLevel = gameState.level,
                            currentChainLength = gameState.longestChain,
                            currentSingleMove = gameState.highestSingleScore,
                            modifier = Modifier.fillMaxWidth(),
                            visualTheme = visualTheme
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Button(
                                onClick = {
                                    showGameOverOverlay = false
                                    viewModel.restartGame()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ButtonPrimary
                                )
                            ) {
                                AutoShrinkText(stringResource(R.string.action_play_again), style = LocalTextStyle.current.copy(fontSize = 14.sp), color = Color.White)
                            }

                            Button(
                                onClick = onNavigateToMenu,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ButtonPrimary
                                )
                            ) {
                                AutoShrinkText(stringResource(R.string.action_menu), style = LocalTextStyle.current.copy(fontSize = 14.sp), color = Color.White)
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
                    .background(Color.Black.copy(alpha = ThemeConfig.Overlay.ALPHA)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.padding(32.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = visualTheme.createRenderer().getOverlayBackgroundColor()
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.paused),
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
                                    containerColor = ButtonPrimary
                                )
                            ) {
                                AutoShrinkText(stringResource(R.string.action_resume), style = LocalTextStyle.current.copy(fontSize = 14.sp), color = Color.White)
                            }

                            Button(
                                onClick = onNavigateToMenu,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CardBackground
                                )
                            ) {
                                AutoShrinkText(stringResource(R.string.action_menu), style = LocalTextStyle.current.copy(fontSize = 14.sp), color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}
