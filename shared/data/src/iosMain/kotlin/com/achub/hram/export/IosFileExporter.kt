package com.achub.hram.export

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.Foundation.NSString
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Foundation.writeToFile
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication

class IosFileExporter : FileExporter {
    @OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
    override suspend fun exportData(fileName: String, content: String) {
        withContext(Dispatchers.Main) {
            val tempDir = NSTemporaryDirectory()
            val path = tempDir + fileName
            val nsContent = NSString.create(string = content)

            nsContent.writeToFile(path, true, NSUTF8StringEncoding, null)

            val url = NSURL.fileURLWithPath(path)
            val controller = UIActivityViewController(listOf(url), null)

            val rootController = UIApplication.sharedApplication.keyWindow?.rootViewController

            rootController?.presentViewController(controller, animated = true, completion = null)
        }
    }
}

