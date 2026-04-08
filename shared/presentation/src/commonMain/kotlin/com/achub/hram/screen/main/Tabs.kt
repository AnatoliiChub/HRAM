package com.achub.hram.screen.main

import com.achub.hram.view.tabs.TabType
import hram.composeapp.generated.resources.Res
import hram.composeapp.generated.resources.ic_activities
import hram.composeapp.generated.resources.ic_record
import hram.composeapp.generated.resources.main_tab_activity
import hram.composeapp.generated.resources.main_tab_record

object Activities : TabType {
    override val icon = Res.drawable.ic_activities
    override val label = Res.string.main_tab_activity
}

object Record : TabType {
    override val icon = Res.drawable.ic_record
    override val label = Res.string.main_tab_record
}

