package io.github.jdanders.dropcount.model

import androidx.annotation.StringRes
import io.github.jdanders.dropcount.R
import io.github.jdanders.dropcount.ui.theme.ClassicThemeRenderer
import io.github.jdanders.dropcount.ui.theme.NeonThemeRenderer
import io.github.jdanders.dropcount.ui.theme.FoundryThemeRenderer
import io.github.jdanders.dropcount.ui.theme.WoodblockThemeRenderer
import io.github.jdanders.dropcount.ui.theme.ThemeRenderer
import kotlinx.serialization.Serializable

/**
 * Available visual themes for the game.
 */
@Serializable
enum class VisualTheme(@param:StringRes val displayNameRes: Int, @param:StringRes val descriptionRes: Int) {
    CLASSIC(
        displayNameRes = R.string.theme_classic,
        descriptionRes = R.string.theme_classic_desc
    ),
    NEON(
        displayNameRes = R.string.theme_neon,
        descriptionRes = R.string.theme_neon_desc
    ),
    FOUNDRY(
        displayNameRes = R.string.theme_foundry,
        descriptionRes = R.string.theme_foundry_desc
    ),
    WOODBLOCK(
        displayNameRes = R.string.theme_woodblock,
        descriptionRes = R.string.theme_woodblock_desc
    );
    
    /**
     * Creates the appropriate renderer for this theme.
     */
    fun createRenderer(): ThemeRenderer = when (this) {
        CLASSIC -> ClassicThemeRenderer()
        NEON -> NeonThemeRenderer()
        FOUNDRY -> FoundryThemeRenderer()
        WOODBLOCK -> WoodblockThemeRenderer()
    }
}
