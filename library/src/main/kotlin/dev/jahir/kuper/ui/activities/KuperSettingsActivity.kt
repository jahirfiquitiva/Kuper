package dev.jahir.kuper.ui.activities

import dev.jahir.frames.ui.activities.SettingsActivity
import dev.jahir.frames.ui.fragments.SettingsFragment
import dev.jahir.kuper.ui.fragments.KuperSettingsFragment

open class KuperSettingsActivity : SettingsActivity() {
    override fun getSettingsFragment(): SettingsFragment = KuperSettingsFragment()
}