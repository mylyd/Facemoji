package com.facemoji.cut.activity

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.os.AsyncTask
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.facemoji.cut.MyApp
import com.facemoji.cut.R
import com.facemoji.cut.adapter.HomePagerAdapter
import com.facemoji.cut.adapter.OnClickItem
import com.facemoji.cut.ads.AdIdsManager
import com.facemoji.cut.ads.AdIdsManager.AdId
import com.facemoji.cut.ads.BannerAdaptiveManager
import com.facemoji.cut.aide.Constants._drawer
import com.facemoji.cut.aide.Constants._main
import com.facemoji.cut.aide.GrayStatus
import com.facemoji.cut.aide.MyTrack
import com.facemoji.cut.network.entity.AddPreview
import com.facemoji.cut.network.entity.ItemBean
import com.facemoji.cut.sticker.WhitelistCheck
import com.facemoji.cut.utils.*
import com.google.android.gms.common.util.CollectionUtils
import java.lang.ref.WeakReference

class MainActivity : BaseAdsActivity(), View.OnClickListener, DrawerLayout.DrawerListener {
    private var drawerLayout: DrawerLayout? = null
    private var version: TextView? = null
    private var privacy: TextView? = null
    private var vemoji: TextView? = null
    private var lovemoji: TextView? = null
    private var smilemoji: TextView? = null
    private var recyclerView: RecyclerView? = null
    private var layoutShare: LinearLayout? = null
    private var layoutLike: LinearLayout? = null
    private var layoutVip: LinearLayout? = null
    private var layoutSetting: LinearLayout? = null
    private var layoutMoreApp: LinearLayout? = null
    private var more: ImageView? = null
    private var actionDrawer: ImageView? = null
    private var actionDrawerBack: ImageView? = null
    private var moreLayout: LinearLayout? = null
    private var items: MutableList<ItemBean>? = mutableListOf()
    private var adapter: HomePagerAdapter? = null
    private var adsLayoutDrawer: FrameLayout? = null
    private var adsLayoutHome: FrameLayout? = null
    private var isDrawerStatus: Boolean = false
    private var whiteListCheckAsyncTask: WhiteListCheckAsyncTask? = null


    override fun getLayoutId(): Int = R.layout.activity_main

    @SuppressLint("SetTextI18n")
    override fun init() {
        instance = this
        Util.setViewPadding(findViewById(R.id.activity_layout), this)
        Util.setViewPadding(findViewById(R.id.drawer_activity_layout), this)
        initActionBarHeight()
        initActionBarHeight(R.id.action_bar_drawer)
        drawerLayout = findViewById(R.id.drawer_layout)
        drawerLayout?.addDrawerListener(this)
        version = findViewById(R.id.version)
        privacy = findViewById(R.id.privacy)
        lovemoji = findViewById(R.id.diversion_lovemoji)
        smilemoji = findViewById(R.id.diversion_smilemoji)
        vemoji = findViewById(R.id.diversion_vemoji)
        recyclerView = findViewById(R.id.recyclerView)
        layoutShare = findViewById(R.id.item_share)
        layoutLike = findViewById(R.id.item_like)
        layoutVip = findViewById(R.id.item_vip)
        layoutSetting = findViewById(R.id.item_setting)
        layoutMoreApp = findViewById(R.id.more_app)
        more = findViewById(R.id.more)
        actionDrawer = findViewById(R.id.action_drawer)
        actionDrawerBack = findViewById(R.id.action_drawer_back)
        moreLayout = findViewById(R.id.layout_app_more)
        actionDrawer?.setOnClickListener(this)
        actionDrawerBack?.setOnClickListener(this)
        privacy?.setOnClickListener(this)
        layoutSetting?.setOnClickListener(this)
        layoutVip?.setOnClickListener(this)
        layoutLike?.setOnClickListener(this)
        layoutShare?.setOnClickListener(this)
        layoutMoreApp?.setOnClickListener(this)
        lovemoji?.setOnClickListener(this)
        vemoji?.setOnClickListener(this)
        smilemoji?.setOnClickListener(this)
        version?.text = "${getString(R.string.app_name)} V${Util.getVersionName(this@MainActivity)}"
        privacy?.paintFlags = Paint.UNDERLINE_TEXT_FLAG
        adsLayoutDrawer = findViewById(R.id.banner_ad_container)
        adsLayoutHome = findViewById(R.id.banner_ad_container_home)
        items = PresetDataUtils.getPresetData()
        if (items?.isEmpty()!!) {
            return
        }
        initRecyclerView()

        registerBroadcastReceiver(registerBroadcastReceiver())

        initBanner(adsLayoutHome!!, AdIdsManager.homeBanner, big = false, nested = false)
        window.decorView.postDelayed({
            initBanner(adsLayoutDrawer!!, AdIdsManager.settingBanner , big = true, nested = true)
        },1000)

    }

    private inner class registerBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            adsLayoutDrawer?.visibility = View.GONE
            adsLayoutHome?.visibility = View.GONE
        }
    }

    private fun initBanner(container :FrameLayout, ids: AdId, big: Boolean, nested: Boolean) {
        if (!GrayStatus.ad_on || MyApp.billingManager?.whetherToBuy!!) return
        BannerAdaptiveManager.instance
            .setActivity(this)
            .setLayout(container)
            .setAdaptive(big)
            .setNested(nested)
            .setType(if (big) _drawer else _main)
            .initAd(ids)
    }

    private fun initRecyclerView() {
        adapter = HomePagerAdapter(this)
        recyclerView?.layoutManager = LinearLayoutManager(this)
        recyclerView?.adapter = adapter
        upData()
        adapter?.setClickItem(object : OnClickItem {
            override fun item(position: Int, items: ItemBean) {
                when (position) {
                    0 -> {
                        track(MyTrack.main_feed_vip_click)
                        MyApp.billingManager?.showSub(this@MainActivity)
                    }
                    3 -> {
                        track(MyTrack.main_feed_ad_smilemoji)
                        Util.startWebView(
                            this@MainActivity,
                            getString(R.string.diversion_smilemoji_url)
                        )
                    }
                    6 -> {
                        track(MyTrack.main_feed_ad_lovemoji)
                        Util.startWebView(
                            this@MainActivity,
                            getString(R.string.diversion_lovemoji_url)
                        )
                    }
                    9 -> {
                        track(MyTrack.main_feed_ad_vemoji)
                        Util.startWebView(
                            this@MainActivity,
                            getString(R.string.diversion_vemoji_url)
                        )
                    }
                    else -> {
                        val string = StringUtils.splitCapital(items.name!!, " ", "_",
                            capital = false, down = false)
                        track(MyTrack.main_feed_emoji_, string)
                        PreviewActivity.newStart(this@MainActivity, items)
                    }
                }
            }
        })
    }

    fun upData(){
        adapter?.upRes(items!!,SPManager.init().getPreview())
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var instance: MainActivity

        fun newStart(context: Context) {
            val intent = Intent(context, MainActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun onClick(view: View?) {
        when (view) {
            actionDrawer -> {
                track(MyTrack.main_drawer_open_click)
                drawerLayout?.openDrawer(GravityCompat.START)
            }
            layoutSetting -> {
                track(MyTrack.click_setting)
                SettingActivity.newStart(this)
            }
            layoutVip -> {
                track(MyTrack.click_premium_upgrade)
                MyApp.billingManager?.showSub(this)
            }
            layoutLike -> {
                track(MyTrack.click_like_it)
                LikeItActivity.newStart(this)
            }
            layoutShare -> {
                track(MyTrack.click_share_app)
                ShareManager.toSystemShare(this@MainActivity, getString(R.string.share_app_content))
            }
            layoutMoreApp -> {
                isPackMoreApp()
            }
            vemoji -> {
                track(MyTrack.click_moreapp_vemoji)
                Util.startWebView(this, getString(R.string.diversion_vemoji_url))
            }
            lovemoji -> {
                track(MyTrack.click_moreapp_lovemoji)
                Util.startWebView(this, getString(R.string.diversion_lovemoji_url))
            }
            smilemoji -> {
                track(MyTrack.click_moreapp_smilemoji)
                Util.startWebView(this, getString(R.string.diversion_smilemoji_url))
            }
            privacy -> {
                track(MyTrack.click_privacy)
                Util.startWebView(this, getString(R.string.privacy_url))
            }
            actionDrawerBack -> {
                track(MyTrack.click_shut_drawer)
                drawerLayout?.closeDrawer(GravityCompat.START)
            }
        }
    }

    private fun isPackMoreApp() {
        if (moreLayout?.visibility == View.GONE) {
            track(MyTrack.drawer_more_app_shut)
            moreLayout?.visibility = View.VISIBLE
            more?.setImageResource(R.mipmap.drawer_app_pack)
            layoutMoreApp?.setBackgroundResource(R.drawable.button_drawer_item_round_top)
        } else if (moreLayout?.visibility == View.VISIBLE) {
            track(MyTrack.drawer_more_app_open)
            moreLayout?.visibility = View.GONE
            more?.setImageResource(R.mipmap.drawer_app_unfold)
            layoutMoreApp?.setBackgroundResource(R.drawable.button_drawer_item_round)
        }
    }

    override fun onBackPressed() {
        if (isDrawerStatus){
            drawerLayout!!.closeDrawer(GravityCompat.START)
            return
        }

        //返回到桌面但不退出应用进程(application中的数据仍然存在)
        val startMain = Intent(Intent.ACTION_MAIN)
        startMain.addCategory(Intent.CATEGORY_HOME)
        startMain.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(startMain)
    }

    override fun onDrawerStateChanged(newState: Int) {
        Log.d("drawer", "onDrawerStateChanged: $newState")
    }

    override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
    }

    override fun onDrawerClosed(drawerView: View) {
        isDrawerStatus = false
    }

    override fun onDrawerOpened(drawerView: View) {
        isDrawerStatus = true
        track(MyTrack.drawer_layout_open_num)
    }

    override fun onResume() {
        super.onResume()
        if (items?.isEmpty()!!) return
        //检索WhatApps对表情包的导入情况，即时更新列表
        whiteListCheckAsyncTask = WhiteListCheckAsyncTask(this)
        whiteListCheckAsyncTask?.execute(items)
    }

    override fun onPause() {
        super.onPause()
        if (whiteListCheckAsyncTask != null && !whiteListCheckAsyncTask?.isCancelled!!) {
            whiteListCheckAsyncTask?.cancel(true)
        }
    }

    internal class WhiteListCheckAsyncTask(stickerPackListActivity: MainActivity) :
        AsyncTask<MutableList<ItemBean>?, Void?, MutableList<AddPreview>?>() {
        private val stickerPackDetailsActivityWeakReference: WeakReference<MainActivity> =
            WeakReference(stickerPackListActivity)

        override fun doInBackground(vararg itemBean: MutableList<ItemBean>?): MutableList<AddPreview>? {
            var addPreview = SPManager.init().getPreview()
            var itemBeans = itemBean[0]
            if (itemBeans?.isEmpty()!!) return addPreview
            val mainActivity: MainActivity =
                stickerPackDetailsActivityWeakReference.get() ?: return addPreview
            for (_items in itemBeans){
                if (WhitelistCheck.isWhitelisted(mainActivity, _items.name!!)) {
                    //已添加到whatapps
                    if (CollectionUtils.isEmpty(addPreview)) {
                        addPreview = mutableListOf()
                        addPreview.add(AddPreview(_items.name!!))
                    } else {
                        if (!AddPreviewUtils.contains(addPreview!!, _items.name!!)) {
                            addPreview.add(AddPreview(_items.name!!))
                        }
                    }
                } else {
                    //未添加到whatapps
                    if (!CollectionUtils.isEmpty(addPreview)){
                        if (AddPreviewUtils.contains(addPreview!!, _items.name!!)) {
                            AddPreviewUtils.remove(addPreview, _items.name!!)
                        }
                    }
                }
            }
            return addPreview
        }

        override fun onPostExecute(result: MutableList<AddPreview>?) {
            result?.let { SPManager.init().addPreview(it) }
            instance.upData()
        }
    }

}