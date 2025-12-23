package io.github.jdanders.dropseven.sound

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Build

/**
 * Manages game sound effects.
 * 
 * Note: This class is set up for future sound integration.
 * To add sounds, place audio files in app/src/main/res/raw/ and load them here.
 */
class SoundManager(private val context: Context) {
    private var soundPool: SoundPool? = null
    private var isSoundEnabled = true
    
    // Sound IDs (will be populated when actual sound files are added)
    private var dropSoundId: Int? = null
    private var breakSoundId: Int? = null
    private var chainSoundId: Int? = null
    private var levelUpSoundId: Int? = null
    private var gameOverSoundId: Int? = null
    
    init {
        initializeSoundPool()
        // TODO: Load sound files when they're added to res/raw/
        // Example:
        // dropSoundId = soundPool?.load(context, R.raw.drop_sound, 1)
        // breakSoundId = soundPool?.load(context, R.raw.break_sound, 1)
    }
    
    private fun initializeSoundPool() {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        
        soundPool = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            SoundPool.Builder()
                .setMaxStreams(10)
                .setAudioAttributes(audioAttributes)
                .build()
        } else {
            @Suppress("DEPRECATION")
            SoundPool(10, android.media.AudioManager.STREAM_MUSIC, 0)
        }
    }
    
    /**
     * Sets whether sound is enabled.
     */
    fun setSoundEnabled(enabled: Boolean) {
        isSoundEnabled = enabled
    }
    
    /**
     * Plays the disc drop sound.
     */
    fun playDropSound() {
        if (!isSoundEnabled) return
        dropSoundId?.let { soundId ->
            soundPool?.play(soundId, 1.0f, 1.0f, 1, 0, 1.0f)
        }
    }
    
    /**
     * Plays the disc break sound.
     * @param pitch Pitch multiplier for variety (1.0 is normal, can vary for chains)
     */
    fun playBreakSound(pitch: Float = 1.0f) {
        if (!isSoundEnabled) return
        breakSoundId?.let { soundId ->
            soundPool?.play(soundId, 1.0f, 1.0f, 1, 0, pitch)
        }
    }
    
    /**
     * Plays the chain sound.
     * @param chainLevel The current chain level (higher = different pitch)
     */
    fun playChainSound(chainLevel: Int) {
        if (!isSoundEnabled) return
        chainSoundId?.let { soundId ->
            val pitch = 1.0f + (chainLevel - 1) * 0.1f // Increase pitch for higher chains
            soundPool?.play(soundId, 1.0f, 1.0f, 1, 0, pitch)
        }
    }
    
    /**
     * Plays the level up sound.
     */
    fun playLevelUpSound() {
        if (!isSoundEnabled) return
        levelUpSoundId?.let { soundId ->
            soundPool?.play(soundId, 1.0f, 1.0f, 1, 0, 1.0f)
        }
    }
    
    /**
     * Plays the game over sound.
     */
    fun playGameOverSound() {
        if (!isSoundEnabled) return
        gameOverSoundId?.let { soundId ->
            soundPool?.play(soundId, 1.0f, 1.0f, 1, 0, 1.0f)
        }
    }
    
    /**
     * Releases resources when the SoundManager is no longer needed.
     */
    fun release() {
        soundPool?.release()
        soundPool = null
    }
}

/**
 * Instructions for adding sound files:
 * 
 * 1. Create a 'raw' folder in app/src/main/res/ if it doesn't exist
 * 2. Add your audio files (preferably .ogg or .mp3 format):
 *    - drop_sound.ogg - short "plop" sound for dropping disc
 *    - break_sound.ogg - satisfying "pop" for breaking discs
 *    - chain_sound.ogg - exciting sound for chains
 *    - level_up_sound.ogg - triumphant sound for leveling up
 *    - game_over_sound.ogg - game over tone
 * 
 * 3. Uncomment the load statements in the init block
 * 4. Integrate with GameEngine/ViewModel to call sounds at appropriate times
 */

