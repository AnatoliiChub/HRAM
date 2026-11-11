package com.achub.hram.screen.record

import androidx.lifecycle.viewModelScope
import com.achub.hram.ActivityNameErrorMapper
import com.achub.hram.data.state.BleStateRepo
import com.achub.hram.data.state.SettingsStateRepo
import com.achub.hram.data.state.StateRepo
import com.achub.hram.data.state.TrackingStateRepo
import com.achub.hram.ext.BlePermissionController
import com.achub.hram.models.BleState
import com.achub.hram.models.HrBucket
import com.achub.hram.models.UserSettings
import com.achub.hram.tracking.ActiveSessionBuckets
import com.achub.hram.tracking.SessionRecorder
import com.achub.hram.tracking.TrackingController
import com.achub.hram.tracking.TrackingStateStage
import com.achub.hram.usecase.CalculateCaloriesUseCase
import com.achub.hram.usecase.ObserveBleStateUseCase
import com.achub.hram.usecase.ObserveTrackingStateUseCase
import com.achub.hram.usecase.ObserveUserSettingsUseCase
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.mock
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class)
class RecordCaloriesRefreshTest {
    @Test
    fun `calories refresh every five seconds and update on pause and reset`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)
        try {
            val tracking = TrackingStore()
            val settings = SettingsStore()
            val session = mock<SessionRecorder>(MockMode.autofill)
            val calculator = CalculateCaloriesUseCase()
            val first = sessionBuckets(10_000, 140f)
            val second = sessionBuckets(20_000, 160f)
            everySuspend { session.currentBuckets() } returns first
            val viewModel = RecordViewModel(
                activityNameErrorMapper = ActivityNameErrorMapper(),
                dispatcher = dispatcher,
                trackingController = mock<TrackingController>(MockMode.autofill),
                observeBleState = ObserveBleStateUseCase(BleStore()),
                observeTrackingState = ObserveTrackingStateUseCase(tracking),
                observeUserSettings = ObserveUserSettingsUseCase(settings),
                calculateCalories = calculator,
                sessionRecorder = session,
                permissionController = mock<BlePermissionController>(MockMode.autofill)
            )
            runCurrent()
            tracking.state.value = TrackingStateStage.ACTIVE_TRACKING_STATE
            runCurrent()
            val firstCalories = calculator(UserSettings.Default, first.buckets, 10)
            assertEquals(firstCalories, viewModel.uiState.value.kcalBurnt)

            everySuspend { session.currentBuckets() } returns second
            advanceTimeBy(4_999.milliseconds)
            runCurrent()
            assertEquals(firstCalories, viewModel.uiState.value.kcalBurnt)
            advanceTimeBy(1.milliseconds)
            runCurrent()
            val secondCalories = calculator(UserSettings.Default, second.buckets, 20)
            assertEquals(secondCalories, viewModel.uiState.value.kcalBurnt)

            everySuspend { session.currentBuckets() } returns first
            tracking.state.value = TrackingStateStage.PAUSED_TRACKING_STATE
            runCurrent()
            assertEquals(firstCalories, viewModel.uiState.value.kcalBurnt)
            val newSettings = UserSettings.Default.copy(weightKg = 90f)
            settings.state.value = newSettings
            runCurrent()
            assertEquals(calculator(newSettings, first.buckets, 10), viewModel.uiState.value.kcalBurnt)
            tracking.state.value = TrackingStateStage.TRACKING_INIT_STATE
            runCurrent()
            assertEquals(0.0, viewModel.uiState.value.kcalBurnt)
            advanceTimeBy(10_000.milliseconds)
            runCurrent()
            assertEquals(0.0, viewModel.uiState.value.kcalBurnt)
            viewModel.viewModelScope.cancel()
        } finally {
            Dispatchers.resetMain()
        }
    }

    private fun sessionBuckets(durationMillis: Long, heartRate: Float) = ActiveSessionBuckets(
        durationMillis,
        List(100) { HrBucket(it, heartRate, it.toLong()) }
    )
}

private open class FakeStore<T>(initial: T) : StateRepo<T> {
    val state = MutableStateFlow(initial)

    override suspend fun update(state: T) {
        this.state.value = state
    }

    override fun listen() = state

    override suspend fun get() = state.value

    override suspend fun release() = Unit
}

private class BleStore : FakeStore<BleState>(BleState.Disconnected), BleStateRepo

private class TrackingStore :
    FakeStore<TrackingStateStage>(TrackingStateStage.TRACKING_INIT_STATE), TrackingStateRepo

private class SettingsStore : FakeStore<UserSettings>(UserSettings.Default), SettingsStateRepo
