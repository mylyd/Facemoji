package com.facemoji.cut.activity

import ad.mobo.base.request.AdPull
import android.annotation.SuppressLint
import android.app.Activity
import android.text.TextUtils
import android.widget.TextView
import com.facemoji.cut.MyApp
import com.facemoji.cut.R
import com.facemoji.cut.ads.AdIdsManager
import com.facemoji.cut.utils.PresetDataUtils
import com.facemoji.cut.utils.Util

class StartActivity : BaseAdsActivity() {

    override fun getLayoutId(): Int = R.layout.activity_start

    @SuppressLint("SetTextI18n")
    override fun init() {
        val intent = intent
        if (intent != null) {
            val intentString: String? = intent.getStringExtra("NOTIFICATION_KEY")
            if (!TextUtils.isEmpty(intentString)){
                if (intentString.equals("notification")) {
                    newStart(this)
                }
            }
        }

        Util.setViewPadding(findViewById(R.id.activity_layout), this)
        findViewById<TextView>(R.id.start_version).text =
            "${getString(R.string.app_name)} V${Util.getVersionName(this)}"

        window.decorView.postDelayed({
            if (!MyApp.appOpenManager?.isShowingAd!!) {
                newStart(this)
            }
        }, 3000)
    }

    override fun onStart() {
        super.onStart()
        insert = initInterstitialAd(AdIdsManager.insert)

        PresetDataUtils.getPresetData()?.let { MyApp.instance.initStickerPack(it) }
    }

    companion object {
        private var insert: AdPull? = null

        fun getInsert(): AdPull? = insert

        fun newStart(activity: Activity) {
            MainActivity.newStart(activity)
            activity.finish()
        }
    }
}