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

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import jahirfiquitiva.libs.kext.ui.fragments.adapters.DynamicFragmentsPagerAdapter
import jahirfiquitiva.libs.kuper.ui.fragments.KuperFragment
import jahirfiquitiva.libs.kuper.ui.fragments.SetupFragment
import jahirfiquitiva.libs.kuper.ui.fragments.WallpapersFragment

internal class KuperSectionsAdapter(
    manager: FragmentManager,
    private val withSetup: Boolean,
    private val withWallpapers: Boolean,
    private val withChecker: Boolean
                                   ) :
    DynamicFragmentsPagerAdapter(manager) {
    
    private val wallpapersSection: Int
        get() = if (withSetup) 2 else 1
    
    private val kuperSection: Int
        get() = if (withSetup) 1 else 0
    
    private val setupSection: Int
        get() = if (withSetup) 0 else -1
    
    override fun createItem(position: Int): Fragment = if (position >= 0) {
        when (position) {
            wallpapersSection -> WallpapersFragment.create(withChecker)
            kuperSection -> KuperFragment()
            setupSection -> SetupFragment()
            else -> Fragment()
        }
    } else Fragment()
    
    override fun getCount(): Int {
        return if (withWallpapers) {
            if (withSetup) 3 else 2
        } else {
            if (withSetup) 2 else 1
        }
    }
}