package io.github.jdanders.dropcount.config

/**
 * Central configuration for all game constants and magic numbers.
 * This ensures consistency across the codebase and makes tuning easier.
 */
object GameConfig {

    // ===== Grid Configuration =====
    /**
     * The size of the game grid (both width and height).
     */
    const val GRID_SIZE = 7

    /**
     * Number of rows in the grid (same as GRID_SIZE for square grid).
     */
    const val GRID_ROWS = GRID_SIZE

    /**
     * Number of columns in the grid (same as GRID_SIZE for square grid).
     */
    const val GRID_COLS = GRID_SIZE

    // ===== Initial Game State Configuration =====
    /**
     * Minimum number of discs to populate when starting a Normal/Sequence game.
     */
    const val NORMAL_INITIAL_MIN_DISCS = 8

    /**
     * Maximum number of discs to populate when starting a Normal/Sequence game.
     */
    const val NORMAL_INITIAL_MAX_DISCS = 14

    /**
     * Minimum number of discs to populate when starting a Challenge game.
     */
    const val CHALLENGE_INITIAL_MIN_DISCS = 6

    /**
     * Maximum number of discs to populate when starting a Challenge game.
     */
    const val CHALLENGE_INITIAL_MAX_DISCS = 10

    /**
     * Maximum number of discs allowed in any single column at game start.
     */
    const val INITIAL_MAX_DISCS_PER_COLUMN = 4


    // ===== Disc Configuration =====
    /**
     * Minimum disc value (numbered discs).
     */
    const val MIN_DISC_VALUE = 1

    /**
     * Maximum disc value (numbered discs).
     */
    const val MAX_DISC_VALUE = 7

    /**
     * Initial number of cracks on a solid disc.
     */
    const val SOLID_DISC_INITIAL_CRACKS = 0

    /**
     * Number of cracks needed to reveal a solid disc (it reveals on the crack after this).
     */
    const val SOLID_DISC_CRACKS_TO_REVEAL = 2

    /**
     * Probability of generating a numbered disc in Normal/Sequence mode (0.0-1.0).
     */
    const val NUMBERED_DISC_PROBABILITY = 0.8f

    /**
     * Size of the sliding window for checking disc generation runs.
     */
    const val DISC_RUN_WINDOW_SIZE = 7

    /**
     * Maximum number of discs with the same value allowed in the run window.
     */
    const val MAX_SAME_VALUE_IN_WINDOW = 3

    /**
     * Maximum number of solid discs allowed in the run window.
     */
    const val MAX_SOLID_DISCS_IN_WINDOW = 3

    // ===== Game Mode Configuration =====
    /**
     * Initial number of drops until a new row appears in Normal mode.
     */
    const val NORMAL_MODE_INITIAL_DROPS_PER_ROW = 30

    /**
     * Minimum number of drops per row in Normal mode (stops decrementing at this value).
     */
    const val NORMAL_MODE_MIN_DROPS_PER_ROW = 5

    /**
     * Default seed for Sequence mode.
     */
    const val DEFAULT_SEQUENCE_SEED = 1L

    // ===== Challenge Mode Difficulty Configuration =====
    /**
     * Drops per row for Easy difficulty.
     */
    const val CHALLENGE_EASY_DROPS_PER_ROW = 10

    /**
     * Drops per row for Medium difficulty.
     */
    const val CHALLENGE_MEDIUM_DROPS_PER_ROW = 7

    /**
     * Drops per row for Hard difficulty.
     */
    const val CHALLENGE_HARD_DROPS_PER_ROW = 5

    /**
     * Drops per row for Extreme difficulty.
     */
    const val CHALLENGE_EXTREME_DROPS_PER_ROW = 3

    // ===== Scoring Configuration =====
    /**
     * Minimum chain level (first chain has no multiplier beyond 1x).
     */
    const val MIN_CHAIN_LEVEL = 1

    /**
     * Bonus points awarded when a new row is added in Normal mode (leveling up).
     */
    const val NORMAL_MODE_LEVEL_BONUS_POINTS = 7000

    /**
     * Bonus points awarded when a new row is added in Challenge Easy mode.
     */
    const val CHALLENGE_EASY_LEVEL_BONUS_POINTS = 4000

    /**
     * Bonus points awarded when a new row is added in Challenge Medium mode.
     */
    const val CHALLENGE_MEDIUM_LEVEL_BONUS_POINTS = 10000

    /**
     * Bonus points awarded when a new row is added in Challenge Hard mode.
     */
    const val CHALLENGE_HARD_LEVEL_BONUS_POINTS = 17000

    /**
     * Bonus points awarded when a new row is added in Challenge Extreme mode.
     */
    const val CHALLENGE_EXTREME_LEVEL_BONUS_POINTS = 20000

    /**
     * Bonus points awarded when a new row is added in Sequence mode (leveling up).
     */
    const val SEQUENCE_MODE_LEVEL_BONUS_POINTS = 7000

    // ===== Animation Duration Configuration (milliseconds) =====
    /**
     * Duration for disc drop animation.
     */
    const val ANIM_DROP_DURATION_MS = 300

    /**
     * Duration for disc break/scale animation.
     */
    const val ANIM_BREAK_DURATION_MS = 300

    /**
     * Duration for fade out animation.
     */
    const val ANIM_FADE_OUT_DURATION_MS = 400

    /**
     * Duration for touch feedback animation.
     */
    const val ANIM_TOUCH_FEEDBACK_DURATION_MS = 250

    /**
     * Duration for pulse animation (one cycle).
     */
    const val ANIM_PULSE_DURATION_MS = 500

    /**
     * Delay before animations start (preview disc).
     */
    const val ANIM_PREVIEW_DELAY_MS = 200L

    /**
     * Duration to highlight matches before removal.
     */
    const val ANIM_HIGHLIGHT_DURATION_MS = 600L

    /**
     * Duration for gravity/falling animation when discs drop.
     */
    const val ANIM_GRAVITY_WITH_MOVEMENT_MS = 200L

    /**
     * Duration for gravity animation when no discs move.
     */
    const val ANIM_GRAVITY_NO_MOVEMENT_MS = 100L

    /**
     * Duration to display new row before processing matches.
     */
    const val ANIM_NEW_ROW_DISPLAY_MS = 300L

    /**
     * Delay before showing game over overlay after game ends.
     */
    const val GAME_OVER_OVERLAY_DELAY_MS = 1500L

    // ===== UI Rendering Configuration =====
    /**
     * Disc radius as a fraction of cell size.
     */
    const val DISC_RADIUS_FRACTION = 0.4f

    /**
     * Highlight ring radius multiplier (relative to disc radius).
     */
    const val HIGHLIGHT_RADIUS_MULTIPLIER = 1.15f

    /**
     * Preview disc Y offset (in cell heights) above the grid.
     */
    const val PREVIEW_DISC_Y_OFFSET = -1.5f

    /**
     * Alpha transparency for preview disc when not touching.
     */
    const val PREVIEW_DISC_ALPHA_IDLE = 0.7f

    /**
     * Alpha transparency for preview disc when touching.
     */
    const val PREVIEW_DISC_ALPHA_ACTIVE = 1.0f

    /**
     * Text size for disc numbers as a fraction of disc radius.
     */
    const val DISC_TEXT_SIZE_FRACTION = 1.6f

    /**
     * Alpha transparency for highlight overlay.
     */
    const val HIGHLIGHT_ALPHA = 0.3f

    /**
     * Alpha transparency for column hover effect.
     */
    const val HOVER_ALPHA = 0.2f

    /**
     * Alpha transparency for game over red overlay.
     */
    const val GAME_OVER_OVERLAY_ALPHA = 0.7f

    /**
     * Alpha transparency for black outline on discs.
     */
    const val DISC_OUTLINE_ALPHA = 0.3f

    /**
     * Alpha transparency for crack lines on solid discs.
     */
    const val CRACK_LINE_ALPHA = 1.0f

    /**
     * Stroke width for highlight ring.
     */
    const val HIGHLIGHT_STROKE_WIDTH = 6f

    /**
     * Stroke width for crack lines.
     */
    const val CRACK_LINE_STROKE_WIDTH = 3f

    /**
     * Padding for game over red background rectangle.
     */
    const val GAME_OVER_RECT_PADDING = 2f

    /**
     * Crack line offset multipliers for visual effect.
     */
    const val CRACK_LINE_1_START_X = -0.6f
    const val CRACK_LINE_1_START_Y = -0.3f
    const val CRACK_LINE_1_END_X = 0.4f
    const val CRACK_LINE_1_END_Y = 0.5f
    const val CRACK_LINE_2_START_X = -0.3f
    const val CRACK_LINE_2_START_Y = -0.5f
    const val CRACK_LINE_2_END_X = 0.5f
    const val CRACK_LINE_2_END_Y = 0.3f

    /**
     * Pulse animation scale target (1.0 = normal, 1.2 = 20% larger).
     */
    const val PULSE_SCALE_TARGET = 1.2f

    /**
     * Break animation scale target (how much discs scale up when breaking).
     */
    const val BREAK_SCALE_TARGET = 1.5f

    // ===== Drops Remaining Indicator Configuration =====
    /**
     * Maximum number of mini-discs to display in the drops remaining indicator.
     */
    const val MAX_DROPS_INDICATOR_COUNT = 30

    /**
     * Size of each mini-disc in the drops remaining indicator (in dp).
     */
    const val DROPS_INDICATOR_DISC_SIZE_DP = 10

    /**
     * Spacing between mini-discs in the drops remaining indicator (in dp).
     */
    const val DROPS_INDICATOR_SPACING_DP = 3

    /**
     * Alpha transparency for mini-discs in the drops remaining indicator.
     */
    const val DROPS_INDICATOR_ALPHA = 0.6f

    // ===== Statistics Configuration =====
    /**
     * Maximum number of recent games to keep in history.
     */
    const val MAX_RECENT_GAMES = 100

    // ===== Undo Configuration =====
    /**
     * Maximum number of moves that can be undone.
     */
    const val MAX_UNDO_HISTORY = 3

    // ===== Engine & Generator Limits =====
    /**
     * Maximum attempts to find a valid initial board state.
     */
    const val MAX_STARTUP_ATTEMPTS = 5000

    /**
     * Maximum attempts to generate an acceptable next disc.
     */
    const val MAX_GENERATION_ATTEMPTS = 50

    // ===== Game Mode Logic (Centralized) =====

    /**
     * Gets the level bonus for a specific game mode.
     */
    fun getLevelBonus(mode: io.github.jdanders.dropcount.model.GameMode): Int {
        return when (mode) {
            is io.github.jdanders.dropcount.model.GameMode.Normal -> NORMAL_MODE_LEVEL_BONUS_POINTS
            is io.github.jdanders.dropcount.model.GameMode.Challenge -> when (mode.difficulty) {
                io.github.jdanders.dropcount.model.ChallengeDifficulty.EASY -> CHALLENGE_EASY_LEVEL_BONUS_POINTS
                io.github.jdanders.dropcount.model.ChallengeDifficulty.MEDIUM -> CHALLENGE_MEDIUM_LEVEL_BONUS_POINTS
                io.github.jdanders.dropcount.model.ChallengeDifficulty.HARD -> CHALLENGE_HARD_LEVEL_BONUS_POINTS
                io.github.jdanders.dropcount.model.ChallengeDifficulty.EXTREME -> CHALLENGE_EXTREME_LEVEL_BONUS_POINTS
            }
            is io.github.jdanders.dropcount.model.GameMode.Sequence -> SEQUENCE_MODE_LEVEL_BONUS_POINTS
        }
    }

    /**
     * Gets the minimum initial discs for a specific game mode.
     */
    fun getInitialMinDiscs(mode: io.github.jdanders.dropcount.model.GameMode): Int {
        return when (mode) {
            is io.github.jdanders.dropcount.model.GameMode.Normal -> NORMAL_INITIAL_MIN_DISCS
            is io.github.jdanders.dropcount.model.GameMode.Challenge -> CHALLENGE_INITIAL_MIN_DISCS
            is io.github.jdanders.dropcount.model.GameMode.Sequence -> NORMAL_INITIAL_MIN_DISCS
        }
    }

    /**
     * Gets the maximum initial discs for a specific game mode.
     */
    fun getInitialMaxDiscs(mode: io.github.jdanders.dropcount.model.GameMode): Int {
        return when (mode) {
            is io.github.jdanders.dropcount.model.GameMode.Normal -> NORMAL_INITIAL_MAX_DISCS
            is io.github.jdanders.dropcount.model.GameMode.Challenge -> CHALLENGE_INITIAL_MAX_DISCS
            is io.github.jdanders.dropcount.model.GameMode.Sequence -> NORMAL_INITIAL_MAX_DISCS
        }
    }
}