package com.achub.hram.usecase

import com.achub.hram.data.repo.HrActivityRepo

class DeleteActivitiesUseCase(private val hrActivityRepo: HrActivityRepo) {
    suspend operator fun invoke(ids: Set<String>) = hrActivityRepo.deleteActivitiesById(ids)
}
