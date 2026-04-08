@file:Suppress("detekt:NoUnusedImports") // https://github.com/detekt/detekt/issues/8140

package com.achub.hram.ext

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

private const val STATE_WHILE_SUBSCRIBED_TIMEOUT_MS = 5_000L

context(viewModel: ViewModel)
fun <T> MutableStateFlow<T>.stateInExt(initialValue: T) = stateIn(
    scope = viewModel.viewModelScope,
    started = SharingStarted.WhileSubscribed(STATE_WHILE_SUBSCRIBED_TIMEOUT_MS),
    initialValue = initialValue,
)

context(viewModel: ViewModel)
fun <T> Flow<T>.stateInExt(initialValue: T) = stateIn(
    scope = viewModel.viewModelScope,
    started = SharingStarted.WhileSubscribed(STATE_WHILE_SUBSCRIBED_TIMEOUT_MS),
    initialValue = initialValue,
)
