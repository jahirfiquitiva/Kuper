package dev.jahir.kuper.ui.activities

import dev.jahir.frames.ui.activities.SettingsActivity
import dev.jahir.kuper.BuildConfig

class KuperSettingsActivity : SettingsActivity() {
    override val dashboardName: String = BuildConfig.DASHBOARD_NAME
    override val dashboardVersion: String = BuildConfig.DASHBOARD_VERSION
}
