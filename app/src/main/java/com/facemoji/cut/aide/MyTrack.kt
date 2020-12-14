package com.facemoji.cut.aide

/**
 * @author : ydli
 * @time : 2020/11/11 8:56
 * @description :
 */
interface MyTrack {
    companion object {
        /*开屏广告*/
        //首次启动应用的开屏广告展示次数
        const val open_ads_first_show = "open_ads_first_show"

        // 开屏广告展示总次数
        const val open_ads_show = "open_ads_show"

        // 开屏广告跳过点击次数
        const val open_ads_skip_click = "open_ads_skip_click"

        //统计开屏广告冷启动加载时长
        const val open_ads_start_time_0_1 = "open_ads_start_time_0_1" //1-x 单位s
        const val open_ads_start_time_1_2 = "open_ads_start_time_1_2"
        const val open_ads_start_time_2_3 = "open_ads_start_time_2_3"
        const val open_ads_start_time_3_4 = "open_ads_start_time_3_4"
        const val open_ads_start_time_4_5 = "open_ads_start_time_4_5"
        const val open_ads_start_time_5_6 = "open_ads_start_time_5_6"
        const val open_ads_start_time_6_7 = "open_ads_start_time_6_7"
        const val open_ads_start_time_7_8 = "open_ads_start_time_7_8"
        const val open_ads_start_time_8_9 = "open_ads_start_time_8_9"
        const val open_ads_start_time_9_10 = "open_ads_start_time_9_10"
        const val open_ads_start_time_10_ = "open_ads_start_time_10_" //大于10 s

        //冷启动加载失败
        const val open_ads_start_fail = "open_ads_start_fail"

        /*侧边栏*/
        //侧边栏打开次数
        const val drawer_layout_open_num = "drawer_layout_open_num"

        //主页侧边栏打开按钮点击次数
        const val main_drawer_open_click = "main_drawer_open_click"

        //侧栏内「setting」按钮点击次数
        const val click_setting = "click_setting"

        //侧边栏「no ads」按钮点击次数
        const val click_premium_upgrade = "click_premium_upgrade"

        //侧栏内「share」点击次数
        const val click_share_app = "click_share_app"

        //侧栏内「like it?」按钮点击次数
        const val click_like_it = "click_like_it"

        //侧栏内隐私政策点击次数
        const val click_privacy = "click_privacy"

        //侧栏内左上角关闭按钮点击次数
        const val click_shut_drawer = "click_shut_drawer"

        //评分页「dislike」点击次数
        const val click_likeit_dislike = "click_likeit_dislike"

        //评分页「5 start」点击次数
        const val click_likeit_5star = "click_likeit_5star"

        //反馈页「submit」点击次数
        const val click_likeit_feedback_submit = "click_likeit_feedback_submit"

        //setting页关闭push点击次数
        const val click_setting_push = "click_setting_push"

        //侧边栏more apps 收起次数
        const val drawer_more_app_open = "drawer_more_app_open"

        //侧边栏more apps 打开次数
        const val drawer_more_app_shut = "drawer_more_app_shut"

        //more app下「Lovemoji」点击次数
        const val click_moreapp_lovemoji = "click_moreapp_lovemoji"

        //more app下「Vemoji」点击次数
        const val click_moreapp_vemoji = "click_moreapp_vemoji"

        //more app下「Smilemoji」点击次数
        const val click_moreapp_smilemoji = "click_moreapp_smilemoji"

        /*插页广告*/
        //插页广告请求次数
        const val request_insert_ad = "request_insert_ad"

        //插页广告显示时处于loading过程次数
        const val loading_insert_ad = "loading_insert_ad"

        //插页广告加载成功
        const val insert_load_success = "insert_load_success"

        //插页广告加载失败
        const val insert_load_fail = "insert_load_fail"

        //插页广告展示成功次数
        const val show_insert_ad = "show_insert_ad"

        //插页广告未展示次数
        const val not_show_insert_ad = "not_show_insert_ad"

        /*banner广告*/
        //侧边栏底部自适应广告加载成功次数（成功即展示）
        const val banner_drawer_load_success = "banner_drawer_load_success"

        //侧边栏底部自适应广告加载失败次数
        const val banner_drawer_load_fail = "banner_drawer_load_fail"

        //主页底部自适应广告加载成功次数
        const val banner_main_load_success = "banner_main_load_success"

        //主页底部栏自适应广告加载失败次数
        const val banner_main_load_fail = "banner_main_load_fail"

        //详情页底部自适应广告加载成功次数
        const val banner_preview_load_success = "banner_preview_load_success"

        //详情页底部栏自适应广告加载失败次数
        const val banner_preview_load_fail = "banner_preview_load_fail"

        //侧边栏底部自适应广告点击次数
        const val banner_drawer_click = "banner_drawer_click"

        //主页底部自适应广告点击次数
        const val banner_main_click = "banner_main_click"

        //详情页底部自适应广告点击次数
        const val banner_preview_click = "banner_preview_click"

        /*主页feed list部分*/
        //顶部订阅横幅点击次数
        const val main_feed_vip_click = "main_feed_vip_click"

        //feed list smilemoji导流点击次数
        const val main_feed_ad_smilemoji = "main_feed_ad_smilemoji"

        //feed list lovemoji导流点击次数
        const val main_feed_ad_lovemoji = "main_feed_ad_lovemoji"

        //feed list vemoji导流点击次数
        const val main_feed_ad_vemoji = "main_feed_ad_vemoji"

        //表情包分类点击情况
        const val main_feed_emoji_ = "main_feed_emoji_"

        //详情页点击add whatapp按钮的次数
        const val preview_add_click_ = "preview_add_click_"

        //详情页表情包点击总次数
        const val preview_item_click_all = "preview_item_click_all"
    }
}