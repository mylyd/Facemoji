package com.facemoji.cut.network.entity

/**
 * @author : ydli
 * @time : 2020/11/11 8:56
 * @description :
 */
class GrayItem {
    var status = 0
    var msg: String? = null
    var data: List<DataBean>? = null

    class DataBean {
        var isStatus = false
        var tag: String? = null
    }
}