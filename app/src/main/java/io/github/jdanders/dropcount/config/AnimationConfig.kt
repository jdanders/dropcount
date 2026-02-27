package io.github.jdanders.dropcount.config

import io.github.jdanders.dropcount.model.AnimationSpeed

/**
 * Centralized animation duration calculations that account for animation speed settings.
 * This eliminates duplication between GameViewModel and UI components.
 */
object AnimationConfig {

    fun getDropDuration(speed: AnimationSpeed): Long =
        speed.adjustNonMatchDuration(GameConfig.ANIM_DROP_DURATION_MS.toLong())

    fun getHighlightDuration(speed: AnimationSpeed): Long =
        speed.adjustDuration(GameConfig.ANIM_HIGHLIGHT_DURATION_MS.toLong())

    fun getNewRowDisplayDuration(speed: AnimationSpeed): Long =
        speed.adjustNonMatchDuration(GameConfig.ANIM_NEW_ROW_DISPLAY_MS)

    fun getGravityDuration(speed: AnimationSpeed, hasMovement: Boolean): Long {
        val baseDuration = if (hasMovement) {
            GameConfig.ANIM_GRAVITY_WITH_MOVEMENT_MS
        } else {
            GameConfig.ANIM_GRAVITY_NO_MOVEMENT_MS
        }
        return speed.adjustNonMatchDuration(baseDuration)
    }

    fun getBreakDuration(speed: AnimationSpeed): Long =
        speed.adjustNonMatchDuration(GameConfig.ANIM_BREAK_DURATION_MS.toLong())

    fun getFadeOutDuration(speed: AnimationSpeed): Long =
        speed.adjustNonMatchDuration(GameConfig.ANIM_FADE_OUT_DURATION_MS.toLong())

    fun getTouchFeedbackDuration(speed: AnimationSpeed): Long =
        speed.adjustNonMatchDuration(GameConfig.ANIM_TOUCH_FEEDBACK_DURATION_MS.toLong())

    fun getPulseDuration(speed: AnimationSpeed): Long =
        speed.adjustNonMatchDuration(GameConfig.ANIM_PULSE_DURATION_MS.toLong())
}