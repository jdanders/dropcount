package io.github.jdanders.dropcount.util

import android.util.Log
import io.github.jdanders.dropcount.BuildConfig

object Logger {
    private val ENABLED = BuildConfig.DEBUG

    fun d(tag: String, message: String) {
        if (ENABLED) Log.d(tag, message)
    }

    fun e(tag: String, message: String, throwable: Throwable? = null) {
        if (ENABLED) Log.e(tag, message, throwable)
    }
}
