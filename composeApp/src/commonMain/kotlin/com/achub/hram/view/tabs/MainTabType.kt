package com.achub.hram.view.tabs

import hram.composeapp.generated.resources.Res
import hram.composeapp.generated.resources.ic_activities
import hram.composeapp.generated.resources.ic_record
import hram.composeapp.generated.resources.main_tab_activity
import hram.composeapp.generated.resources.main_tab_record
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

enum class MainTabType(val icon: DrawableResource, val label: StringResource) {
    Activities(Res.drawable.ic_activities, Res.string.main_tab_activity),
    Record(Res.drawable.ic_record, Res.string.main_tab_record)
}
