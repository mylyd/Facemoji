package com.facemoji.cut.ads

import android.app.Activity
import android.app.Application.ActivityLifecycleCallbacks
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import com.facemoji.cut.BuildConfig
import com.facemoji.cut.MyApp
import com.facemoji.cut.activity.StartActivity
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.appopen.AppOpenAd.AppOpenAdLoadCallback
import com.facemoji.cut.aide.MyTrack
import com.facemoji.cut.aide.tracker.FirebaseTracker
import java.util.*

/**
 * @author : ydli
 * @time : 20-10-19 下午6:26
 * @description 开屏广告Manager
 */
class AppOpenManager(private val myApp: MyApp) : LifecycleObserver, ActivityLifecycleCallbacks {
    /**
     * 获取当前广告对象
     *
     * @return
     */
    //应用程序打开广告实例，用于存储预加载的广告
    var appOpenAd: AppOpenAd? = null
        private set

    //广告加载抽象回调
    private var loadCallback: AppOpenAdLoadCallback? = null

    //检查广告是否仍然有效
    private var loadTime: Long = 0

    //最近执行的活动
    private var mostCurrentActivity: Activity? = null

    /**
     * 当前广告是否处于显示状态
     *
     * @return
     */
    //当前是否正在显示开屏广告
    var isShowingAd = false
        private set

    //开屏页 Activity Name
    private val startActivityName = StartActivity::class.java.simpleName

    //当前显示的 Activity Name
    private var currentAcName: Activity? = null

    //判断是否是第一次进行加载，类比对于冷启动
    private var isColdStart = true

    //开始加载时间
    private var timeStart: Long = 0

    //加载结束时间
    private var timeEnd: Long = 0

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onStart() {
        //使用lifecycle完成处理其他界面下的热启动
        Log.d(TAG, "LifecycleEvent onStart")
        showAdIfAvailable()
    }

    /**
     * 加载广告
     */
    private fun fetchAd() {
        if (isColdStart && (timeStart == 0L)){
            timeStart = System.currentTimeMillis()
        }
        //存在缓存广告时不用新的获取
        if (isAdAvailable) return
        loadCallback = object : AppOpenAdLoadCallback() {
            /**
             * 广告加载成功广告后调用
             */
            override fun onAppOpenAdLoaded(appOpenAd: AppOpenAd) {
                this@AppOpenManager.appOpenAd = appOpenAd
                if (isColdStart){
                    timeEnd = System.currentTimeMillis()
                    val time = (timeEnd - timeStart) / 1000
                    Log.d(TAG, "open load time (s) : $time")
                    startTime(time)
                    isColdStart = false
                }
                loadTime = Date().time
                loadTime = Date().time
                Log.d(TAG, "Get the current Activity : ${getCurrentActivityName(mostCurrentActivity)}")
                //首次进入时，加载成功便展示，冷启动模式
                if (isStartActivity) {
                    showAdIfAvailable()
                }
            }

            /**
             * 广告加载失败时调用
             */
            override fun onAppOpenAdFailedToLoad(loadAdError: LoadAdError) {
                super.onAppOpenAdFailedToLoad(loadAdError)
                Log.d(TAG, "App open ad load error $loadAdError")
                if (isColdStart){
                    FirebaseTracker.instance.track(MyTrack.open_ads_start_fail)
                    isColdStart = false
                }
            }
        }
        AppOpenAd.load(myApp, if (BuildConfig.DEBUG) AD_UNIT_TEST_ID else AD_UNIT_ID,
            adRequest,
            AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT,  //竖屏模式
            loadCallback
        )
    }

    /**
     * 显示广告
     */
    private fun showAdIfAvailable() {
        // 仅在当前没有正在显示的广告时显示广告
        if (!isShowingAd && isAdAvailable) {
            currentAcName = mostCurrentActivity
            Log.d(TAG, "Will show ad  : ${getCurrentActivityName(mostCurrentActivity)}")
            val fullScreenContentCallback: FullScreenContentCallback =
                object : FullScreenContentCallback() {
                    /**
                     * 显示失败
                     * @param adError
                     */
                    override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                        super.onAdFailedToShowFullScreenContent(adError)
                        Log.d(TAG, "Add open ad show error")
                    }

                    /**
                     * 显示成功
                     */
                    override fun onAdShowedFullScreenContent() {
                        if (isStartActivity) {
                            FirebaseTracker.instance.track(MyTrack.open_ads_first_show)
                        }
                        FirebaseTracker.instance.track(MyTrack.open_ads_show)
                        isShowingAd = true
                    }

                    /**
                     * 关闭显示 && 点击跳过
                     */
                    override fun onAdDismissedFullScreenContent() {
                        /* 运行到这里时，当前的activity会替换成广告内部的AdActivity,
                     即使用currentAcName来区别是否点击的是处于开屏页的 跳过&关闭 按钮 */
                        Log.d(TAG, "onclick app open ad dismiss")
                        FirebaseTracker.instance.track(MyTrack.open_ads_skip_click)
                        appOpenAd = null
                        isShowingAd = false
                        //如果处于开屏界面展示了广告，关闭广告时，进入主页面
                        if (getCurrentActivityName(currentAcName) == startActivityName) {
                            currentAcName?.let { StartActivity.newStart(it) }
                        }
                        //重置当前Activity字段
                        currentAcName = null
                        //查看完开屏广告后，重新加载进行缓存
                        fetchAd()
                    }
                }
            appOpenAd!!.show(mostCurrentActivity, fullScreenContentCallback)
        } else {
            Log.d(TAG, "Can not show ad")
            fetchAd()
        }
    }

    private fun wasLoadTimeLessThanNHoursAgo(): Boolean {
        val dateDifference = Date().time - loadTime
        val numMilliSecondsPerHour: Long = 3600000
        return dateDifference < numMilliSecondsPerHour * 4
    }

    /**
     * 创建并返回广告请求
     *
     * @return
     */
    private val adRequest: AdRequest get() = AdRequest.Builder().build()

    /**
     * 检查广告是否存在并可以显示
     *
     * @return
     */
    private val isAdAvailable: Boolean get() = appOpenAd != null && wasLoadTimeLessThanNHoursAgo()

    /**
     * 是否处于开屏页面
     *
     * @return
     */
    val isStartActivity: Boolean get() = mostCurrentActivity != null &&
            getCurrentActivityName(mostCurrentActivity) == startActivityName

    /**
     * 获取当前 Activity名字
     *
     * @return
     */
    fun getCurrentActivityName(activity: Activity?): String? = activity?.javaClass?.simpleName

    private fun startTime(time: Long) {
        when(time){
            in 0L..1L ->{
                FirebaseTracker.instance.track(MyTrack.open_ads_start_time_0_1)
            }
            in 1L..2L -> {
                FirebaseTracker.instance.track(MyTrack.open_ads_start_time_1_2)
            }
            in 2L..3L -> {
                FirebaseTracker.instance.track(MyTrack.open_ads_start_time_2_3)
            }
            in 3L..4L -> {
                FirebaseTracker.instance.track(MyTrack.open_ads_start_time_3_4)
            }
            in 4L..5L -> {
                FirebaseTracker.instance.track(MyTrack.open_ads_start_time_4_5)
            }
            in 5L..6L -> {
                FirebaseTracker.instance.track(MyTrack.open_ads_start_time_5_6)
            }
            in 6L..7L -> {
                FirebaseTracker.instance.track(MyTrack.open_ads_start_time_6_7)
            }
            in 7L..8L -> {
                FirebaseTracker.instance.track(MyTrack.open_ads_start_time_7_8)
            }
            in 8L..9L -> {
                FirebaseTracker.instance.track(MyTrack.open_ads_start_time_8_9)
            }
            in 9L..10L -> {
                FirebaseTracker.instance.track(MyTrack.open_ads_start_time_9_10)
            }
            else -> {
                FirebaseTracker.instance.track(MyTrack.open_ads_start_time_10_)
            }
        }
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
    }

    override fun onActivityStarted(activity: Activity) {
        mostCurrentActivity = activity
    }

    override fun onActivityResumed(activity: Activity) {
        mostCurrentActivity = activity
    }

    override fun onActivityPaused(activity: Activity) {}

    override fun onActivityStopped(activity: Activity) {}

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

    override fun onActivityDestroyed(activity: Activity) {
        mostCurrentActivity = null
    }

    companion object {
        private const val TAG = "AppOpenManager"

        //正式广告ID
        private const val AD_UNIT_ID = "ca-app-pub-3707640778474213/7074547804"

        //测试广告ID
        private const val AD_UNIT_TEST_ID = "ca-app-pub-3940256099942544/1033173712"
    }

    init {
        //注册跟踪用户正在使用的最新活动时间监听
        myApp.registerActivityLifecycleCallbacks(this)
        //注册前台事件监听
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }
}