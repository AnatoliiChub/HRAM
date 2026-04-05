package com.achub.hram.utils

import com.achub.hram.utils.formatElapsedTime as formatElapsedTimeUtil

object DateUtils {
    fun formatElapsedTime(timeInMs: Long): String = formatElapsedTimeUtil(timeInMs)
}

