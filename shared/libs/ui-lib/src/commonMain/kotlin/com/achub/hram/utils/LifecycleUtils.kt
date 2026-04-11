package com.achub.hram.utils

import androidx.compose.runtime.Composable

/**
 * Platform-specific function to check if the app is in the background.
 * - Android: Returns true when app is in background (based on lifecycle events)
 * - Desktop/JVM & iOS: Always returns false
 */
@Composable
expect fun isAppInBackground(): Boolean

/**
 * Platform-specific callback for app state changes.
 * - Android: Observes lifecycle events and calls the callback
 * - Desktop/JVM & iOS: No-op (not applicable on these platforms)
 */
@Suppress("ComposableNaming")
@Composable
expect fun appStateChanged(onChanged: (state: AppState) -> Unit)

enum class AppState {
    FOREGROUND,
    BACKGROUND;

    fun isBackground() = this == BACKGROUND
}
