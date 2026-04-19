package com.achub.hram.usecase

import com.achub.hram.data.HrActivityRepo
import com.achub.hram.models.ActivityInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ObserveActivitiesUseCase(private val hrActivityRepo: HrActivityRepo) {
    operator fun invoke(limit: Int): Flow<List<ActivityInfo>> = hrActivityRepo.getActivities(limit)
        .map { list -> list.filter { it.name.isNotEmpty() } }
}
