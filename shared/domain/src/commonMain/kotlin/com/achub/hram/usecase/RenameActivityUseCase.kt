package com.achub.hram.usecase

import com.achub.hram.data.HrActivityRepo

class RenameActivityUseCase(private val hrActivityRepo: HrActivityRepo) {
    suspend operator fun invoke(id: String, name: String) = hrActivityRepo.updateById(id, name)
}
