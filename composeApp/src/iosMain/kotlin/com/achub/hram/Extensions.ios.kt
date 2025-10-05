package com.achub.hram

import platform.Foundation.NSThread

actual fun currentThread() = "iOS thread ${NSThread.currentThread()} is main: ${NSThread.isMainThread()}"