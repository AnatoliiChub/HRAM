package com.achub.hram.usecase

import com.achub.hram.data.repo.HrActivityRepo
import com.achub.hram.export.FileExporter

class ExportCsvUseCase(
    private val hrActivityRepo: HrActivityRepo,
    private val fileExporter: FileExporter,
) {
    suspend operator fun invoke(activityId: String) {
        val hearts = hrActivityRepo.getHeartRatesForActivity(activityId)
        if (hearts.isEmpty()) return

        val csvHeader = "Timestamp (ms),Elapsed Time (ms),Heart Rate (bpm),Contact On,Battery Level\n"
        val csvContent = hearts.joinToString(separator = "\n") {
            "${it.timestamp},${it.elapsedTime},${it.heartRate},${it.isContactOn},${it.batteryLevel}"
        }
        val fullContent = csvHeader + csvContent

        val activity = hrActivityRepo.getActivity(activityId)
        // Use activity name for filename, or id if name is empty, sanitized.
        val safeName = (activity?.name ?: "").map { if (it.isLetterOrDigit()) it else '_' }.joinToString("")
        fileExporter.exportData("activity_${safeName}_$activityId.csv", fullContent)
    }
}
