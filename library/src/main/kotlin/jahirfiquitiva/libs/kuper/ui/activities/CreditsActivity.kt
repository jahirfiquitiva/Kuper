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

import jahirfiquitiva.libs.frames.ui.activities.CreditsActivity
import jahirfiquitiva.libs.kuper.R

class CreditsActivity : CreditsActivity() {
    override fun getDashboardTitle(): Int = R.string.kuper_dashboard
    override fun getTranslationSite(): String = "http://j.mp/KuperTranslations"
}