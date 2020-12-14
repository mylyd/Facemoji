package com.facemoji.cut.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Point
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Base64
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import com.google.gson.JsonElement
import com.facemoji.cut.network.CommonCallback
import com.facemoji.cut.network.Request
import com.facemoji.cut.network.RetrofitNetwork
import com.facemoji.cut.network.entity.AddPreview
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*

object Util {
    /**
     * 执行某方法，如果此类中没找到则从父类中找
     *
     * @param owner
     * @param methodName
     * @param types
     * @param values
     * @return
     */
    operator fun invoke(
        owner: Any?, methodName: String, types: Array<Class<*>?>,
        values: Array<Any?>
    ): Any? {
        if (owner == null) return null
        var c: Class<*>? = owner.javaClass
        while (c != null) {
            try {
                val m = c.getDeclaredMethod(methodName, *types)
                m.isAccessible = true
                return m.invoke(owner, *values)
            } catch (e: NoSuchMethodException) {
                c = c.superclass
            } catch (e: Exception) {
                Log.e("app2", "invoke-->methodName=$methodName", e)
                break
            }
        }
        return null
    }

    fun getPackageName(context: Context?): String? {
        return if (context != null) {
            context.packageName
        } else ""
    }

    fun getVersionCode(context: Context): String {
        var versionCode = -1
        try {
            versionCode = context.packageManager
                .getPackageInfo(context.packageName, 0).versionCode
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return versionCode.toString()
    }

    //
    fun getVersionName(context: Context): String {
        var versionName = ""
        try {
            versionName = context.packageManager
                .getPackageInfo(context.packageName, 0).versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return versionName
    }

    @SuppressLint("HardwareIds")
    fun getAndroidId(context: Context): String {
        var androidId: String? = "null"
        try {
            androidId = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ANDROID_ID
            )
        } catch (e: Exception) {
        }
        if (androidId == null || "" == androidId) {
            androidId = "null"
        }
        return androidId
    }

    /**
     * 对字符串进行加密
     * @param plainText 待加密字符串
     * @return 加密后字符串
     */
    fun getStringMD5(plainText: String): String {
        var md: MessageDigest? = null
        try {
            md = MessageDigest.getInstance("MD5")
            md.update(plainText.toByteArray())
        } catch (e: Exception) {
            return "null"
        }
        return encodeHex(md.digest())
    }

    /**
     * 二进制加密为16进制转换
     * @param data 待转换二进制
     * @return 加密后字符串
     */
    fun encodeHex(data: ByteArray?): String {
        if (data == null) {
            return "null"
        }
        val HEXES = "0123456789abcdef"
        val len = data.size
        val hex = StringBuilder(len * 2)
        for (i in 0 until len) {
            hex.append(HEXES[data[i].toInt() and 0xF0 ushr 4])
            hex.append(HEXES[data[i].toInt() and 0x0F])
        }
        return hex.toString()
    }

    fun getChannelId(context: Context): String? {
        val appInfo: ApplicationInfo
        try {
            appInfo = context.packageManager
                .getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
            if (appInfo.metaData != null) {
                return appInfo.metaData["CYOU_CHANNEL"].toString()
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return ""
    }

    fun startWebView(context: Context, url: String?) {
        try {
            val intent = Intent()
            intent.action = "android.intent.action.VIEW"
            val content_url = Uri.parse(url)
            intent.data = content_url
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
        }
    }

    /**
     * 设置状态栏图标颜色
     * @param setDark 状态栏颜色 true:黑色 false：白色
     * @return
     */
    fun changStatusIconColor(activity: Activity, setDark: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val decorView = activity.window.decorView
            var vis = decorView.systemUiVisibility
            vis = if (setDark) {
                vis or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            } else {
                vis and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
            }
            decorView.systemUiVisibility = vis
        }
    }

    /**
     * 判断当前虚拟导航栏是否显示
     * @param activity
     * @return
     */
    @SuppressLint("NewApi")
    fun isNavigationVisible(activity: Activity): Boolean {
        if ("xiaomi".equals(Build.MANUFACTURER, ignoreCase = true)) {
            // 开启了全面屏，肯定没有导航栏;如果还有厂商估计不就就要倒闭了
            if (Settings.Global.getInt(activity.contentResolver, "force_fsg_nav_bar", 0) != 0) {
                return false
            }
        }
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            val windowManager =
                activity.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val display = windowManager.defaultDisplay
            val size = Point()
            val realSize = Point()
            display.getSize(size)
            display.getRealSize(realSize)
            realSize.y != size.y
        } else {
            val menu = ViewConfiguration.get(activity).hasPermanentMenuKey()
            val back = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK)
            !menu && !back
        }
    }

    fun getCurrentLanguage(): String? {
        val locale = Locale.getDefault()
        return locale.language
    }

    fun getCurrentCountry(): String? {
        val locale = Locale.getDefault()
        return locale.country
    }

    /**
     * 获得屏幕高度
     *
     * @param context
     * @return
     */
    fun getWindowHeight(context: Context): Int {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val outMetrics = DisplayMetrics()
        wm.defaultDisplay.getMetrics(outMetrics)
        return outMetrics.heightPixels
    }


    /**
     * 提交反馈信息
     *
     * @param email
     * @param suggestion
     */
    fun requestFeedBack(
        context: Context, email: String, suggestion: String,
        commonCallback: CommonCallback<JsonElement?>?
    ) {
        val queryParams: MutableMap<String, String?> = hashMapOf()
        queryParams["pkgname"] = context.packageName
        queryParams["version"] = getVersionName(context)
        queryParams["vercode"] = getVersionCode(context)
        queryParams["did"] = getAndroidId(context)
        queryParams["deviceModel"] = Build.MODEL
        queryParams["os"] = Build.VERSION.RELEASE
        queryParams["language"] = getCurrentLanguage()
        queryParams["country"] = getCurrentCountry()
        queryParams["channelId"] = getChannelId(context)
        queryParams["resolution"] = getWindowHeight(context).toString()
        queryParams["cpu"] = Build.BOARD
        queryParams["email"] = email
        queryParams["message"] = suggestion
        RetrofitNetwork.INSTANCE.getRequest<Request>().postFeedBack(queryParams)
            ?.enqueue(commonCallback)
    }

    /**
     * 获取签名文件的哈希散列值
     *
     * @param context
     * @return
     */
    fun getSignatureHasCode(context: Context): String? {
        try {
            val info = context.packageManager
                .getPackageInfo(context.packageName, PackageManager.GET_SIGNATURES)
            for (signature in info.signatures) {
                val md = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                //KeyHash 就是你要的，不用改任何代码  复制粘贴 ;
                return Base64.encodeToString(md.digest(), Base64.DEFAULT)
            }
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }
        return null
    }

    /**
     * 获取底部虚拟栏高度
     */
    public fun getNavigationBarHeight(context: Context): Int {
        val resources = context.resources
        val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
        return resources.getDimensionPixelSize(resourceId)
    }

    fun setViewPadding(view: View, activity: Activity) {
        //处理底部不全面屏问题 ,判断底部是否含有虚拟导航栏
        if (isNavigationVisible(activity)) {
            view.setPadding(
                view.paddingLeft,
                view.paddingTop,
                view.paddingRight,
                getNavigationBarHeight(activity) + view.paddingBottom
            )
        }
    }

    /**
     * 获取状态栏高度
     * 单位 px
     */
    fun getStatusBarHeight(activity: Activity): Int {
        val resources: Resources = activity.resources
        val resourceId: Int = resources.getIdentifier("status_bar_height", "dimen", "android")
        //return px2dip(resources,resources.getDimensionPixelSize(resourceId))
        return resources.getDimensionPixelSize(resourceId)
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     */
    fun px2dip(context: Context, pxValue: Int): Int {
        val scale: Float = context.resources.displayMetrics.density
        return (pxValue / scale + 0.5f).toInt()
    }

    fun dip2px(context: Context, dipValue: Int): Int {
        val scale: Float = context.resources.displayMetrics.density
        return ((dipValue - 0.5f) * scale).toInt()
    }

}