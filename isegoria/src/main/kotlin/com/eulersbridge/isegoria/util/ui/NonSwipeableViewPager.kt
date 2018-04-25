package com.eulersbridge.isegoria.util.ui

/*
  Created by Anthony on 02/04/2015.
 */
import android.annotation.SuppressLint
import android.content.Context
import android.support.v4.view.ViewPager
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class NonSwipeableViewPager : ViewPager {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    override fun canScroll(v: View, checkV: Boolean, dx: Int, x: Int, y: Int) = false

    override fun canScrollHorizontally(direction: Int) = false

    // Never allow swiping to switch between pages
    override fun onInterceptTouchEvent(event: MotionEvent) = false

    @SuppressLint("ClickableViewAccessibility")
    // Never allow swiping to switch between pages
    override fun onTouchEvent(event: MotionEvent) = false
}