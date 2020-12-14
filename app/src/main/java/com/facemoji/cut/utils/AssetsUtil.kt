package com.facemoji.cut.utils

import android.content.Context
import com.google.gson.Gson
import com.facemoji.cut.network.entity.push.LocalPushConfig

/**
 * @Description:
 * @Author: jzhou
 * @CreateDate: 19-8-15 下午9:36
 */
object AssetsUtil {
    //从assets 文件夹中获取文件并读取数据
    fun getJson(context: Context, fileName: String?): String {
        var result = ""
        try {
            val `in` = context.assets.open(fileName!!)
            //获取文件的字节数
            val length = `in`.available()
            //创建byte数组
            val buffer = ByteArray(length)
            //将文件中的数据读到byte数组中
            `in`.read(buffer)
            result = String(buffer)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return result
    }

    /**
     * 获取push本地数据信息
     *
     * @return
     */
    fun getLocalPushList(context: Context): LocalPushConfig.DataBean {
        val json = getJson(context, "local_push_list.json")
        return Gson().fromJson(json, LocalPushConfig.DataBean::class.java)
    }
}