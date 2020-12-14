package com.facemoji.cut.ads

import ad.mobo.base.bean.AdInfo
import ad.mobo.base.bean.PullInfos
import ad.mobo.common.request.AdRequestHelper
import androidx.annotation.ArrayRes
import com.facemoji.cut.BuildConfig
import com.facemoji.cut.MyApp
import com.facemoji.cut.R
import java.util.*

/**
 * author : ydli
 * time   : 2019/11/29
 * desc   : 广告位自定义id,用于识别控制策略
 * version: 1.0
 */
object AdIdsManager {
    private const val BANNER = "BANNER"
    private const val NATIVE = "NATIVE"
    private const val REWARD = "REWARD"
    private const val INTERSTITIAL = "INTERSTITIAL"
    private const val debugFBId = "YOUR_PLACEMENT_ID"

    val homeBanner = AdId(BANNER, "1002", R.array.gms_home_banner)
    val mojiBanner = AdId(BANNER, "1003", R.array.gms_moji_banner)
    val insert = AdId(INTERSTITIAL, "1004", R.array.gms_insert)
    val settingBanner = AdId(BANNER, "1005", R.array.gms_setting_banner)

    fun buildAdPullInfo(adId: AdId): PullInfos? {
        val adInfoList = getAdInfos(adId)
        if (adInfoList.isEmpty()) return null
        return AdRequestHelper.getPullInfos(adId.number, 1, adInfoList)
    }

    private fun getAdInfos(adId: AdId): List<AdInfo> {
        try {
            val infos: ArrayList<AdInfo> = arrayListOf()
            if (adId.admobIds != null && adId.admobIds!!.isNotEmpty()) {
                var admob = "admob"
                for (i in adId.admobIds!!.indices) {
                    if (i != 0) admob = "admob$i"
                    infos.add(AdInfo(admob, adId.admobIds!![i]))
                }
            }
            return infos
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return arrayListOf()
    }

    class AdId(val type: String, val number: String, private val admobId: Int, private val fbId: Int) {
        var moboapps: String? = ""

        constructor(type: String, number: String, admobId: Int) : this(
            type,
            number,
            admobId,
            R.array.fb
        )

        val admobIds: List<String>?
            get() {
                if (BuildConfig.DEBUG) return getDebugIds(debugAdmobId)
                val arr = getStringArray(admobId)
                return if (arr == null || arr.isEmpty()) null else listOf(*arr)
            }

        val fBIds: List<String>?
            get() {
                if (BuildConfig.DEBUG) return getDebugIds(debugFBId)

                val arr = getStringArray(fbId)
                return if (arr == null || arr.isEmpty()) null else mutableListOf(*arr)
            }

        private fun getStringArray(@ArrayRes resId: Int): Array<String>? {
            return try {
                MyApp.instance.resources.getStringArray(resId)
            } catch (e: Exception) {
                null
            }
        }

        private fun getDebugIds(adId: String): List<String> = mutableListOf(adId)

        private val debugAdmobId: String
            get() = when (type) {
                BANNER -> "ca-app-pub-3940256099942544/6300978111"
                NATIVE -> "ca-app-pub-3940256099942544/2247696110"
                REWARD -> "ca-app-pub-3940256099942544/5224354917"
                INTERSTITIAL -> "ca-app-pub-3940256099942544/1033173712"
                else -> "ca-app-pub-3940256099942544/1033173712"
            }

        init {
            moboapps = if (NATIVE == type) number else ""
        }
    }
}