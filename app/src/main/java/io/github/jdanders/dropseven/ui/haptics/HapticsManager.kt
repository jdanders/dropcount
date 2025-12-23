package io.github.jdanders.dropseven.ui.haptics

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/**
 * Manages haptic feedback for the game.
 */
class HapticsManager(private val context: Context) {
    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }
    
    private var isHapticsEnabled = true
    
    /**
     * Sets whether haptics are enabled.
     */
    fun setHapticsEnabled(enabled: Boolean) {
        isHapticsEnabled = enabled
    }
    
    /**
     * Light tap feedback for disc drop.
     */
    fun lightTap() {
        if (!isHapticsEnabled) return
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            vibrator?.vibrate(
                VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK)
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(
                VibrationEffect.createOneShot(10, VibrationEffect.DEFAULT_AMPLITUDE)
            )
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(10)
        }
    }
    
    /**
     * Medium impact feedback for disc breaking.
     */
    fun mediumImpact() {
        if (!isHapticsEnabled) return
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            vibrator?.vibrate(
                VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK)
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(
                VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE)
            )
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(20)
        }
    }
    
    /**
     * Strong impact feedback for chain reactions.
     */
    fun strongImpact() {
        if (!isHapticsEnabled) return
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            vibrator?.vibrate(
                VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK)
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(
                VibrationEffect.createOneShot(40, VibrationEffect.DEFAULT_AMPLITUDE)
            )
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(40)
        }
    }
    
    /**
     * Success pattern for level up.
     */
    fun successPattern() {
        if (!isHapticsEnabled) return
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val pattern = longArrayOf(0, 50, 50, 50)
            val amplitudes = intArrayOf(0, 100, 0, 150)
            vibrator?.vibrate(
                VibrationEffect.createWaveform(pattern, amplitudes, -1)
            )
        } else {
            @Suppress("DEPRECATION")
            val pattern = longArrayOf(0, 50, 50, 50)
            vibrator?.vibrate(pattern, -1)
        }
    }
    
    /**
     * Failure pattern for game over.
     */
    fun failurePattern() {
        if (!isHapticsEnabled) return
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val pattern = longArrayOf(0, 100, 50, 100)
            val amplitudes = intArrayOf(0, 200, 0, 200)
            vibrator?.vibrate(
                VibrationEffect.createWaveform(pattern, amplitudes, -1)
            )
        } else {
            @Suppress("DEPRECATION")
            val pattern = longArrayOf(0, 100, 50, 100)
            vibrator?.vibrate(pattern, -1)
        }
    }
}

