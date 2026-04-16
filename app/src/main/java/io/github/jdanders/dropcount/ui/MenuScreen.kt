package io.github.jdanders.dropcount.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import io.github.jdanders.dropcount.model.AnimationSpeed
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.geometry.Offset
import io.github.jdanders.dropcount.data.AllGameStatistics
import androidx.compose.ui.graphics.Shadow
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import io.github.jdanders.dropcount.model.GameMode
import io.github.jdanders.dropcount.model.VisualTheme
import io.github.jdanders.dropcount.ui.theme.*
import io.github.jdanders.dropcount.config.UIConfig
import io.github.jdanders.dropcount.config.ThemeConfig
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import io.github.jdanders.dropcount.ui.components.AutoShrinkText

import androidx.compose.ui.res.stringResource
import io.github.jdanders.dropcount.R

@Composable
fun MenuScreen(
    onStartGame: (GameMode) -> Unit,
    onResumeGame: (() -> Unit)? = null,
    allGameStatistics: AllGameStatistics,
    animationSpeed: AnimationSpeed,
    onAnimationSpeedChange: (AnimationSpeed) -> Unit,
    visualTheme: VisualTheme,
    onVisualThemeChange: (VisualTheme) -> Unit
) {
    when (visualTheme) {
        VisualTheme.NEON -> NeonMenuContent(
            onStartGame = onStartGame,
            onResumeGame = onResumeGame,
            allGameStatistics = allGameStatistics,
            visualTheme = visualTheme,
            onVisualThemeChange = onVisualThemeChange,
            animationSpeed = animationSpeed,
            onAnimationSpeedChange = onAnimationSpeedChange
        )
        VisualTheme.FOUNDRY -> FoundryMenuContent(
            onStartGame = onStartGame,
            onResumeGame = onResumeGame,
            allGameStatistics = allGameStatistics,
            visualTheme = visualTheme,
            onVisualThemeChange = onVisualThemeChange,
            animationSpeed = animationSpeed,
            onAnimationSpeedChange = onAnimationSpeedChange
        )
        VisualTheme.WOODBLOCK -> WoodblockMenuContent(
            onStartGame = onStartGame,
            onResumeGame = onResumeGame,
            allGameStatistics = allGameStatistics,
            visualTheme = visualTheme,
            onVisualThemeChange = onVisualThemeChange,
            animationSpeed = animationSpeed,
            onAnimationSpeedChange = onAnimationSpeedChange
        )
        else -> ClassicMenuContent(
            onStartGame = onStartGame,
            onResumeGame = onResumeGame,
            allGameStatistics = allGameStatistics,
            animationSpeed = animationSpeed,
            onAnimationSpeedChange = onAnimationSpeedChange,
            visualTheme = visualTheme,
            onVisualThemeChange = onVisualThemeChange
        )
    }
}

/**
 * A title component that scales its font size based on available width.
 * It will shrink the text to ensure it fits on a single line.
 */
@Composable
private fun ResponsiveTitle(
    text: String,
    style: TextStyle,
    modifier: Modifier = Modifier,
    baseFontSize: Float = 56f,
    color: Color = Color.White
) {
    BoxWithConstraints(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        val initialFontSize = UIConfig.calculateTitleFontSize(maxWidth.value, baseFontSize)
        AutoShrinkText(
            text = text,
            style = style,
            color = color,
            textAlign = TextAlign.Center,
            initialFontSize = initialFontSize,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun ClassicMenuContent(
    onStartGame: (GameMode) -> Unit,
    onResumeGame: (() -> Unit)? = null,
    allGameStatistics: AllGameStatistics,
    animationSpeed: AnimationSpeed,
    onAnimationSpeedChange: (AnimationSpeed) -> Unit,
    visualTheme: VisualTheme,
    onVisualThemeChange: (VisualTheme) -> Unit
) {
    var showStatsDialog by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    var showHowToPlay by remember { mutableStateOf(false) }
    var showNewGameConfirmation by remember { mutableStateOf(false) }
    var pendingGameMode by remember { mutableStateOf<GameMode?>(null) }

    fun requestStartGame(mode: GameMode) {
        if (onResumeGame != null) {
            pendingGameMode = mode
            showNewGameConfirmation = true
        } else {
            onStartGame(mode)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = visualTheme.createRenderer().getBackgroundGradient()
            )
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val screenH = maxHeight
            val screenW = maxWidth
            val contentPadding = (screenW.value * 0.08f).coerceIn(16f, 32f).dp
            val itemSpacing = (screenH.value * 0.025f).coerceIn(8f, 20f).dp

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
                ResponsiveTitle(
                    text = stringResource(R.string.menu_title),
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        shadow = Shadow(
                            color = Color.White.copy(alpha = 0.25f),
                            offset = Offset(0f, 0f),
                            blurRadius = 24f
                        )
                    ),
                    color = Color.White
                )

                // Resume button (if game is paused)
                if (onResumeGame != null) {
                    Button(
                        onClick = onResumeGame,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ButtonPrimary
                        )
                    ) {
                        Text(
                            text = stringResource(R.string.action_resume),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }

                ModeButton(
                    title = stringResource(id = R.string.menu_normal_mode),
                    description = stringResource(id = R.string.menu_normal_desc_classic),
                    onClick = { requestStartGame(GameMode.Normal) }
                )

                // Challenge Mode Button
                ModeButton(
                    title = stringResource(id = R.string.menu_challenge_mode),
                    description = stringResource(id = R.string.menu_challenge_desc_classic),
                    onClick = {
                        requestStartGame(
                            GameMode.Challenge(
                                difficulty = io.github.jdanders.dropcount.model
                                    .ChallengeDifficulty.HARD
                            )
                        )
                    }
                )

                // Sequence Mode Button
                ModeButton(
                    title = stringResource(id = R.string.menu_sequence_mode),
                    description = stringResource(id = R.string.menu_sequence_desc_classic),
                    onClick = { requestStartGame(GameMode.Sequence()) }
                )

                // Stats and Settings side-by-side
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { showStatsDialog = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ButtonSecondary
                        )
                    ) {
                        AutoShrinkText(
                            text = stringResource(R.string.action_stats),
                            style = LocalTextStyle.current.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }

                    Button(
                        onClick = { showSettings = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ButtonSettings
                        )
                    ) {
                        AutoShrinkText(
                            text = stringResource(R.string.action_settings),
                            style = LocalTextStyle.current.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }
                }

                // How To Play - in column flow, no longer absolutely positioned
                Button(
                    onClick = { showHowToPlay = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    AutoShrinkText(
                        text = stringResource(R.string.action_how_to_play),
                        style = LocalTextStyle.current.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold),
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }

    if (showHowToPlay) {
        HowToPlayDialog(
            onDismiss = { showHowToPlay = false },
            visualTheme = visualTheme
        )
    }

    if (showStatsDialog) {
        StatisticsDialog(
            allGameStatistics = allGameStatistics,
            visualTheme = visualTheme,
            onDismiss = { showStatsDialog = false }
        )
    }

    if (showSettings) {
        SettingsDialog(
            currentSpeed = animationSpeed,
            onSpeedChange = onAnimationSpeedChange,
            currentTheme = visualTheme,
            onThemeChange = onVisualThemeChange,
            onDismiss = { showSettings = false }
        )
    }

    if (showNewGameConfirmation) {
        AlertDialog(
            onDismissRequest = { showNewGameConfirmation = false },
            title = { Text(stringResource(R.string.new_game_title), fontWeight = FontWeight.Bold) },
            text = { Text(stringResource(R.string.new_game_message)) },
            confirmButton = {
                Button(
                    onClick = {
                        showNewGameConfirmation = false
                        pendingGameMode?.let { onStartGame(it) }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AlertRedLight)
                ) { Text(stringResource(R.string.action_start_new)) }
            },
            dismissButton = {
                TextButton(onClick = { showNewGameConfirmation = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }
}

@Composable
private fun NeonMenuContent(
    onStartGame: (GameMode) -> Unit,
    onResumeGame: (() -> Unit)? = null,
    allGameStatistics: AllGameStatistics,
    animationSpeed: AnimationSpeed,
    onAnimationSpeedChange: (AnimationSpeed) -> Unit,
    visualTheme: VisualTheme,
    onVisualThemeChange: (VisualTheme) -> Unit
) {
    var showStatsDialog by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    var showHowToPlay by remember { mutableStateOf(false) }
    var showNewGameConfirmation by remember { mutableStateOf(false) }
    var pendingGameMode by remember { mutableStateOf<GameMode?>(null) }

    fun requestStartGame(mode: GameMode) {
        if (onResumeGame != null) {
            pendingGameMode = mode
            showNewGameConfirmation = true
        } else {
            onStartGame(mode)
        }
    }

    // Animated background
    val infiniteTransition = androidx.compose.animation.core.rememberInfiniteTransition()
    val scanLineY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SpaceBlack)
            .drawBehind {
                // Animated scan line
                drawLine(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            CyanGlow.copy(alpha = 0.2f),
                            Color.Transparent
                        )
                    ),
                    start = Offset(0f, size.height * scanLineY),
                    end = Offset(size.width, size.height * scanLineY),
                    strokeWidth = 60f
                )

                // Grid pattern background
                val gridSpacing = 40f
                for (i in 0..(size.width / gridSpacing).toInt()) {
                    drawLine(
                        color = GridLineNeon.copy(alpha = 0.3f),
                        start = Offset(i * gridSpacing, 0f),
                        end = Offset(i * gridSpacing, size.height),
                        strokeWidth = 0.5f
                    )
                }
                for (i in 0..(size.height / gridSpacing).toInt()) {
                    drawLine(
                        color = GridLineNeon.copy(alpha = 0.3f),
                        start = Offset(0f, i * gridSpacing),
                        end = Offset(size.width, i * gridSpacing),
                        strokeWidth = 0.5f
                    )
                }
            },
        contentAlignment = Alignment.Center
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val screenH = maxHeight
            val screenW = maxWidth
            val contentPadding = (screenW.value * 0.08f).coerceIn(16f, 32f).dp
            val itemSpacing = (screenH.value * 0.025f).coerceIn(6f, 24f).dp

        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(horizontal = contentPadding, vertical = contentPadding / 2)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(itemSpacing)
        ) {
            // Title with neon effect
            ResponsiveTitle(
                text = stringResource(R.string.menu_title),
                style = MaterialTheme.typography.displayLarge.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 4.sp,
                    shadow = Shadow(
                        color = CyanGlow,
                        offset = Offset(0f, 0f),
                        blurRadius = 20f
                    )
                ),
                color = Color.White,
                baseFontSize = 48f, // Reduced base size to accommodate 4.sp letter spacing
                modifier = Modifier
                    .drawBehind {
                        // Text underglow
                        drawRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    CyanGlow.copy(alpha = 0.1f),
                                    Color.Transparent
                                )
                            )
                        )
                    }
            )

            Text(
                text = stringResource(R.string.menu_subtitle),
                style = MaterialTheme.typography.labelLarge.copy(
                    letterSpacing = 1.sp
                ),
                color = CyanGlow.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(0.dp)) // spacer replaced by itemSpacing

             // Resume button (if game is paused)
            if (onResumeGame != null) {
                NeonModeButton(
                    title = stringResource(R.string.menu_resume_game),
                    description = stringResource(R.string.menu_resume_desc),
                    accentColor = LimeBeam,
                    onClick = onResumeGame
                )
            }

            // Mode buttons with glass morphism
            NeonModeButton(
                title = stringResource(id = R.string.menu_normal_mode).uppercase(),
                description = stringResource(id = R.string.menu_normal_desc_neon),
                accentColor = CyanGlow,
                onClick = { requestStartGame(GameMode.Normal) }
            )

            NeonModeButton(
                title = stringResource(id = R.string.menu_challenge_mode).uppercase(),
                description = stringResource(id = R.string.menu_challenge_desc_neon),
                accentColor = MagentaPulse,
                onClick = { requestStartGame(GameMode.Challenge(io.github.jdanders.dropcount.model.ChallengeDifficulty.HARD)) }
            )

            NeonModeButton(
                title = stringResource(id = R.string.menu_sequence_mode).uppercase(),
                description = stringResource(id = R.string.menu_sequence_desc_neon),
                accentColor = AmberAlert,
                onClick = { requestStartGame(GameMode.Sequence()) }
            )

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                NeonActionButton(
                    text = stringResource(R.string.action_stats).uppercase(),
                    icon = "▃▆",
                    onClick = { showStatsDialog = true },
                    modifier = Modifier.weight(1f)
                )

                NeonActionButton(
                    text = stringResource(R.string.action_settings).uppercase(),
                    icon = "⚙",
                    onClick = { showSettings = true },
                    modifier = Modifier.weight(1f)
                )
            }
        }
        } // end BoxWithConstraints
    }

    if (showHowToPlay) {
        HowToPlayDialog(
            onDismiss = { showHowToPlay = false },
            visualTheme = visualTheme
        )
    }

    if (showStatsDialog) {
        StatisticsDialog(
            allGameStatistics = allGameStatistics,
            visualTheme = visualTheme,
            onDismiss = { showStatsDialog = false }
        )
    }

    if (showSettings) {
        SettingsDialog(
            currentSpeed = animationSpeed,
            onSpeedChange = onAnimationSpeedChange,
            currentTheme = visualTheme,
            onThemeChange = onVisualThemeChange,
            onDismiss = { showSettings = false }
        )
    }

    if (showNewGameConfirmation) {
        AlertDialog(
            onDismissRequest = { showNewGameConfirmation = false },
            title = { Text(stringResource(R.string.new_game_title), fontWeight = FontWeight.Bold) },
            text = { Text(stringResource(R.string.new_game_message)) },
            confirmButton = {
                Button(
                    onClick = {
                        showNewGameConfirmation = false
                        pendingGameMode?.let { onStartGame(it) }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AlertRedLight)
                ) { Text(stringResource(R.string.action_start_new)) }
            },
            dismissButton = {
                TextButton(onClick = { showNewGameConfirmation = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }
}

@Composable
private fun NeonModeButton(
    title: String,
    description: String,
    accentColor: Color,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 80.dp) // Dynamic height to fit long descriptions
            .scale(if (isPressed) 0.98f else 1f)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    },
                    onTap = { onClick() }
                )
            },
        shape = androidx.compose.foundation.shape.RoundedCornerShape(2.dp), // Sharp corners
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        onClick = onClick // Redundant with pointerInput but good for accessibility
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .border(
                    width = 1.dp,
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            accentColor.copy(alpha = 0.5f),
                            Color.Transparent
                        )
                    ),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(2.dp)
                )
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            MidnightVoid.copy(alpha = 0.4f),
                            MidnightVoid.copy(alpha = 0.6f),
                            MidnightVoid.copy(alpha = 0.4f)
                        )
                    )
                )
                .padding(horizontal = 20.dp, vertical = 10.dp)
        ) {
            // Accent bar
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .heightIn(min = 60.dp)
                    .align(Alignment.CenterStart)
                    .background(accentColor)
                    .drawBehind {
                        // Glow effect
                        drawRect(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    accentColor,
                                    Color.Transparent
                               )
                            )
                        )
                    }
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(start = 24.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    ),
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        letterSpacing = 0.5.sp
                    ),
                    color = accentColor.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun NeonActionButton(
    text: String,
    icon: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(2.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, CyanGlow.copy(alpha = 0.3f))
    ) {
        AutoShrinkText(
            text = "$icon $text",
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.sp
            ),
            color = CyanGlow
        )
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
            containerColor = Color(0xFF24243D)
        ),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
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

@Composable
private fun WoodblockMenuContent(
    onStartGame: (GameMode) -> Unit,
    onResumeGame: (() -> Unit)? = null,
    allGameStatistics: AllGameStatistics,
    animationSpeed: AnimationSpeed,
    onAnimationSpeedChange: (AnimationSpeed) -> Unit,
    visualTheme: VisualTheme,
    onVisualThemeChange: (VisualTheme) -> Unit
) {
    var showStatsDialog by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    var showHowToPlay by remember { mutableStateOf(false) }
    var showNewGameConfirmation by remember { mutableStateOf(false) }
    var pendingGameMode by remember { mutableStateOf<GameMode?>(null) }

    fun requestStartGame(mode: GameMode) {
        if (onResumeGame != null) {
            pendingGameMode = mode
            showNewGameConfirmation = true
        } else {
            onStartGame(mode)
        }
    }

    // Animated brush stroke effect
    val infiniteTransition = androidx.compose.animation.core.rememberInfiniteTransition()
    val brushStrokeAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = androidx.compose.animation.core.FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WoodblockPaper)
            .drawBehind {
                // Paper texture background
                val random = kotlin.random.Random(42)
                
                // Grain
                repeat(ThemeConfig.Woodblock.GRAIN_COUNT * 2) {
                    val x = random.nextFloat() * size.width
                    val y = random.nextFloat() * size.height
                    drawCircle(
                        color = WoodblockInk.copy(alpha = ThemeConfig.Woodblock.GRAIN_ALPHA * 0.8f),
                        radius = random.nextFloat() * ThemeConfig.Woodblock.GRAIN_SIZE,
                        center = Offset(x, y)
                    )
                }

                // Fibers
                repeat(ThemeConfig.Woodblock.LONG_FIBER_COUNT * 2) {
                    val x = random.nextFloat() * size.width
                    val y = random.nextFloat() * size.height
                    val length = 15f + random.nextFloat() * 25f
                    val angle = random.nextFloat() * 360f

                    val endX = x + length * kotlin.math.cos(Math.toRadians(angle.toDouble())).toFloat()
                    val endY = y + length * kotlin.math.sin(Math.toRadians(angle.toDouble())).toFloat()

                    drawLine(
                        color = WoodblockInk.copy(alpha = ThemeConfig.Woodblock.PAPER_FIBER_ALPHA * 0.8f),
                        start = Offset(x, y),
                        end = Offset(endX, endY),
                        strokeWidth = 0.8f
                    )
                }

                // Decorative brush stroke in corner
                val strokePath = Path().apply {
                    moveTo(size.width * 0.85f, 0f)
                    cubicTo(
                        size.width * 0.9f, size.height * 0.1f,
                        size.width * 0.95f, size.height * 0.15f,
                        size.width, size.height * 0.2f
                    )
                }
                drawPath(
                    path = strokePath,
                    color = WoodblockGold.copy(alpha = brushStrokeAlpha),
                    style = Stroke(width = 30f, cap = StrokeCap.Round)
                )
            },
        contentAlignment = Alignment.Center
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val screenH = maxHeight
            val screenW = maxWidth
            val contentPadding = (screenW.value * 0.08f).coerceIn(16f, 32f).dp
            val itemSpacing = (screenH.value * 0.025f).coerceIn(6f, 24f).dp

        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(horizontal = contentPadding, vertical = contentPadding / 2)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(itemSpacing)
        ) {
            // Title with traditional calligraphy style
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ResponsiveTitle(
                    text = stringResource(R.string.menu_title),
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp,
                        shadow = Shadow(
                            color = WoodblockVermilion.copy(alpha = 0.3f),
                            offset = Offset(3f, 3f),
                            blurRadius = 0f
                        )
                    ),
                    color = WoodblockInk,
                    baseFontSize = 52f
                )

                // Decorative seal
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = WoodblockVermilion,
                            shape = androidx.compose.foundation.shape.CircleShape
                        )
                        .padding(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                color = WoodblockPaper,
                                shape = androidx.compose.foundation.shape.CircleShape
                            )
                    )
                }
            }

            // Resume button
            if (onResumeGame != null) {
                WoodblockModeButton(
                    title = stringResource(R.string.menu_resume_game).uppercase(),
                    description = stringResource(R.string.menu_resume_desc),
                    accentColor = WoodblockGold,
                    onClick = onResumeGame
                )
            }

            // Mode buttons
            WoodblockModeButton(
                title = stringResource(R.string.menu_normal_mode).uppercase(),
                description = stringResource(R.string.menu_normal_desc_woodblock),
                accentColor = WoodblockVermilion,
                onClick = { requestStartGame(GameMode.Normal) }
            )

            WoodblockModeButton(
                title = stringResource(R.string.menu_challenge_mode).uppercase(),
                description = stringResource(R.string.menu_challenge_desc_woodblock),
                accentColor = WoodblockIndigo,
                onClick = { requestStartGame(GameMode.Challenge(io.github.jdanders.dropcount.model.ChallengeDifficulty.HARD)) }
            )

            WoodblockModeButton(
                title = stringResource(R.string.menu_sequence_mode).uppercase(),
                description = stringResource(R.string.menu_sequence_desc_woodblock),
                accentColor = WoodblockSage,
                onClick = { requestStartGame(GameMode.Sequence()) }
            )

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                WoodblockActionButton(
                    text = stringResource(R.string.action_stats).uppercase(),
                    onClick = { showStatsDialog = true },
                    modifier = Modifier.weight(1f)
                )

                WoodblockActionButton(
                    text = stringResource(R.string.action_settings).uppercase(),
                    onClick = { showSettings = true },
                    modifier = Modifier.weight(1f)
                )
            }
        }
        } // end BoxWithConstraints
    }

    if (showStatsDialog) {
        StatisticsDialog(
            allGameStatistics = allGameStatistics,
            visualTheme = visualTheme,
            onDismiss = { showStatsDialog = false }
        )
    }

    if (showSettings) {
        SettingsDialog(
            currentSpeed = animationSpeed,
            onSpeedChange = onAnimationSpeedChange,
            currentTheme = visualTheme,
            onThemeChange = onVisualThemeChange,
            onDismiss = { showSettings = false }
        )
    }

    if (showNewGameConfirmation) {
        AlertDialog(
            onDismissRequest = { showNewGameConfirmation = false },
            title = { Text(stringResource(R.string.new_game_title), fontWeight = FontWeight.Bold) },
            text = { Text(stringResource(R.string.new_game_message)) },
            confirmButton = {
                Button(
                    onClick = {
                        showNewGameConfirmation = false
                        pendingGameMode?.let { onStartGame(it) }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AlertRedLight)
                ) { Text(stringResource(R.string.action_start_new)) }
            },
            dismissButton = {
                TextButton(onClick = { showNewGameConfirmation = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }
}

@Composable
private fun WoodblockModeButton(
    title: String,
    description: String,
    accentColor: Color,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 85.dp)
            .scale(if (isPressed) 0.98f else 1f)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    },
                    onTap = { onClick() }
                )
            },
        shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        border = BorderStroke(3.dp, WoodblockInk),
        onClick = onClick
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            accentColor.copy(alpha = 0.15f),
                            WoodblockPaper.copy(alpha = 0.9f),
                            accentColor.copy(alpha = 0.15f)
                        )
                    )
                )
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // Accent bar (brush stroke)
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .heightIn(min = 60.dp)
                    .align(Alignment.CenterStart)
                    .background(accentColor)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(start = 20.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = WoodblockInk
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        letterSpacing = 0.5.sp,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    ),
                    color = WoodblockInk.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun WoodblockActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(52.dp),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = WoodblockIndigo
        ),
        border = BorderStroke(2.dp, WoodblockInk)
    ) {
        AutoShrinkText(
            text = text,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            ),
            color = WoodblockPaper
        )
    }
}

@Composable
private fun FoundryMenuContent(
    onStartGame: (GameMode) -> Unit,
    onResumeGame: (() -> Unit)? = null,
    allGameStatistics: AllGameStatistics,
    animationSpeed: AnimationSpeed,
    onAnimationSpeedChange: (AnimationSpeed) -> Unit,
    visualTheme: VisualTheme,
    onVisualThemeChange: (VisualTheme) -> Unit
) {
    var showStatsDialog by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    var showHowToPlay by remember { mutableStateOf(false) }
    var showNewGameConfirmation by remember { mutableStateOf(false) }
    var pendingGameMode by remember { mutableStateOf<GameMode?>(null) }

    fun requestStartGame(mode: GameMode) {
        if (onResumeGame != null) {
            pendingGameMode = mode
            showNewGameConfirmation = true
        } else {
            onStartGame(mode)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = visualTheme.createRenderer().getBackgroundGradient()
            )
            .drawBehind {
                // Foundry structural grid
                val renderer = visualTheme.createRenderer()
                renderer.apply {
                     drawGridBackground(size, 50f)
                     val cellSize = 50f
                     val cols = (size.width / cellSize).toInt()
                     val rows = (size.height / cellSize).toInt()

                     for (i in 0..cols) {
                        val pos = i * cellSize
                         drawLine(
                            color = GridStroke.copy(alpha = 0.5f),
                            start = Offset(pos, 0f),
                            end = Offset(pos, size.height),
                            strokeWidth = 2f
                        )
                    }
                    for (i in 0..rows) {
                        val pos = i * cellSize
                         drawLine(
                            color = GridStroke.copy(alpha = 0.5f),
                            start = Offset(0f, pos),
                            end = Offset(size.width, pos),
                            strokeWidth = 2f
                        )
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val screenH = maxHeight
            val screenW = maxWidth
            val contentPadding = (screenW.value * 0.08f).coerceIn(16f, 32f).dp
            val itemSpacing = (screenH.value * 0.025f).coerceIn(6f, 24f).dp

        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(horizontal = contentPadding, vertical = contentPadding / 2)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(itemSpacing)
        ) {
            // Title - Structural/Stenciled look
            ResponsiveTitle(
                text = stringResource(R.string.menu_title),
                style = MaterialTheme.typography.displayLarge.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-2).sp,
                    shadow = Shadow(
                        color = IndustrialOrange.copy(alpha = 0.3f),
                        offset = Offset(0f, 0f),
                        blurRadius = 16f
                    )
                ),
                color = StarkWhite
            )

            Text(
                text = stringResource(R.string.foundry_branding),
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                ),
                color = IndustrialOrange
            )

             // Resume button
            if (onResumeGame != null) {
                FoundryModeButton(
                    title = stringResource(R.string.menu_resume_game).uppercase(),
                    description = stringResource(R.string.menu_resume_desc).uppercase(),
                    onClick = onResumeGame,
                    isPrimary = true
                )
            }

            // Mode buttons
            FoundryModeButton(
                title = stringResource(R.string.menu_normal_mode).uppercase(),
                description = stringResource(R.string.menu_normal_desc_foundry),
                onClick = { requestStartGame(GameMode.Normal) }
            )

            FoundryModeButton(
                title = stringResource(R.string.menu_challenge_mode).uppercase(),
                description = stringResource(R.string.menu_challenge_desc_foundry),
                onClick = { requestStartGame(GameMode.Challenge(io.github.jdanders.dropcount.model.ChallengeDifficulty.HARD)) }
            )

            FoundryModeButton(
                title = stringResource(R.string.menu_sequence_mode).uppercase(),
                description = stringResource(R.string.menu_sequence_desc_foundry),
                onClick = { requestStartGame(GameMode.Sequence()) }
            )

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FoundryActionButton(
                    text = stringResource(R.string.action_stats).uppercase(),
                    onClick = { showStatsDialog = true },
                    modifier = Modifier.weight(1f)
                )

                FoundryActionButton(
                    text = stringResource(R.string.action_settings).uppercase(),
                    onClick = { showSettings = true },
                    modifier = Modifier.weight(1f)
                )
            }
        }
        } // end BoxWithConstraints
    }

    if (showStatsDialog) {
        StatisticsDialog(
            allGameStatistics = allGameStatistics,
            visualTheme = visualTheme,
            onDismiss = { showStatsDialog = false }
        )
    }

    if (showSettings) {
        SettingsDialog(
            currentSpeed = animationSpeed,
            onSpeedChange = onAnimationSpeedChange,
            currentTheme = visualTheme,
            onThemeChange = onVisualThemeChange,
            onDismiss = { showSettings = false }
        )
    }

    if (showNewGameConfirmation) {
        AlertDialog(
            onDismissRequest = { showNewGameConfirmation = false },
            title = { Text(stringResource(R.string.new_game_title), fontWeight = FontWeight.Bold) },
            text = { Text(stringResource(R.string.new_game_message)) },
            confirmButton = {
                Button(
                    onClick = {
                        showNewGameConfirmation = false
                        pendingGameMode?.let { onStartGame(it) }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AlertRedLight)
                ) { Text(stringResource(R.string.action_start_new)) }
            },
            dismissButton = {
                TextButton(onClick = { showNewGameConfirmation = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }
}

@Composable
private fun FoundryModeButton(
    title: String,
    description: String,
    onClick: () -> Unit,
    isPrimary: Boolean = false
) {
    val borderColor = if (isPrimary) IndustrialOrange else GridStroke
    val textColor = if (isPrimary) IndustrialOrange else StarkWhite

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 80.dp),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(0.dp), // Sharp corners
        colors = CardDefaults.cardColors(
            containerColor = ConcreteMid
        ),
        border = BorderStroke(2.dp, borderColor),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                ),
                color = textColor
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                ),
                color = StarkWhite.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun FoundryActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(56.dp),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(0.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = ConcreteMid
        ),
        border = BorderStroke(2.dp, GridStroke)
    ) {
        AutoShrinkText(
            text = text,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            ),
            color = StarkWhite
        )
    }
}
