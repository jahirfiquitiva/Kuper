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
package jahirfiquitiva.libs.kuper.ui.widgets

import android.animation.Animator
import android.annotation.SuppressLint
import android.content.Context
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.Scroller
import jahirfiquitiva.libs.kauextensions.extensions.SimpleAnimatorListener

open class PseudoViewPager : ViewPager {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    
    companion object {
        private const val FADE_OUT_DURATION = 200L
        private const val FADE_IN_DURATION = 250L
    }
    
    init {
        setPageTransformer(false, FadeTransformer())
        setPseudoScroller()
    }
    
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(ev: MotionEvent?) = false
    
    override fun onInterceptTouchEvent(ev: MotionEvent?) = false
    
    override fun canScrollHorizontally(direction: Int) = false
    
    override fun executeKeyEvent(event: KeyEvent) = false
    
    override fun setAdapter(adapter: PagerAdapter?) {
        super.setAdapter(adapter)
        adapter?.let {
            if (it.count <= 5) offscreenPageLimit = it.count
        }
    }
    
    private fun setPseudoScroller() {
        try {
            val viewpager = ViewPager::class.java
            val scroller = viewpager.getDeclaredField("mScroller")
            scroller.isAccessible = true
            scroller.set(this, PseudoScroller(context))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    override fun setCurrentItem(item: Int, smoothScroll: Boolean) {
        animate().alpha(0.0F).setDuration(FADE_OUT_DURATION).setListener(
                object : SimpleAnimatorListener() {
                    override fun onEnd(animator: Animator) {
                        super.onEnd(animator)
                        actualSetCurrentItem(item, false)
                    }
                })
    }
    
    private fun actualSetCurrentItem(item: Int, smoothScroll: Boolean) {
        super.setCurrentItem(item, smoothScroll)
        animate().alpha(1.0F).duration = FADE_IN_DURATION
    }
    
    private class PseudoScroller(context: Context) : Scroller(context, DecelerateInterpolator()) {
        override fun startScroll(startX: Int, startY: Int, dx: Int, dy: Int, duration: Int) {
            super.startScroll(startX, startY, dx, dy, 1)
        }
    }
    
    private class FadeTransformer : ViewPager.PageTransformer {
        override fun transformPage(page: View, position: Float) {
            if (position <= -1.0F || position >= 1.0F) {
                page.translationX = page.width * position
                page.alpha = 0.0F
            } else if (position == 0.0F) {
                page.translationX = page.width * position
                page.alpha = 1.0F
            } else {
                page.translationX = page.width * -position
                page.alpha = 1.0F - Math.abs(position)
            }
        }
    }
}