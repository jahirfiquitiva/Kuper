/*
 * Copyright (c) 2018. Jahir Fiquitiva
 *
 * Licensed under the CreativeCommons Attribution-ShareAlike
 * 4.0 International License. You may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *    http://creativecommons.org/licenses/by-sa/4.0/legalcode
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jahirfiquitiva.libs.kuper.ui.activities

import android.support.v4.app.Fragment
import jahirfiquitiva.libs.frames.ui.activities.SettingsActivity
import jahirfiquitiva.libs.kuper.helpers.utils.KuperKonfigs
import jahirfiquitiva.libs.kuper.ui.fragments.SettingsFragment

class SettingsActivity : SettingsActivity() {
    override val configs: KuperKonfigs by lazy { KuperKonfigs(this) }
    override fun settingsFragment(): Fragment = SettingsFragment()
    override fun getTranslationSite(): String = "http://j.mp/KuperTranslations"
}