package com.facemoji.cut.network.entity

import android.os.Parcelable
import com.facemoji.cut.network.entity.GIF
import kotlinx.android.parcel.Parcelize

/**
 * @author : ydli
 * @time : 2020/11/11 8:56
 * @description :
 */
@Parcelize
class ItemBean(var name: String?, var items: MutableList<GIF>? = null) : Parcelable