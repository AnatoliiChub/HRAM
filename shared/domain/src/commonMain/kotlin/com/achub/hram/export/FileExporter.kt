package com.achub.hram.export

interface FileExporter {
    suspend fun exportData(fileName: String, content: String)
}

