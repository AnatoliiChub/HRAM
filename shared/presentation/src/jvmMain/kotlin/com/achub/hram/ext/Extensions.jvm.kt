package com.achub.hram.ext

actual fun currentThread(): String = "Thread: ${Thread.currentThread()}"
