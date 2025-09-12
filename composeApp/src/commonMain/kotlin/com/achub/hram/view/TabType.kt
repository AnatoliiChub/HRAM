package com.achub.hram.view

import cafe.adriel.voyager.navigator.tab.Tab
import com.achub.hram.screen.activities.ActivitiesScreen
import com.achub.hram.screen.record.RecordScreen
import hram.composeapp.generated.resources.Res
import hram.composeapp.generated.resources.ic_activities
import hram.composeapp.generated.resources.ic_record
import org.jetbrains.compose.resources.DrawableResource

enum class TabType(val icon: DrawableResource, val tab: Tab) {
    Activities(Res.drawable.ic_activities, ActivitiesScreen),
    Record(Res.drawable.ic_record, RecordScreen);
}
