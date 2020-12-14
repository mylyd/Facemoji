package com.facemoji.cut.network.entity.push

/**
 * @author : ydli
 * @description 本地推送 全局配置数据
 * @time :
 */
class LocalPushConfig {
    var ret = 0
    var data: DataBean? =
        null

    class DataBean {
        var interval: List<Int>? = null
        var messages: List<List<LocalPushMessage>>? =
            null

    }
}