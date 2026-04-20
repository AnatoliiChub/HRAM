package com.achub.hram.usecase

import com.achub.hram.data.HrActivityRepo
import com.achub.hram.data.state.SettingsStateRepo
import com.achub.hram.models.ActivityInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class ObserveActivitiesUseCase(
    private val hrActivityRepo: HrActivityRepo,
    private val settingsRepo: SettingsStateRepo,
    private val calculateCalories: CalculateCaloriesUseCase
) {
    operator fun invoke(limit: Int): Flow<List<ActivityInfo>> = combine(
        hrActivityRepo.getActivities(limit),
        settingsRepo.listen()
    ) { activities, settings ->
        activities.filter { it.name.isNotEmpty() }
            .map { activity ->
                activity.copy(
                    kcalBurnt = calculateCalories(settings, activity.buckets, activity.duration)
                )
            }
    }
}
