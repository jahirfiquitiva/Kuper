package dev.jahir.kuper.ui.activities

import dev.jahir.frames.ui.activities.AboutActivity
import dev.jahir.kuper.BuildConfig

class KuperAboutActivity : AboutActivity() {
    override val dashboardName
    get() = BuildConfig.DASHBOARD_NAME
}
