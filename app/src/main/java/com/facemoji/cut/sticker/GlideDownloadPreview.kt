package com.facemoji.cut.sticker

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.net.Uri
import android.util.Log
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.facemoji.cut.network.entity.ItemBean
import com.facemoji.cut.utils.FileUtil
import com.facemoji.cut.utils.Util
import java.io.File

/**
 * @author : ydli
 * @time : 2020/11/24 10:29
 * @description :
 */
object GlideDownloadPreview {
    var activity: Activity? = null
    var mGlide: RequestManager? = null

    fun downloadStickers(activity: Activity, itemBean: ItemBean) {
        this.activity = activity
        this.mGlide = Glide.with(activity)
        if (itemBean.items?.isEmpty()!!) return
        val path = activity.getExternalFilesDir(null)?.absolutePath
        for (thumb in itemBean.items!!) {
            path?.let { thumb.thumbnail?.let { it1 -> savePicture(it, it1, itemBean.name!!) } }
        }
    }

    @SuppressLint("CheckResult")
    private fun savePicture(path: String, url: String, packName: String) {
        val startIdx = url.lastIndexOf("/")
        val fileName = url.substring(startIdx + 1)
        val dirName: String = packName
        val filePath = File("$path/$dirName")
        if (!filePath.exists()) {
            filePath.mkdir()
        }
        val tarFile = File("$path/$dirName/$fileName")
        if (tarFile.exists() && tarFile.isFile) {
            updateContentProvider(dirName, fileName)
            return
        }
        mGlide?.downloadOnly()?.load(url)?.listener(object : RequestListener<File>{
            override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<File>?,
                isFirstResource: Boolean): Boolean {
                Log.d("", "onLoadFailed: ")
                return false
            }

            override fun onResourceReady(resource: File?, model: Any?, target: Target<File>?,
                dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                val dirName: String = packName
                val filePath = File("$path/$dirName")
                if (!filePath.exists()) {
                    filePath.mkdir()
                }

                val tarFile = File("$path/$dirName/$fileName")
                if (tarFile.exists() && tarFile.isFile) {
                    updateContentProvider(dirName, fileName)
                    return true
                }
                FileUtil.copy(resource, tarFile ,fileName)
                updateContentProvider(dirName, fileName)
                return true
            }
        })?.preload()
    }

    private fun updateContentProvider(dirname: String, fileName: String) {
        val resolver = activity?.contentResolver
        val uri: Uri = StickerPackLoader.getStickerListUri(dirname)
        val values = ContentValues()
        values.put("identifier", dirname)
        values.put(StickerContentProvider.STICKER_FILE_NAME_IN_QUERY, fileName)
        values.put(StickerContentProvider.STICKER_FILE_EMOJI_IN_QUERY, ",")
        resolver?.insert(uri, values)
    }

}