package com.facemoji.cut.aide

/**
 * @author : ydli
 * @time : 2020/11/11 8:56
 * @description :
 */
object Constants {
    //区别每个Fragment之间的数据类型
    const val FRAGMENT_VALUE = "fragment_value"
    //是否显示广告
    const val ADS_SHOW = "ads_show"

    //push推送key
    const val KEY_LOCAL_PUSH_INTERVAL_DATA = "local_push_interval_data"
    const val KEY_LOCAL_PUSH_MESSAGE_DATA = "local_push_message_data"

    //setting 开关控制通知key
    const val KEY_SETTING_PUSH_MESSAGE = "key_setting_push_message"

    //内购控制广告广播key
    const val NO_ADS = "no_ads"

    //关于添加到whatapps
    const val _ADD_WHATAPPS = "_add_whatapps"

    //插页广告计数
    var insets:Int = 0

    //Banner广告位置type
    var _drawer = "banner_drawer"
    var _main = "banner_main"
    var _preview = "banner_preview"

}