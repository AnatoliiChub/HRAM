package com.achub.hram.export

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File

class AndroidFileExporter(private val context: Context) : FileExporter {
    override suspend fun exportData(fileName: String, content: String) {
        val file = File(context.cacheDir, fileName)
        file.writeText(content)
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        val chooser = Intent.createChooser(intent, "Export Activity").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }
}

