package io.treehouses.remote.views

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.viewpager.widget.ViewPager

class ServiceViewPager : ViewPager {
    private var isPagingEnabled = true

    constructor(context: Context?) : super(context!!) {
        setPageTransformer(true, ZoomOutPageTransformer())
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context!!, attrs) {
        val zopTransformer = ZoomOutPageTransformer()
        setPageTransformer(true, zopTransformer)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        return isPagingEnabled && super.onTouchEvent(event)
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        return isPagingEnabled && super.onInterceptTouchEvent(event)
    }

    fun setPagingEnabled(b: Boolean) {
        isPagingEnabled = b
    }

    class ZoomOutPageTransformer : PageTransformer {
        override fun transformPage(view: View, position: Float) {
            val pageWidth = view.width
            val pageHeight = view.height

            // Modify the default slide transition to shrink the page as well
            val scaleFactor = Math.max(MIN_SCALE, 1 - Math.abs(position))
            val vertMargin = pageHeight * (1 - scaleFactor) / 2
            val horzMargin = pageWidth * (1 - scaleFactor) / 2
            val alpha = MIN_ALPHA + (scaleFactor - MIN_SCALE) / (1 - MIN_SCALE) * (1 - MIN_ALPHA)
            if (position < -1) { // [-Infinity,-1)
                // This page is way off-screen to the left.
                view.alpha = 0f
            } else if (position in 0.0..1.0) { // [0,1]
                view.translationX = -horzMargin + vertMargin / 2
                view.scaleX = scaleFactor
                view.scaleY = scaleFactor

                // Fade the page relative to its size.
                view.alpha = alpha
            } else if (position < 0) { // [-1, 0)
                view.translationX = horzMargin - vertMargin / 2
                view.scaleX = scaleFactor
                view.scaleY = scaleFactor

                // Fade the page relative to its size.
                view.alpha = alpha
            } else { // (1,+Infinity]
                // This page is way off-screen to the right.
                view.alpha = 0f
            }
        }

        companion object {
            private const val MIN_SCALE = 0.85f
            private const val MIN_ALPHA = 0.5f
        }
    }
}