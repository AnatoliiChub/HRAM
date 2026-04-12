package com.achub.hram.export

import com.achub.hram.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.awt.Desktop
import java.io.File

class DesktopFileExporter : FileExporter {
    companion object {
        private const val TAG = "DesktopFileExporter"
    }

    override suspend fun exportData(fileName: String, content: String) {
        withContext(Dispatchers.IO) {
            val downloadsDir = File(System.getProperty("user.home"), "Downloads")
            downloadsDir.mkdirs()
            val file = File(downloadsDir, fileName)
            file.writeText(content)
            Logger.d(TAG) { "Exported to: ${file.absolutePath}" }
            try {
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(file)
                }
            } catch (e: Exception) {
                Logger.e(TAG) { "Could not open exported file: $e" }
            }
        }
    }
}
