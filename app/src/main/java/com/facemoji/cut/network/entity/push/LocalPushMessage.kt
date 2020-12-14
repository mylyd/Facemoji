package com.facemoji.cut.network.entity.push

import android.graphics.Bitmap
import android.os.Parcel
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class LocalPushMessage(
    var id: Int = -1,
    var desc: String? = null,
    var title: String? = null,
    var icon_url: String? = null,
    var preview_url: String? = null,
    var link: String? = null,
    var iconBitmap: Bitmap? = null,
    var bannerBitmap: Bitmap? = null
) : Parcelable