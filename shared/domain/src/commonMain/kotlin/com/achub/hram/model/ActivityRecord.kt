package com.achub.hram.model

const val ACTIVE_ACTIVITY = ""

data class ActivityRecord(
    val id: String,
    val name: String,
    val duration: Long,
    val startDate: Long,
)

