package io.github.jdanders.dropcount.config

import androidx.compose.ui.graphics.Color
import io.github.jdanders.dropcount.ui.theme.*

/**
 * Centralized configuration for theme-related constants and magic numbers.
 */
object ThemeConfig {

    // ===== Global Theme Configuration =====
    val THEME_SELECTION_BUTTON_HEIGHT = 72
    val SPEED_SELECTION_BUTTON_HEIGHT = 56

    // ===== Neon Theme Configuration =====
    object Neon {
        // Colors
        val PRIMARY_NEON = Color(0xFF00F0FF) // CyanGlow
        val SECONDARY_NEON = Color(0xFFFF00E5) // MagentaPulse
        val BACKGROUND_DARK = Color(0xFF0A0E27) // SpaceBlack
        val BACKGROUND_DEEP = Color(0xFF151B3D) // MidnightVoid
        val SCANLINE_COLOR = Color.White.copy(alpha = 0.02f)
        val GLITCH_COLOR = Color(0xFF00F0FF).copy(alpha = 0.3f)

        // Disc Colors
        val DISC_COLORS = mapOf(
            1 to Color(0xFFFF1744), // Hot Red
            2 to Color(0xFFFF9100), // Vibrant Orange
            3 to Color(0xFFFFEA00), // Electric Yellow
            4 to Color(0xFF00E676), // Neon Green
            5 to Color(0xFF00E5FF), // Cyan Bright
            6 to Color(0xFF3D5AFE), // Electric Blue
            7 to Color(0xFFE040FB)  // Magenta Pink
        )

        // Solid Disc Colors
        val SOLID_LEVEL_0 = Color(0xFF555555)
        val SOLID_LEVEL_1 = Color(0xFF00AAAA)
        val SOLID_LEVEL_2 = Color(0xFF00FFFF)

        // Rendering Specs
        const val NEON_GLOW_WIDTH = 8f
        const val MAIN_STROKE_WIDTH = 2f
        const val SCANLINE_COUNT_FACTOR = 4
        const val SCANLINE_STROKE_WIDTH = 1f
        const val GLITCH_SIZE_FACTOR = 0.3f
        const val GLITCH_HEIGHT = 2f
        const val GRID_LINE_ALPHA = 0.3f
        const val HOVER_ALPHA = 0.15f
        const val HIGHLIGHT_RADIUS_MULTIPLIER = 1.15f
        const val HIGHLIGHT_STROKE_WIDTH = 3f
        const val LETTER_SPACING_SP = 2
        const val TEXT_SCALE_FACTOR = 1.6f

        // Disc Glow Alphas
        const val OUTER_GLOW_ALPHA = 0.3f
        const val MID_GLOW_ALPHA = 0.6f
        const val CORE_GLOW_ALPHA = 1.0f
        const val SPECULAR_ALPHA = 0.4f
        const val EDGE_HIGHLIGHT_ALPHA = 0.2f

        // Glow radius multipliers
        const val OUTER_GLOW_RADIUS_MULT = 1.6f
        const val MID_GLOW_RADIUS_MULT = 1.2f
        const val HIGHLIGHT_GLOW_RADIUS_MULT = 1.3f
    }

    // ===== Classic Theme Configuration =====
    object Classic {
        // Existing constants are already in GameConfig and UIConfig,
        // but we can centralize specific ones here if needed.
        val BACKGROUND_DARKER = Color(0xFF0F0F1E)
        val OVERLAY_BACKGROUND = Color(0xFF1A1A2E) // BackgroundDark

        // Crack rendering constants
        const val CRACK_BASE_ANGLE = Math.PI / 4.8
        const val CRACK_MAX_ANGLE_SPREAD_DEG = 15.0
        const val CRACK_THICK_FACTOR = 0.075f
        const val CRACK_THIN_FACTOR = 0.050f
        const val CRACK_PARTIAL_FACTOR = 0.025f
        const val CRACK_JITTER_FACTOR = 0.95f
        const val CRACK_SEGMENT_BASE = 0.32f
        const val CRACK_SEGMENT_VARIANCE = 0.08f

        // Disc rendering
        const val RIM_STROKE_WIDTH = 1.5f
        const val INNER_HIGHLIGHT_ALPHA = 0.35f
        const val RIM_ALPHA = 0.15f
        const val SOLID_RIM_ALPHA = 0.3f
        const val SOLID_RIM_STROKE_WIDTH = 2f
        const val GRID_OUTER_BORDER_WIDTH = 2f
        const val GRID_LINE_ALPHA = 0.5f
        const val GRID_OUTER_BORDER_ALPHA = 0.3f
    }

    object Overlay {
        val ALPHA = 0.8f
        val CLASSIC_BACKGROUND = Color(0xFF1A1A2E) // BackgroundDark
        val RETRO_BACKGROUND = Color(0xFF0A0A12)
    }
    // ===== Woodblock Theme Configuration =====
    object Woodblock {
        // Hanko Seal Shape & Size (square with rounded corners)
        const val SEAL_SIZE_MULTIPLIER = 1.7f // Makes seal larger than disc radius
        const val SEAL_CORNER_RADIUS_FACTOR = 0.12f // Proportional to seal width
        const val SEAL_EDGE_WOBBLE_FACTOR = 0.015f // Hand-carved irregularity

        // Shadows & Depth (stamped impression effect)
        const val SHADOW_OFFSET_X = 3f
        const val SHADOW_OFFSET_Y = 3f
        const val SHADOW_ALPHA = 0.35f

        // Outlines
        const val OUTLINE_WIDTH = 3f

        // Paper Texture
        const val LONG_FIBER_COUNT = 400 // Increased from 150
        const val SHORT_FIBER_COUNT = 800 // Increased from 300
        const val PAPER_FIBER_DENSITY = 80f // More dense (lower value)
        const val PAPER_FIBER_MIN_LENGTH = 2f
        const val PAPER_FIBER_MAX_LENGTH = 12f
        const val PAPER_FIBER_ALPHA = 0.15f // Increased from 0.12f

        const val GRAIN_COUNT = 3000
        const val GRAIN_ALPHA = 0.1f
        const val GRAIN_SIZE = 1.5f

        // Number Visibility (calligraphic style with strong shadows)
        const val BRUSH_STEPS = 2
        const val BRUSH_ALPHA_FACTOR = 0.8f
        const val BRUSH_SIZE_FACTOR = 3.5f

        const val NUMBER_OUTLINE_STEPS = 3
        const val NUMBER_OUTLINE_SIZE = 2.5f
        const val NUMBER_OUTLINE_ALPHA = 0.7f

        const val NUMBER_GLOW_STEPS = 3
        const val NUMBER_GLOW_SIZE = 1.5f
        const val NUMBER_GLOW_ALPHA = 0.35f

        // Solid disc texture intensity
        const val SOLID_GRAIN_INTENSITY = 1.5f

        // Kintsugi Cracks
        const val CRACK_START_RADIUS_MIN = 0.3f
        const val CRACK_SEGMENTS = 8
        const val CRACK_ANGLE_VARIATION = 60f
        const val CRACK_SEGMENT_LENGTH = 0.2f
        const val CRACK_WIDTH = 4f
        const val CRACK_OUTLINE_WIDTH = 6f
        const val CRACK_ALPHA = 1.0f

        // Highlight
        const val HIGHLIGHT_PADDING = 6f
        const val HIGHLIGHT_ALPHA = 0.7f
        const val HIGHLIGHT_STROKE_WIDTH = 4f

        // Grid
        const val GRID_WOBBLE_FACTOR = 1f
        const val GRID_LINE_ALPHA = 0.3f
        const val GRID_LINE_WIDTH = 1.5f

        // Cancellation Stamp (shattered solid disc overlay)
        const val CANCEL_STAMP_STROKE_FACTOR = 0.20f   // Max brush width as fraction of sealSize
        const val CANCEL_STAMP_EXTENT_FACTOR = 0.36f   // Arm reach from center as fraction of sealSize
        const val CANCEL_STAMP_ALPHA = 0.88f           // Main fill opacity
        const val CANCEL_STAMP_SPLAT_RADIUS_FACTOR = 0.30f
        const val CANCEL_STAMP_SPLAT_ALPHA = 0.50f
        const val CANCEL_STAMP_ROTATION_RANGE = 10f    // ± degrees of random tilt per disc
        const val CANCEL_STAMP_CURVE_STEPS = 24        // Bezier tessellation steps (higher = smoother)
    }

    // ===== Foundry Theme Configuration =====
    object Foundry {
        const val GRID_STROKE_WIDTH = 4f
        const val OUTER_FRAME_WIDTH = 8f
        const val CELL_INNER_SHADOW = 4f
        const val CORNER_ACCENT_SIZE = 12f

        // Disc rendering - Sizing
        // Target size relative to cell width (0.98 = 98%)
        const val TARGET_DISC_SIZE = 0.98f
        // Scale factor to convert from standard GameConfig size to Foundry size
        const val DISC_SIZE_FACTOR = TARGET_DISC_SIZE / (io.github.jdanders.dropcount.config.GameConfig.DISC_RADIUS_FRACTION * 2)
        const val DISC_SHADOW_OFFSET = 6f
        const val BEVEL_SIZE = 4f
        const val BORDER_WIDTH = 2f
        const val HIGHLIGHT_WIDTH = 3f

        // Border specifics
        const val SOLID_BORDER_ALPHA = 0.5f // Light outline for visibility
        const val NUMBERED_BORDER_ALPHA = 0.8f // Dark outline for contrast
        const val HIGHLIGHT_PADDING = 4f
        const val FONT_SCALE_FACTOR = 1.3f

        // Disc rendering - Alphas & Effects
        const val DISC_SHADOW_ALPHA = 0.4f
        const val BEVEL_HIGHLIGHT_ALPHA = 0.15f
        const val BEVEL_SHADOW_ALPHA = 0.3f
        const val HIGHLIGHT_ALPHA = 0.3f
        const val STRIPE_ALPHA = 0.3f
        const val STRIPE_STROKE_WIDTH = 4f
        const val STRIPE_COUNT = 6

        // Cracks / Structural
        const val STRUCTURAL_X_WIDTH = 8f
        const val STRUCTURAL_X_ALPHA = 0.6f


        // Background / Noise
        const val NOISE_ALPHA = 0.01f
        const val NOISE_COUNT = 200
        const val NOISE_RADIUS = 1f

        // Grid Details
        const val CORNER_DOT_ALPHA = 0.1f
        const val CORNER_DOT_SIZE = 4f

        // Cell Backgrounds
        // Cell Backgrounds
        const val CELL_HIGHLIGHT_ALPHA = 0.5f // Stronger white highlight for "beam" effect
        const val CELL_HOVER_ALPHA = 0.5f
        const val INNER_SHADOW_ALPHA = 0.15f

        // Overlay
        const val OVERLAY_ALPHA = 0.95f
    }

    // ===== Stained Glass Theme Configuration =====
    object StainedGlass {
        // Glass appearance
        const val OUTER_GLOW_RADIUS = 1.35f
        const val OUTER_GLOW_ALPHA = 0.3f
        const val GLASS_CENTER_ALPHA = 1.0f
        const val GLASS_MID_ALPHA = 0.9f
        const val GLASS_EDGE_ALPHA = 0.95f

        // Light effects
        const val LIGHT_OFFSET_FACTOR = 0.25f
        const val BLOOM_CENTER_ALPHA = 0.4f
        const val BLOOM_SIZE_FACTOR = 0.35f
        const val BLOOM_POSITION_FACTOR = 0.8f

        // Leading (iron borders)
        const val LEADING_WIDTH = 4f
        const val INNER_SHADOW_WIDTH = 3f
        const val INNER_SHADOW_ALPHA = 0.3f

        // Text readability
        const val TEXT_GLOW_RADIUS = 4f
        const val TEXT_GLOW_ALPHA = 0.5f
        const val TEXT_OUTLINE_ALPHA = 0.6f
        const val TEXT_SIZE_MULTIPLIER = 2.8f

        // Highlight
        const val HIGHLIGHT_STROKE_WIDTH = 3f
        const val HIGHLIGHT_INNER_ALPHA = 0.2f
        const val HIGHLIGHT_OUTER_ALPHA = 0.5f

        // Solid disc frosted texture
        const val FROSTED_TEXTURE_DENSITY = 150
        const val FROSTED_DOT_ALPHA = 0.15f
        const val FROSTED_DOT_MAX_SIZE = 2f

        // Cracks
        const val CRACKS_PER_LEVEL = 2
        const val CRACK_SEGMENTS = 4
        const val CRACK_WIDTH = 2f
        const val CRACK_GLOW_WIDTH = 5f

        // Background
        const val STONE_TEXTURE_SEED = 42
        const val STONE_TEXTURE_DENSITY = 800
        const val STONE_MIN_SIZE = 1f
        const val STONE_SIZE_RANGE = 3f
        const val STONE_ALPHA = 0.08f

        // Grid
        const val GRID_LINE_WIDTH = 5f

        // Cell interaction
        const val CELL_HIGHLIGHT_ALPHA = 0.25f
        const val CELL_HOVER_ALPHA = 0.1f

        // Overlay
        const val OVERLAY_ALPHA = 0.92f
    }
}