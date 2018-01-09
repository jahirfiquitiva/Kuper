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
package jahirfiquitiva.libs.kuper.ui.decorations

import android.graphics.Rect
import android.support.v7.widget.RecyclerView
import android.view.View
import jahirfiquitiva.libs.kuper.ui.adapters.KuperAdapter

class SectionedGridSpacingDecoration(
        private val spanCount: Int, private val spacing: Int,
        private val includeEdge: Boolean,
        private val adapter: KuperAdapter?
                                    ) : RecyclerView.ItemDecoration() {
    
    override fun getItemOffsets(
            outRect: Rect, view: View, parent: RecyclerView,
            state: RecyclerView.State
                               ) {
        super.getItemOffsets(outRect, view, parent, state)
        
        var position = (view.layoutParams as RecyclerView.LayoutParams).viewAdapterPosition
        val headersBeforeItemPosition = adapter?.getHeadersBeforePosition(position) ?: 0
        
        position -= headersBeforeItemPosition
        
        val column = position % spanCount
        
        if (includeEdge) {
            outRect.left = spacing - column * spacing / spanCount
            outRect.right = (column + 1) * spacing / spanCount
            if (position < spanCount) {
                outRect.top = spacing
            }
            outRect.bottom = spacing
        } else {
            outRect.left = column * spacing / spanCount
            outRect.right = spacing - (column + 1) * spacing / spanCount
            if (position >= spanCount) {
                outRect.top = spacing
            }
        }
    }
}