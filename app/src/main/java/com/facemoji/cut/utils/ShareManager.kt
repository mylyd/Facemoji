package com.facemoji.cut.utils

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Parcelable
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import com.facemoji.cut.R
import java.io.File

/**
 * @author : ydli
 * @time : 2020/11/11 8:56
 * @description :
 */
object ShareManager {
    const val whatApp = 0
    const val facebook = 1
    const val messenger = 2

    /**
     * 分享应用App && 文字
     *
     * @param context
     * @param string
     */
    fun toSystemShare(context: Context, string: String?) {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TEXT, string)
        context.startActivity(Intent.createChooser(intent, "Share it ${context.getString(R.string.app_name)}"))
    }

    /**
     * 分享图片
     *
     * @param context
     * @param file
     */
    fun toSystemShare(context: Context, file: File?) {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "image/*"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val contentUri =
                FileProvider.getUriForFile(context, context.packageName + ".fileprovider", file!!)
            intent.putExtra(Intent.EXTRA_STREAM, contentUri)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        } else {
            intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file))
        }
        context.startActivity(Intent.createChooser(intent, "Share it ${context.getString(R.string.app_name)}"))
    }

    /**
     * 0 -> whatApp
     * 1 -> facebook
     * 2 -> messenger
     */
    fun startShareImage(activity: Activity, file: File?, int: Int) {
        var type: String? = ""
        //过滤出需要分享到对应的平台：微信好友、朋友圈、QQ好友。  可自行修改
        val targetApp: MutableList<String> = mutableListOf()

        when (int) {
            0 -> {//whatApp
                type = "whatApp"
                targetApp.add("com.whatsapp.ContactPicker")
            }
            1 -> {//facebook
                type = "facebook"
                targetApp.add("com.facebook.timeline.stagingground.Fb4aProfilePictureShareActivityAlias")
                targetApp.add("com.facebook.composer.shareintent.ImplicitShareIntentHandlerDefaultAlias")
                targetApp.add("com.facebook.inspiration.shortcut.shareintent.InpirationCameraShareDefaultAlias")
            }
            2 -> {//messenger
                type = "messenger"
                targetApp.add("com.facebook.messenger.intents.ShareIntentHandler")
            }
        }

        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "image/*" //设置分享内容的类型：图片
        putExtra(activity, shareIntent, file)
        try {
            val resInfo = activity.packageManager.queryIntentActivities(shareIntent, 0)
            if (resInfo.isNotEmpty()) {
                val targetedShareIntents = ArrayList<Intent>()
                for (info in resInfo) {
                    val targeted = Intent(Intent.ACTION_SEND)
                    targeted.type = "image/*"  //设置分享内容的类型
                    val activityInfo = info.activityInfo
                    //如果还需要分享至其它平台，可以打印出具体信息，然后找到对应的Activity名称，填入上面的数组中即可
                    /* Log.d(
                         "share",
                         "package = ${activityInfo.packageName}, activity = ${activityInfo.name}"
                     )*/

                    //进行过滤（只显示需要分享的平台）
                    if (targetApp.any { it == activityInfo.name }) {
                        val comp = ComponentName(activityInfo.packageName, activityInfo.name)
                        targeted.component = comp
                        putExtra(activity, targeted, file)
                        targetedShareIntents.add(targeted)
                    }
                }
                if (targetedShareIntents.isEmpty()) {
                    Toast.makeText(
                        activity,
                        "No query to install the $type application",
                        Toast.LENGTH_SHORT
                    ).show()
                    return
                }
                val chooserIntent =
                    Intent.createChooser(
                        targetedShareIntents.removeAt(0),
                        "Choose the platform to share to"
                    )
                if (chooserIntent != null) {
                    chooserIntent.putExtra(
                        Intent.EXTRA_INITIAL_INTENTS,
                        targetedShareIntents.toTypedArray<Parcelable>()
                    )
                    activity.startActivity(chooserIntent)
                }
            }
        } catch (e: Exception) {
            Log.e("share", "Unable to share image,  logs : $e")
        }
    }

    private fun putExtra(activity: Activity, intent: Intent, file: File?): Intent? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val contentUri =
                FileProvider.getUriForFile(activity, activity.packageName + ".fileprovider", file!!)
            intent.putExtra(Intent.EXTRA_STREAM, contentUri)
            intent.putExtra(Intent.EXTRA_TEXT, activity.getString(R.string.app_gp))
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        } else {
            intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file))
            intent.putExtra(Intent.EXTRA_TEXT, activity.getString(R.string.app_gp))
        }
        return intent
    }

}