package com.facemoji.cut.view

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration

/**
 * author : jzhou
 * time   : 2019/11/15
 * desc   : 壁纸列表item间距设置
 * version: 1.0
 */
class GridWallpaperDecoration(private val spacing: Int) : ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View,
        parent: RecyclerView, state: RecyclerView.State) {
        val layoutManager = parent.layoutManager as GridLayoutManager?
            ?: throw IllegalStateException("GridLayoutManager is null.")
        val sizeLookup = layoutManager.spanSizeLookup

        // 计算网格每行最多显示几个
        val spanCount = layoutManager.spanCount
        val position = parent.getChildAdapterPosition(view)
        // 计算item位于第几行
        val groupIndex = sizeLookup.getSpanGroupIndex(position, spanCount)
        // 计算item占用几个span
        if (sizeLookup.getSpanSize(position) == spanCount) {
            outRect.left = spacing
            outRect.right = spacing
        } else {
            // 计算item位于该行的第几个
            val spanIndex = sizeLookup.getSpanIndex(position, spanCount)
            outRect.top = spacing / 2
            outRect.bottom = outRect.top
            when (spanIndex) {
                0 -> {
                    outRect.left = spacing
                    outRect.right = spacing / 2
                }
                spanCount - 1 -> {
                    outRect.right = spacing
                    outRect.left = spacing / 2
                }
                else -> {
                    outRect.right = spacing / 2
                    outRect.left = spacing / 2
                }
            }
        }
    }

}