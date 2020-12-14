package com.facemoji.cut.view

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.viewpager.widget.ViewPager

/**
 * 不可以通过触摸左右滑动的ViewPager，setCurrentItem(m)会有滑动效果
 * Created by Administrator on 2017/3/29.
 */
class UnTouchScrollViewPager : ViewPager {
    private var onTouch: Boolean? = false

    constructor(context: Context?) : super(context!!)

    constructor(context: Context?, attrs: AttributeSet?) : super(context!!, attrs)

    override fun onTouchEvent(ev: MotionEvent): Boolean = false

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean = false

    public fun setOnTouch(touch : Boolean){
        this.onTouch = touch
    }

}