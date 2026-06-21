package com.hairgrowth.tracker.widget

import androidx.glance.appwidget.GlanceAppWidgetReceiver

class HairGrowthWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = HairGrowthWidget()
}
