package com.achub.hram.models

const val ACTIVE_ACTIVITY = ""

data class ActivityRecord(
    val id: String,
    val name: String,
    val duration: Long,
    val startDate: Long,
)
