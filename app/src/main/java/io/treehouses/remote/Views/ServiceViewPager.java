package io.treehouses.remote.Views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.viewpager.widget.ViewPager;

public class ServiceViewPager extends ViewPager {
    private boolean isPagingEnabled = true;

        public ServiceViewPager(Context context) {
            super(context);
        }

        public ServiceViewPager(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            return this.isPagingEnabled && super.onTouchEvent(event);
        }

        @Override
        public boolean onInterceptTouchEvent(MotionEvent event) {
            return this.isPagingEnabled && super.onInterceptTouchEvent(event);
        }

        public void setPagingEnabled(boolean b) {
            this.isPagingEnabled = b;
        }
}