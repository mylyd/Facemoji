package com.facemoji.cut.adapter

import com.facemoji.cut.network.entity.GIF
import com.facemoji.cut.network.entity.ItemBean

/**
 * @author : ydli
 * @time : 2020/11/18 14:46
 * @description :
 */
interface OnClickItemGIF {

    fun item(position: Int, items: GIF)

}