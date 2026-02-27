package io.github.jdanders.dropcount

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.jdanders.dropcount.model.AnimationSpeed
import io.github.jdanders.dropcount.model.GameMode
import io.github.jdanders.dropcount.model.GameStatus
import io.github.jdanders.dropcount.ui.ChallengeModeConfigScreen
import io.github.jdanders.dropcount.ui.GameScreen
import io.github.jdanders.dropcount.ui.MenuScreen
import io.github.jdanders.dropcount.ui.HowToPlayDialog
import io.github.jdanders.dropcount.ui.theme.DropCountTheme
import io.github.jdanders.dropcount.viewmodel.GameViewModel

import androidx.compose.runtime.saveable.rememberSaveable

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DropCountTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    DropCountApp()
                }
            }
        }
    }
}

enum class Screen {
    Menu,
    ChallengeConfig,
    Game
}

@Composable
fun DropCountApp() {
    val viewModel: GameViewModel = viewModel()
    val gameState by viewModel.gameState.collectAsState()
    val allGameStatistics by viewModel.allGameStatistics.collectAsState()
    val animationSpeed by viewModel.animationSpeed.collectAsState<AnimationSpeed>()
    val visualTheme by viewModel.visualTheme.collectAsState()
    val hasSeenTutorial by viewModel.hasSeenTutorial.collectAsState()

    // Determine initial screen based on whether there's a saved game
    var currentScreen by rememberSaveable {
        mutableStateOf(
            if (gameState.status == GameStatus.Playing && gameState.totalDrops > 0) {
                Screen.Game
            } else {
                Screen.Menu
            }
        )
    }

    // Show tutorial automatically on first launch
    if (!hasSeenTutorial) {
        HowToPlayDialog(
            onDismiss = { viewModel.dismissTutorial() },
            visualTheme = visualTheme
        )
    }

    // Update screen if game state changes (e.g., after loading)
    LaunchedEffect(gameState.status, gameState.totalDrops) {
        if (currentScreen == Screen.Menu &&
            gameState.status == GameStatus.Playing &&
            gameState.totalDrops > 0) {
            currentScreen = Screen.Game
        }
    }

    when (currentScreen) {
        Screen.Menu -> {
            MenuScreen(
                onStartGame = { mode ->
                    when (mode) {
                        is GameMode.Challenge -> {
                            // Go to challenge config screen
                            currentScreen = Screen.ChallengeConfig
                        }
                        else -> {
                            // Start game directly with selected mode
                            viewModel.startNewGame(mode)
                            currentScreen = Screen.Game
                        }
                    }
                },
                onResumeGame = if (gameState.status == GameStatus.Paused ||
                                  (gameState.status == GameStatus.Playing && gameState.totalDrops > 0)) {
                    {
                        if (gameState.status == GameStatus.Paused) {
                            viewModel.resumeGame()
                        }
                        currentScreen = Screen.Game
                    }
                } else null,
                allGameStatistics = allGameStatistics,
                animationSpeed = animationSpeed,
                onAnimationSpeedChange = { speed: AnimationSpeed ->
                  viewModel.setAnimationSpeed(speed) },
                visualTheme = visualTheme,
                onVisualThemeChange = { theme -> viewModel.setVisualTheme(theme) }
            )
        }

        Screen.ChallengeConfig -> {
            ChallengeModeConfigScreen(
                onStartGame = { challengeMode ->
                    viewModel.startNewGame(challengeMode)
                    currentScreen = Screen.Game
                },
                onBack = {
                    currentScreen = Screen.Menu
                },
                visualTheme = visualTheme
            )
        }

        Screen.Game -> {
            GameScreen(
                viewModel = viewModel,
                onNavigateToMenu = {
                    currentScreen = Screen.Menu
                }
            )
        }
    }
}
