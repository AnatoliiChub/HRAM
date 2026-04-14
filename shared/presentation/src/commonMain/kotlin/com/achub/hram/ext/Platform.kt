package com.achub.hram.ext

enum class Platform {
    Android,
    Ios,
    Desktop;

    fun isMobile(): Boolean = this == Android || this == Ios

    fun isDesktop(): Boolean = this == Desktop
}

expect fun getPlatform(): Platform
