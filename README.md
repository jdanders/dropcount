# DropCount - Android Game

A recreation of the classic Drop7 puzzle game for Android, built with Kotlin and Jetpack Compose.

## Features

### ✨ Core Gameplay
- **7×7 Grid**: Classic Drop7 gameplay with numbered and solid discs
- **Chain Reactions**: Create cascading combos for higher scores
- **Solid Disc Mechanics**: Three-state system (solid → cracked → revealed)
- **Dynamic Difficulty**: New rows appear from the bottom based on game mode

### 🎮 Game Modes

1. **Normal Mode**
   - New row every 30 drops, then 29, 28, 27... (decreasing)
   - 70% colored (numbered) discs, 30% gray (solid) discs
   - Classic Drop7 experience

2. **Challenge Mode**
   - Configurable difficulty presets:
     - Easy: 10 drops per row
     - Medium: 7 drops per row
     - Hard: 5 drops per row
     - Extreme: 3 drops per row
   - Choose Fixed or Decreasing row timing
   - Colored discs only (no solid discs)
   - High-pressure gameplay

3. **Sequence Mode**
   - Same rules as Normal mode
   - Deterministic disc generation for puzzle-solving
   - Predictable sequences for strategic planning

### 🏆 Features
- **Visual Themes**: Choose from Classic, Neon, Woodblock, and Foundry themes
- **High Score & Stats**: Track per-mode high scores and detailed lifetime statistics
- **Undo Support**: Mistake? Undo the last 5 moves
- **Animations**: Smooth disc drops and break effects with adjustable speed
- **Tutorial**: Interactive "How to Play" orientation for new players
- **Beautiful UI**: Modern Material 3 design with theme-specific aesthetics
- **Pause/Resume**: Pause the game anytime

## How to Play

1. **Tap a column** to drop the current disc
2. **Match numbers**: A disc breaks when contiguous discs in its row OR column equal its number
3. **Chain reactions**: Breaking discs can trigger more breaks for bonus points
4. **Solid discs**:
   - Appear solid gray when dropped
   - Crack once after first adjacent break
   - Reveal a number after second adjacent break
5. **Survive**: Avoid filling the top row when a new row appears

## Technical Architecture

### MVVM Pattern
- **Model**: Game state and logic (`GameState`, `Disc`, `Cell`, `GameMode`)
- **View**: Jetpack Compose UI components (`GameScreen`, `GameGrid`, `MenuScreen`)
- **ViewModel**: `GameViewModel` manages state and user actions

### Key Components

```
app/src/main/java/io/github/jdanders/dropcount/
├── model/              # Data models (Disc, Grid, GameState)
├── engine/             # Game logic (GameEngine, DiscGenerator)
├── viewmodel/          # ViewModels (GameViewModel)
├── data/               # Data persistence (DataStore, Repositories)
├── config/             # Game and Animation configurations
├── util/               # Logging and utility functions
└── ui/                 # Compose UI
    ├── theme/          # Theme definitions and Renderers
    ├── components/     # Reusable UI elements (GameGrid, Stats)
    ├── GameScreen.kt   # Main game interface
    ├── MenuScreen.kt   # Mode selection menu
    ├── SettingsDialog.kt # Theme and speed settings
    └── HowToPlayScreen.kt # Tutorial content
```

## Building and Running

### Prerequisites
- Android Studio (latest version recommended)
- Android SDK API 24+ (Android 7.0+)
- Kotlin 2.0.21+

### Setup
1. Open the project in Android Studio
2. Sync Gradle files
3. Run on emulator or physical device

### Using with Cursor
1. Open the project folder in Cursor for code editing
2. Use Android Studio for building and running
3. Leverage Cursor's AI for refactoring and enhancements

## Testing

The project includes comprehensive unit tests with excellent coverage of core game logic:

### Running Tests
```bash
./gradlew test
```

### Code Coverage Report
```bash
./gradlew test jacocoTestReport
open app/build/reports/jacoco/jacocoTestReport/html/index.html
```

### Test Coverage
- **Game Engine**: 85.6% instruction coverage, 77.4% branch coverage
- **Game Models**: 70.4% instruction coverage, 69.1% branch coverage
- **137 total unit tests** covering:
  - Core game mechanics
  - Chain reaction logic
  - Contiguous counting rules
  - Solid disc state transitions
  - New row generation
  - Game mode configurations
  - Regression tests for all fixed bugs

## Game Rules

### Breaking Mechanics
- A numbered disc (1-7) breaks when:
  - The number of **contiguous** discs in its row equals its number, OR
  - The number of **contiguous** discs in its column equals its number
- Contiguous means consecutive discs with no gaps (empty cells break the chain)

### Solid Disc States
1. **Solid Gray** (0 cracks): Initial state
2. **Cracked Gray** (1 crack): After first adjacent break
3. **Numbered** (revealed): After second adjacent break

### Scoring
- Base points: 7 points per disc
- Chain multiplier: Score multiplied by chain level
- Example: 3-disc chain = 3 × (3 × 7) = 63 points

### Game Over
- Game ends when a new disc cannot fit in the selected column
- New rows appear from the bottom based on mode timing
- Top row must be clear when a new row appears

## License

This project is licensed under the GPL v3 License - see the [LICENSE](LICENSE) file for details.

## Privacy Policy

We value your privacy. This app does not collect any personal data. See our [Privacy Policy](PRIVACY_POLICY.md) for details.

## Credits

Inspired by the original Drop7 game by Area/Code Entertainment (acquired by Zynga).
