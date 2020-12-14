package com.facemoji.cut.utils

import android.content.Context
import android.os.Build
import android.util.Log
import com.facemoji.cut.aide.GrayStatus
import com.facemoji.cut.network.CommonCallback
import com.facemoji.cut.network.Request
import com.facemoji.cut.network.RetrofitNetwork
import com.facemoji.cut.network.entity.GrayItem
import com.facemoji.cut.utils.Util.getAndroidId
import com.facemoji.cut.utils.Util.getStringMD5
import com.facemoji.cut.utils.Util.getVersionCode
import com.facemoji.cut.utils.Util.getVersionName
import java.util.*

/**
 * @author : ydli
 * @time : 2020/11/11 8:56
 * @description :
 */
object GrayControlManager {
    private val TAG = GrayControlManager::class.java.simpleName

    fun requestGray(context: Context) {
        val queryParams: MutableMap<String, String> = hashMapOf()
        queryParams["did"] = getStringMD5(getAndroidId(context))
        if (Locale.getDefault().toString() == "zh_CN_#Hans") {
            queryParams["lc"] = "zh_CN"
        } else {
            queryParams["lc"] = Locale.getDefault().toString()
        }
        queryParams["pn"] = context.packageName
        queryParams["appvc"] = getVersionCode(context)
        queryParams["appvn"] = getVersionName(context)
        queryParams["os"] = "android"
        queryParams["chn"] = "ofw"
        queryParams["avn"] = Build.VERSION.SDK_INT.toString()
        RetrofitNetwork.INSTANCE.getRequest<Request>().getSwitchConfig(queryParams)
            ?.enqueue(object : CommonCallback<GrayItem?>() {
                override fun onResponse(response: GrayItem?) {
                    if (response?.data != null) {
                        val list = response.data
                        for (bean in list!!) {
                            Log.d(TAG, bean.tag + ":${bean.isStatus}")
                            when (bean.tag) {
                                "ad_on" -> GrayStatus.ad_on = bean.isStatus
                                "push" -> GrayStatus.push = bean.isStatus
                            }
                        }
                    }
                }

                override fun onFailure(t: Throwable?, isServerUnavailable: Boolean) {
                    Log.d(TAG, "onFailure")
                }
            })
    }
}