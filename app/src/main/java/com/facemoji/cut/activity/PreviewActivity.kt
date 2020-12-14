package com.facemoji.cut.activity

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.facemoji.cut.MyApp
import com.facemoji.cut.R
import com.facemoji.cut.adapter.OnClickItemGIF
import com.facemoji.cut.adapter.PreViewAdapter
import com.facemoji.cut.ads.AdIdsManager
import com.facemoji.cut.ads.BannerAdaptiveManager
import com.facemoji.cut.aide.Constants
import com.facemoji.cut.aide.Constants._preview
import com.facemoji.cut.aide.GrayStatus
import com.facemoji.cut.aide.MyTrack
import com.facemoji.cut.network.entity.AddPreview
import com.facemoji.cut.network.entity.GIF
import com.facemoji.cut.network.entity.ItemBean
import com.facemoji.cut.sticker.GlideDownloadPreview
import com.facemoji.cut.sticker.StickerContentProvider.Companion.STICKER_FILE_EMOJI_IN_QUERY
import com.facemoji.cut.sticker.StickerContentProvider.Companion.STICKER_FILE_NAME_IN_QUERY
import com.facemoji.cut.sticker.StickerPack
import com.facemoji.cut.sticker.StickerPackLoader
import com.facemoji.cut.sticker.WhitelistCheck
import com.facemoji.cut.utils.*
import com.facemoji.cut.view.GridWallpaperDecoration
import com.google.android.gms.common.util.CollectionUtils
import java.io.File
import java.lang.ref.WeakReference

class PreviewActivity : BaseAdsActivity(), View.OnClickListener, OnClickItemGIF {
    private var back: ImageView? = null
    private var title: TextView? = null
    private var recyclerView: RecyclerView? = null
    private var items: ItemBean? = null
    private var adapter: PreViewAdapter? = null
    private var adsLayout: FrameLayout? = null
    private var mStickerPack: StickerPack? = null
    private var addWhatApp: FrameLayout? = null
    private var _add: TextView? = null
    private var _exist: TextView? = null
    private var _isAdd :Boolean = false
    private var whiteListCheckAsyncTask: WhiteListCheckAsyncTask? = null


    override fun getLayoutId(): Int = R.layout.activity_preview

    override fun init() {
        instance = this
        initActionBarHeight()
        Util.changStatusIconColor(this, true)
        Util.setViewPadding(findViewById(R.id.activity_layout), this)
        recyclerView = findViewById(R.id.preview_recycler)
        adsLayout = findViewById(R.id.banner_ad_container)
        addWhatApp = findViewById(R.id.whatapps_add)
        addWhatApp?.setOnClickListener(this)
        _add = findViewById(R.id.add_)
        _exist = findViewById(R.id.exist_)
        back = findViewById(R.id.action_back)
        back?.setOnClickListener(this)
        title = findViewById(R.id.preview_item_title)
        items = intent.getParcelableExtra(preview)
        if (items == null) return
        title?.text = items?.name
        adapter = PreViewAdapter(this)
        recyclerView?.layoutManager = GridLayoutManager(this,5)
        recyclerView?.addItemDecoration(GridWallpaperDecoration(Util.dip2px(this,16)))
        recyclerView?.adapter = adapter
        recyclerView?.setHasFixedSize(true)
        items?.items?.let { adapter?.upRes(it) }
        adapter?.setClickItem(this)

        initData()
        registerBroadcastReceiver(registerBroadcastReceiver())
        initBanner(adsLayout!!, AdIdsManager.mojiBanner)
        initInterstitialAd(getInsert(), AdIdsManager.insert)
    }

    private fun initData() {
        mStickerPack = items?.name?.let { MyApp.instance.getSingleStickerPack(it) } //拿到本地表情包配置信息
        items?.let { GlideDownloadPreview.downloadStickers(this, it) }
    }

    private inner class registerBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            adsLayout?.visibility = View.GONE
        }
    }

    private fun initBanner(container : FrameLayout, ids : AdIdsManager.AdId) {
        if (!GrayStatus.ad_on || MyApp.billingManager?.whetherToBuy!!) return
        BannerAdaptiveManager.instance
            .setActivity(this)
            .setLayout(container)
            .setAdaptive(false)
            .setNested(false)
            .setType(_preview)
            .initAd(ids)
    }

    private fun shareUtil(path: String) = if (isLayerDownloaded(path)) {
        ShareManager.toSystemShare(this, File(path))
    } else {
        Toast.makeText(this, getString(R.string.share_content_is_empty), Toast.LENGTH_LONG).show()
    }

    private fun buildNewName(localPath: String, url: String): String? {
        val suffix: String? = url.lastIndexOf("/").let { url.substring(it) }
        return localPath.substring(0, localPath.lastIndexOf("/")) + suffix
    }

    private fun isLayerDownloaded(layerPath: String?): Boolean {
        if (TextUtils.isEmpty(layerPath)) false
        val file = File(layerPath)
        return file.exists() && file.isFile
    }

    companion object {
        private val preview = "preview"
        @SuppressLint("StaticFieldLeak")
        lateinit var instance: PreviewActivity

        fun newStart(context: Context, items: ItemBean) {
            val intent = Intent(context, PreviewActivity::class.java)
            intent.putExtra(preview, items)
            context.startActivity(intent)
        }
    }

    override fun onClick(view: View?) {
        when (view) {
            back -> {
                onBackPressed()
            }
            addWhatApp ->{
                val string = StringUtils.splitCapital(items?.name!!, " ", "_",
                    capital = false, down = false)
                track(MyTrack.preview_add_click_, string)
                if (_isAdd) return
                if (!showInterstitialAd()){
                    closeInters()
                }
            }
        }
    }

    override fun closeInters() {
        mStickerPack?.identifier?.let { mStickerPack?.name?.let { it1 ->
            addStickerPackToWhatsApp(it, it1) } }
    }

    override fun onResume() {
        super.onResume()
        whiteListCheckAsyncTask = WhiteListCheckAsyncTask(this)
        whiteListCheckAsyncTask?.execute(mStickerPack!!.identifier)
    }

    override fun onPause() {
        super.onPause()
        if (whiteListCheckAsyncTask != null && !whiteListCheckAsyncTask?.isCancelled!!) {
            whiteListCheckAsyncTask?.cancel(true)
        }
    }

    override fun item(position: Int, items: GIF) {
        track(MyTrack.preview_item_click_all)
        Glide.with(this@PreviewActivity)
            .downloadOnly()
            .load(items.thumbnail)
            .listener(object : RequestListener<File> {
                override fun onLoadFailed(e: GlideException?, model: Any,
                                          target: Target<File>, isFirstResource: Boolean): Boolean = false

                @SuppressLint("CheckResult")
                override fun onResourceReady(
                    resource: File, model: Any, target: Target<File>,
                    dataSource: DataSource, isFirstResource: Boolean): Boolean {
                    //Glide缓存文件默认不是正确的后缀名，此处需要主动更改为源文件的后缀名
                    val localPath =
                        items.thumbnail?.let { buildNewName(resource.absolutePath, it) }
                    resource.renameTo(File(localPath))
                    localPath?.let { shareUtil(it) }
                    return false
                }
            }).preload()
    }

    private fun updateAddUI(isWhitelisted: Boolean) {
        if (isWhitelisted) {
            //已添加
            _isAdd = true
            _add?.visibility = View.GONE
            _exist?.visibility = View.VISIBLE
        } else {
            //未添加
            _isAdd = false
            _add?.visibility = View.VISIBLE
            _exist?.visibility = View.GONE
        }
        //因为在MainActivity中进行了刷新，所以这里可以不用处理添加信息
        //storage(_isAdd)
    }

    fun storage(storage: Boolean) {
        var allAdd: MutableList<AddPreview>? = SPManager.init().getPreview()
        if (storage) {//添加
            if (CollectionUtils.isEmpty(allAdd)) {
                allAdd = mutableListOf()
                allAdd.add(AddPreview(items!!.name!!))
            } else {
                if (!AddPreviewUtils.contains(allAdd!!, items?.name!!)) {
                    allAdd.add(AddPreview(items!!.name!!))
                }
            }
        } else {//删除
            if (!CollectionUtils.isEmpty(allAdd)){
                if (AddPreviewUtils.contains(allAdd!!, items?.name!!)) {
                    AddPreviewUtils.remove(allAdd, items?.name!!)
                } else {
                    return
                }
            } else{
                return
            }
        }
        SPManager.init().addPreview(allAdd)
        MainActivity.instance.upData()
    }

    internal class WhiteListCheckAsyncTask(stickerPackListActivity: PreviewActivity) :
        AsyncTask<String?, Void?, Boolean>() {
        private val stickerPackDetailsActivityWeakReference: WeakReference<PreviewActivity> =
            WeakReference(stickerPackListActivity)

        override fun doInBackground(vararg stickerPacks: String?): Boolean? {
            val stickerPackIdentifier = stickerPacks[0]
            val previewActivity: PreviewActivity =
                stickerPackDetailsActivityWeakReference.get() ?: return false
            return stickerPackIdentifier?.let { WhitelistCheck.isWhitelisted(previewActivity, it) }
        }

        override fun onPostExecute(isWhitelisted: Boolean) {
            val previewActivity: PreviewActivity? = stickerPackDetailsActivityWeakReference.get()
            previewActivity?.updateAddUI(isWhitelisted)
        }
    }

}