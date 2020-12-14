package com.facemoji.cut.utils

import android.content.Context
import com.google.android.gms.common.util.CollectionUtils
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.facemoji.cut.network.entity.ItemBean

/**
 * @author : ydli
 * @time : 2020/11/11 8:56
 * @description :
 */
class PresetDataUtils {

    companion object {
        var PRESET_DATA = "preset_data"
        private val json = SPManager.init().getString(PRESET_DATA)

        fun initPresetData(context: Context) {
            //如果本地存在数据缓存就不去读取数据
            //if (json != null) return
            val jsons: String? = FileUtil.getStringOfJsonFile(context, "facemoji.json")

            val list =
                Gson().fromJson<MutableList<String>>(jsons,
                    object : TypeToken<MutableList<ItemBean?>?>() {}.type)

            if (CollectionUtils.isEmpty(list)) return
            SPManager.init().setString(PRESET_DATA, Gson().toJson(list.subList(0, list.size)))

        }

        fun getPresetData(): MutableList<ItemBean>? {
            return Gson().fromJson<MutableList<ItemBean>>(
                SPManager.init().getString(PRESET_DATA),
                object : TypeToken<MutableList<ItemBean?>?>() {}.type
            )
        }
    }
}