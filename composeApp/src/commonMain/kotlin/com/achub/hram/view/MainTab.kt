package com.achub.hram.view

import hram.composeapp.generated.resources.Res
import hram.composeapp.generated.resources.ic_activities
import hram.composeapp.generated.resources.ic_record
import org.jetbrains.compose.resources.DrawableResource

enum class MainTab(val icon: DrawableResource) {
    Activities(Res.drawable.ic_activities),
    Record(Res.drawable.ic_record);
}