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

class StickerPack : Parcelable {
    var identifier: String? = null
    var name: String? = null
    var publisher: String? = null
    var trayImageFile: String? = null
    var publisherEmail: String? = null
    var publisherWebsite: String? = null
    var privacyPolicyWebsite: String? = null
    var licenseAgreementWebsite: String? = null
    var imageDataVersion: String? = null
    var avoidCache: Boolean = false
    @JvmField
    var iosAppStoreLink: String? = null
    var stickers: ArrayList<Sticker>? = null

    var totalSize: Long = 0
        private set
    @JvmField
    var androidPlayStoreLink: String? = null
    var isWhitelisted = false

    internal constructor(identifier: String?, name: String?, publisher: String?,
                         trayImageFile: String?, publisherEmail: String?, publisherWebsite: String?,
        privacyPolicyWebsite: String?, licenseAgreementWebsite: String?, imageDataVersion: String?,
        avoidCache: Boolean) {
        this.identifier = identifier
        this.name = name
        this.publisher = publisher
        this.trayImageFile = trayImageFile
        this.publisherEmail = publisherEmail
        this.publisherWebsite = publisherWebsite
        this.privacyPolicyWebsite = privacyPolicyWebsite
        this.licenseAgreementWebsite = licenseAgreementWebsite
        this.imageDataVersion = imageDataVersion
        this.avoidCache = avoidCache
    }

    constructor(identifier: String?, name: String?, trayImage: String?) {
        this.identifier = identifier
        this.name = name
        publisher = "facemoji"
        trayImageFile = trayImage //不能为空
        publisherEmail = ""
        publisherWebsite = "https://stickers-wastickerapps.com"
        privacyPolicyWebsite = "https://stickers-for-whatsap.flycricket.io/privacy.html"
        licenseAgreementWebsite = ""
        imageDataVersion = "1"
        avoidCache = false
    }

    private constructor(`in`: Parcel) {
        identifier = `in`.readString()
        name = `in`.readString()
        publisher = `in`.readString()
        trayImageFile = `in`.readString()
        publisherEmail = `in`.readString()
        publisherWebsite = `in`.readString()
        privacyPolicyWebsite = `in`.readString()
        licenseAgreementWebsite = `in`.readString()
        iosAppStoreLink = `in`.readString()
        stickers = `in`.createTypedArrayList(Sticker.CREATOR)
        totalSize = `in`.readLong()
        androidPlayStoreLink = `in`.readString()
        isWhitelisted = `in`.readByte().toInt() != 0
        imageDataVersion = `in`.readString()
        avoidCache = `in`.readByte().toInt() != 0
    }

    fun setSticker(stickers: ArrayList<Sticker>) {
        this.stickers = stickers
        totalSize = 0
        for (sticker in stickers) {
            totalSize += sticker.size
        }
    }

    fun setAndroidPlayStoreLink(androidPlayStoreLink: String?) {
        this.androidPlayStoreLink = androidPlayStoreLink
    }

    fun setIosAppStoreLink(iosAppStoreLink: String?) {
        this.iosAppStoreLink = iosAppStoreLink
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(identifier)
        dest.writeString(name)
        dest.writeString(publisher)
        dest.writeString(trayImageFile)
        dest.writeString(publisherEmail)
        dest.writeString(publisherWebsite)
        dest.writeString(privacyPolicyWebsite)
        dest.writeString(licenseAgreementWebsite)
        dest.writeString(iosAppStoreLink)
        dest.writeTypedList(stickers)
        dest.writeLong(totalSize)
        dest.writeString(androidPlayStoreLink)
        dest.writeByte((if (isWhitelisted) 1 else 0).toByte())
        dest.writeString(imageDataVersion)
        dest.writeByte((if (avoidCache) 1 else 0).toByte())
    }

    companion object {
        val CREATOR: Parcelable.Creator<StickerPack?> = object : Parcelable.Creator<StickerPack?> {
            override fun createFromParcel(`in`: Parcel): StickerPack? {
                return StickerPack(`in`)
            }

            override fun newArray(size: Int): Array<StickerPack?> {
                return arrayOfNulls(size)
            }
        }
    }
}