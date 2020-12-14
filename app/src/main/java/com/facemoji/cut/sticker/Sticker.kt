/*
 * Copyright (c) WhatsApp Inc. and its affiliates.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.facemoji.cut.sticker

import android.os.Parcel
import android.os.Parcelable
import java.util.*

class Sticker : Parcelable {
    @JvmField
    val imageFileName: String?
    @JvmField
    val emojis: List<String>?
    @JvmField
    var size: Long = 0

    internal constructor(imageFileName: String?, emojis: List<String>?) {
        this.imageFileName = imageFileName
        this.emojis = emojis
    }

    constructor(imageFileName: String?) {
        this.imageFileName = imageFileName
        emojis = Arrays.asList("", "")
    }

    private constructor(`in`: Parcel) {
        imageFileName = `in`.readString()
        emojis = `in`.createStringArrayList()
        size = `in`.readLong()
    }

    fun setSize(size: Long) {
        this.size = size
    }

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(imageFileName)
        dest.writeStringList(emojis)
        dest.writeLong(size)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<Sticker> = object : Parcelable.Creator<Sticker> {
            override fun createFromParcel(`in`: Parcel): Sticker {
                return Sticker(`in`)
            }

            override fun newArray(size: Int): Array<Sticker?> {
                return arrayOfNulls(size)
            }
        }
    }
}