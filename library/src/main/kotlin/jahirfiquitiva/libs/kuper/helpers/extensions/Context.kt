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
package jahirfiquitiva.libs.kuper.helpers.extensions

import android.content.Context
import ca.allanwang.kau.utils.darken
import ca.allanwang.kau.utils.lighten
import jahirfiquitiva.libs.kauextensions.extensions.cardBackgroundColor
import jahirfiquitiva.libs.kauextensions.extensions.usesDarkTheme
import jahirfiquitiva.libs.kuper.helpers.utils.KuperKonfigs

val Context.kuperKonfigs
    get() = KuperKonfigs.newInstance("kuper_konfigs", this)

val Context.tilesColor: Int
    get() {
        return if (usesDarkTheme) {
            cardBackgroundColor.lighten(0.1F)
        } else cardBackgroundColor.darken(0.1F)
    }