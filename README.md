# Drop Seven - Android Game

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
- **High Score Tracking**: Per-mode high score persistence using DataStore
- **Haptic Feedback**: Vibration feedback for drops, breaks, and chains
- **Sound Support**: Infrastructure ready for sound effects (add audio files to `res/raw/`)
- **Beautiful UI**: Modern Material 3 design with gradient backgrounds
- **Animations**: Smooth disc drops and break effects
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
app/src/main/java/io/github/jdanders/dropseven/
├── model/              # Data models
│   ├── GameState.kt    # Game state management
│   ├── Disc.kt         # Disc types (Numbered, Solid)
│   ├── Cell.kt         # Grid cell representation
│   └── GameMode.kt     # Game mode configurations
│
├── engine/             # Game logic
│   ├── GameEngine.kt   # Core game mechanics
│   └── DiscGenerator.kt # Mode-based disc generation
│
├── viewmodel/          # ViewModels
│   └── GameViewModel.kt
│
├── ui/                 # Compose UI
│   ├── GameScreen.kt   # Main game interface
│   ├── MenuScreen.kt   # Mode selection menu
│   ├── ChallengeModeConfigScreen.kt
│   ├── components/     # Reusable components
│   │   ├── GameGrid.kt
│   │   └── NextDiscPreview.kt
│   ├── animations/     # Animation utilities
│   │   ├── DiscAnimations.kt
│   │   └── ParticleEffect.kt
│   ├── haptics/        # Haptic feedback
│   │   └── HapticsManager.kt
│   └── theme/          # Material theme
│
├── data/               # Data persistence
│   ├── PreferencesManager.kt
│   └── ScoreRepository.kt
│
└── sound/              # Audio (ready for integration)
    └── SoundManager.kt
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
- **Game Engine**: 95% instruction coverage, 87% branch coverage
- **Game Models**: 81% instruction coverage, 72% branch coverage
- **111 total unit tests** covering:
  - Core game mechanics
  - Chain reaction logic
  - Contiguous counting rules
  - Solid disc state transitions
  - New row generation
  - Game mode configurations
  - Regression tests for all fixed bugs

## Adding Sound Effects

To add sound effects:

1. Create `app/src/main/res/raw/` directory
2. Add audio files (`.ogg` or `.mp3` format):
   - `drop_sound.ogg` - Disc drop sound
   - `break_sound.ogg` - Disc break sound
   - `chain_sound.ogg` - Chain reaction sound
   - `level_up_sound.ogg` - Level up sound
   - `game_over_sound.ogg` - Game over sound
3. Uncomment the load statements in `SoundManager.kt`
4. Integrate with `GameViewModel` to trigger sounds

## Future Enhancements

- [ ] Add background music
- [ ] Implement settings screen for sound/haptics toggles
- [ ] Add tutorial/help overlay
- [ ] Create custom animations for chain reactions
- [ ] Add statistics tracking (games played, average score, etc.)
- [ ] Implement daily challenges
- [ ] Add color themes
- [ ] Create widget for quick play
- [ ] Add achievements system

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

This is a personal recreation project for learning purposes.

## Credits

Inspired by the original Drop7 game by Area/Code Entertainment (acquired by Zynga).

