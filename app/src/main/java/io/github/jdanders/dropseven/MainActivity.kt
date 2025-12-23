package io.github.jdanders.dropseven

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.jdanders.dropseven.model.GameMode
import io.github.jdanders.dropseven.ui.ChallengeModeConfigScreen
import io.github.jdanders.dropseven.ui.GameScreen
import io.github.jdanders.dropseven.ui.MenuScreen
import io.github.jdanders.dropseven.ui.theme.DropSevenTheme
import io.github.jdanders.dropseven.viewmodel.GameViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DropSevenTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    DropSevenApp()
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
fun DropSevenApp() {
    var currentScreen by remember { mutableStateOf(Screen.Menu) }
    val viewModel: GameViewModel = viewModel()
    val gameState by viewModel.gameState.collectAsState()
    
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
                onResumeGame = if (gameState.status == io.github.jdanders.dropseven.model.GameStatus.Paused) {
                    {
                        viewModel.resumeGame()
                        currentScreen = Screen.Game
                    }
                } else null
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
                }
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