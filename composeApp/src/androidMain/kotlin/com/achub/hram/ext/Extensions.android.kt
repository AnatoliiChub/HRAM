package com.achub.hram.ext

actual fun currentThread() = "Thread: ${Thread.currentThread()}"
