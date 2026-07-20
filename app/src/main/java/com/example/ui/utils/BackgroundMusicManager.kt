package com.example.ui.utils

import android.content.Context
import android.media.MediaPlayer
import com.example.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object BackgroundMusicManager {
    private var mediaPlayer: MediaPlayer? = null
    private val _isMuted = MutableStateFlow(false)
    val isMuted: StateFlow<Boolean> = _isMuted.asStateFlow()
    private var isInitialized: Boolean = false

    fun start(context: Context) {
        val prefs = context.getSharedPreferences("ngebon_prefs", Context.MODE_PRIVATE)
        _isMuted.value = prefs.getBoolean("bg_music_muted", false)

        if (isInitialized) {
            if (!_isMuted.value && mediaPlayer?.isPlaying == false) {
                try {
                    mediaPlayer?.start()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            return
        }

        try {
            mediaPlayer = MediaPlayer.create(context.applicationContext, R.raw.bg_music).apply {
                isLooping = true
                setVolume(0.4f, 0.4f)
            }
            isInitialized = true
            if (!_isMuted.value) {
                mediaPlayer?.start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun pause() {
        try {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.pause()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun resume() {
        try {
            if (isInitialized && !_isMuted.value && mediaPlayer?.isPlaying == false) {
                mediaPlayer?.start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun toggleMute(context: Context) {
        val newValue = !_isMuted.value
        _isMuted.value = newValue
        
        val prefs = context.getSharedPreferences("ngebon_prefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("bg_music_muted", newValue).apply()

        if (newValue) {
            pause()
        } else {
            resume()
        }
    }

    fun release() {
        try {
            mediaPlayer?.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        mediaPlayer = null
        isInitialized = false
    }
}
