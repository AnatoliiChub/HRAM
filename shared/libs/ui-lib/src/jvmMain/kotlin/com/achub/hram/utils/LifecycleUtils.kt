package com.achub.hram.utils

import androidx.compose.runtime.Composable

/**
 * Desktop override: Always returns false since desktop apps don't have lifecycle state changes
 * like Android apps (no pause/resume events).
 */
@Composable
actual fun isAppInBackground(): Boolean = false

/**
 * Desktop override: No-op implementation since desktop doesn't have app lifecycle events.
 */
@Suppress("ComposableNaming")
@Composable
actual fun appStateChanged(onChanged: (state: AppState) -> Unit) {
    // No-op: Desktop doesn't have lifecycle events like Android
}
