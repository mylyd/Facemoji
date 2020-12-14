package com.facemoji.cut.activity

import ad.mobo.base.request.AdPull
import com.facemoji.cut.MyApp
import com.facemoji.cut.ads.AdIdsManager
import com.facemoji.cut.ads.AdIdsManager.AdId
import com.facemoji.cut.aide.GrayStatus
import com.facemoji.cut.aide.MyTrack

/**
 * @author : ydli
 * @time : 2020/11/17 15:15
 * @description :
 */
abstract class BaseAdsActivity : BaseActivity() {
    private var mInterstitialAdPull: AdPull? = null

    protected fun getInsert(): AdPull? = StartActivity.getInsert()

    protected fun initInterstitialAd(adPull: AdPull?, adId: AdId) {
        if (adPull == null) {
            initInterstitialAd(adId)
        } else {
            mInterstitialAdPull = adPull
            mInterstitialAdPull!!.handler(interstitialListener)
        }
    }

    protected fun initInterstitialAd(adId: AdId, type: String? = null): AdPull? {
        if (!GrayStatus.ad_on || MyApp.billingManager?.whetherToBuy!!) return null
        mInterstitialAdPull = AdPull().asInterstitial()
            .info(AdIdsManager.buildAdPullInfo(adId))
            .handler(interstitialListener)
        mInterstitialAdPull?.load(this)
        track(MyTrack.request_insert_ad)
        return mInterstitialAdPull
    }

    private var interstitialListener: AdPull.InterstitialListener =
        object : AdPull.InterstitialListener() {

        override fun onClick() {
        }

        override fun onSucess() {
            track(MyTrack.insert_load_success)
        }

        override fun onFailed() {
            track(MyTrack.insert_load_fail)
        }

        override fun onClosed() {
            closeInters()
            if (mInterstitialAdPull == null)
                return
            else {
                mInterstitialAdPull?.load(this@BaseAdsActivity)
                track(MyTrack.request_insert_ad)
            }
        }
    }

    /**
     * 关闭广告时是否finish当前Activity
     *
     * @param finishAfterAdClosed
     * @return 是否正常加载广告
     */
    fun showInterstitialAd(finishAfterAdClosed: Boolean? = false): Boolean {
        if (MyApp.billingManager?.whetherToBuy!!) {
            if (finishAfterAdClosed!!) {
                finish()
                return false
            }
            return false
        }
        if (mInterstitialAdPull != null) {
            if (mInterstitialAdPull?.isLoading!!){
                track(MyTrack.loading_insert_ad)
            }
            if (mInterstitialAdPull!!.show(this)) {
                track(MyTrack.show_insert_ad)
                if (finishAfterAdClosed!!) finish() else return true
            }
        } else {
            initInterstitialAd(AdIdsManager.insert)
        }
        track(MyTrack.not_show_insert_ad)
        return false
    }

    open fun closeInters(){

    }
}