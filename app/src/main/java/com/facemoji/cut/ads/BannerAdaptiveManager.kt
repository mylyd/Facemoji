package com.facemoji.cut.ads

import android.app.Activity
import android.app.Application.ActivityLifecycleCallbacks
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.IdRes
import androidx.core.view.marginLeft
import androidx.core.view.marginRight
import com.facemoji.cut.BuildConfig
import com.facemoji.cut.MyApp
import com.facemoji.cut.aide.Constants._drawer
import com.facemoji.cut.aide.Constants._main
import com.facemoji.cut.aide.Constants._preview
import com.facemoji.cut.aide.MyTrack
import com.facemoji.cut.aide.tracker.FirebaseTracker
import com.facemoji.cut.utils.Util
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

/**
 * @author : ydli
 * @time : 2020/10/21 17:18
 * @description : 目前在 Android Api 8.0+ 以上存在设置Big Banner滑动抖动问题，*暂时还未定位到具体原因
 */
class BannerAdaptiveManager : ActivityLifecycleCallbacks {
    private var adView: AdView? = null
    private var activity: Activity? = null
    private var frameLayout: FrameLayout? = null
    private var type: String? = null
    private var sizeType: Boolean = false // 是否启用Big Banner
    private var isNested: Boolean = false // 是否存在嵌套布局，eg:自定义边距或者圆角类型广告

    fun setActivity(activity: Activity?): BannerAdaptiveManager {
        this.activity = activity
        return bannerAdaptiveManager!!
    }

    fun setLayout(@IdRes ids: Int): BannerAdaptiveManager {
        frameLayout = activity!!.findViewById(ids)
        return bannerAdaptiveManager!!
    }

    fun setLayout(layout: FrameLayout): BannerAdaptiveManager {
        frameLayout = layout
        return bannerAdaptiveManager!!
    }

    fun initAd(ids: AdIdsManager.AdId) {
        if (frameLayout != null) {
            frameLayout!!.post { loadBanner(ids.admobIds!![0]) }
        }
    }

    /**
     * 必设置项 （设置Banner的大小）
     * @param sizeType 设置自适应Banner的尺寸
     * @true 会出现大Banner  @false 小尺寸
    */
    fun setAdaptive(sizeType :Boolean): BannerAdaptiveManager {
        this.sizeType = sizeType
        return bannerAdaptiveManager!!
    }

    /**
     * 必设置项 （直接影响到Banner的适配工作）
     * @param nested 设置是否存在嵌套类型
     * @true 嵌套  @false 非嵌套
     */
    fun setNested(nested :Boolean): BannerAdaptiveManager {
        this.isNested = nested
        return bannerAdaptiveManager!!
    }

    fun setType(type: String): BannerAdaptiveManager {
        this.type = type
        return bannerAdaptiveManager!!
    }

    private fun loadBanner(id: String) {
        if (!MyApp.billingManager?.whetherToBuy!! && frameLayout?.visibility == View.GONE){
            frameLayout?.visibility = View.VISIBLE
        }
        adView = AdView(activity)
        adView?.adUnitId = id
        frameLayout!!.removeAllViews()
        frameLayout!!.addView(adView)
        adView!!.adSize = adSize

        val adRequest = AdRequest.Builder().build()
        adView!!.loadAd(adRequest)
        adView!!.adListener = object : AdListener() {
            override fun onAdClicked() {
                super.onAdClicked()
                Log.d(TAG, "Ad is banner adaptive clicked $type")
                when(type){
                    _drawer ->{
                        FirebaseTracker.instance.track(MyTrack.banner_drawer_click)
                    }
                    _main ->{
                        FirebaseTracker.instance.track(MyTrack.banner_main_click)
                    }
                    _preview ->{
                        FirebaseTracker.instance.track(MyTrack.banner_preview_click)
                    }
                }
            }

            override fun onAdClosed() {
                super.onAdClosed()
                Log.d(TAG, "Ad is banner adaptive closed")
            }

            override fun onAdFailedToLoad(i: Int) {
                super.onAdFailedToLoad(i)
                Log.d(TAG, "Ad is banner adaptive failed  (code :)$i")
                when(type){
                    _drawer ->{
                        FirebaseTracker.instance.track(MyTrack.banner_drawer_load_fail)
                    }
                    _main ->{
                        FirebaseTracker.instance.track(MyTrack.banner_main_load_fail)
                    }
                    _preview ->{
                        FirebaseTracker.instance.track(MyTrack.banner_preview_load_fail)
                    }
                }
            }

            override fun onAdLoaded() {
                super.onAdLoaded()
                when(type){
                    _drawer ->{
                        FirebaseTracker.instance.track(MyTrack.banner_drawer_load_success)
                    }
                    _main ->{
                        FirebaseTracker.instance.track(MyTrack.banner_main_load_success)
                    }
                    _preview ->{
                        FirebaseTracker.instance.track(MyTrack.banner_preview_load_success)
                    }
                }
                Log.d(TAG, "Ad is banner adaptive loaded")
            }
        }
    }

    // 确定用于广告的屏幕宽度
    private val adSize: AdSize
        private get() {
            // 确定用于广告的屏幕宽度
            val display = activity!!.windowManager.defaultDisplay
            val outMetrics = DisplayMetrics()
            display.getMetrics(outMetrics)

            val density = outMetrics.density
            var adWidthPixels = frameLayout!!.width.toFloat()

            // 如果广告尚未布置，则默认为全屏宽度
            if (adWidthPixels == 0f) {
                adWidthPixels = outMetrics.widthPixels.toFloat()
                if (isNested){
                    val parentViews = frameLayout!!.parent as ViewGroup
                    val width = parentViews.width.toFloat()
                    if (width != 0f){
                        adWidthPixels = width
                    }
                }
            }
            val adWidth = Util.px2dip(activity!!, adWidthPixels.toInt())
            Log.d(TAG, "getAdSize: $adWidth")

            return if (sizeType){
                AdSize.getCurrentOrientationInlineAdaptiveBannerAdSize(activity, adWidth)
            }else{
                AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(activity, adWidth)
            }
        }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
    }

    override fun onActivityStarted(activity: Activity) {}
    override fun onActivityResumed(activity: Activity) {
        if (adView != null) {
            adView!!.resume()
        }
    }

    override fun onActivityPaused(activity: Activity) {
        if (adView != null) {
            adView!!.pause()
        }
    }

    override fun onActivityStopped(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
    }

    override fun onActivityDestroyed(activity: Activity) {
        if (adView != null) {
            adView!!.destroy()
        }
    }

    companion object {
        private const val TAG = "BannerAdaptiveManager"
        const val AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"

        private var bannerAdaptiveManager: BannerAdaptiveManager? = null
        val instance: BannerAdaptiveManager
            get() {
                if (bannerAdaptiveManager == null) {
                    bannerAdaptiveManager =
                        BannerAdaptiveManager()
                }
                return bannerAdaptiveManager!!
            }
    }
}