package com.facemoji.cut

import ad.mobo.mvp.AdManager
import android.annotation.SuppressLint
import android.content.Context
import androidx.multidex.MultiDexApplication
import com.facebook.FacebookSdk
import com.facemoji.cut.ads.AppOpenManager
import com.facemoji.cut.aide.tracker.FirebaseTracker
import com.facemoji.cut.billing.BillingManager
import com.facemoji.cut.network.entity.ItemBean
import com.facemoji.cut.push.LocalPushManager
import com.facemoji.cut.sticker.StickerContentProvider
import com.facemoji.cut.sticker.StickerPack
import com.facemoji.cut.utils.GrayControlManager
import com.facemoji.cut.utils.PresetDataUtils
import java.util.*
import kotlin.collections.ArrayList

/**
 * @author : ydli
 * @time : 2020/11/17 15:13
 * @description :
 */
class MyApp : MultiDexApplication() {
    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var instance: MyApp

        var webp: String = "1.webp"

        var appOpenManager: AppOpenManager? = null
        var billingManager: BillingManager? = null
        var stickerPackList: ArrayList<StickerPack>? = null

    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        FirebaseTracker.instance.init(instance)
        FacebookSdk.setAutoLogAppEventsEnabled(true)
        PresetDataUtils.initPresetData(instance)
        LocalPushManager.instance.requestConfig(this)
        billingManager = BillingManager(this)
        GrayControlManager.requestGray(this)
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        //冷启动，首次启动
        AdManager.getInstance().initContext(this).updateStrategy()
        AdManager.getInstance().initFacebookAd().initAdmob(getString(R.string.gms_app_id))
        //初始化工具类
        appOpenManager = AppOpenManager(this)
    }

    fun initStickerPack(data : MutableList<ItemBean>){
        if (data.isEmpty()) return
        val sticker: ArrayList<StickerPack> = arrayListOf()
        for (items in data) {
            sticker.add(StickerPack(items.name,items.name, webp))
        }
        setStickerPack(sticker)
    }

    fun getStickerPackLists(): ArrayList<StickerPack>? {
        val pack = ArrayList<StickerPack>()
        if (stickerPackList == null){
            return null
        }
        pack.addAll(stickerPackList!!)
        return pack
    }

    private fun setStickerPack(packs: ArrayList<StickerPack>) {
        stickerPackList = packs
        val authority: String = BuildConfig.CONTENT_PROVIDER_AUTHORITY
        for (stickerPack in stickerPackList!!) {
            StickerContentProvider.MATCHER.addURI(authority,
                StickerContentProvider.STICKERS_ASSET + "/" + stickerPack.identifier + "/*",
                StickerContentProvider.STICKERS_ASSET_CODE)
        }
    }

    fun getSingleStickerPack(name: String): StickerPack? {
        for (pack in stickerPackList!!) {
            if (pack.name.equals(name, ignoreCase = true)) {
                return pack
            }
        }
        throw IllegalStateException("not found sticker pack:$name")
    }

}