package com.facemoji.cut.fragment

import android.content.BroadcastReceiver
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.facemoji.cut.aide.Constants
import com.facemoji.cut.aide.tracker.FirebaseTracker

/**
 * @author : ydli
 * @time : 2020/11/11 10:49
 * @description :
 */
abstract class BaseFragment : Fragment() {
    private var mReceiver: BroadcastReceiver? = null

    protected fun <T : View?> findViewById(@IdRes id: Int): T? {
        return try {
            view?.findViewById<T>(id)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(getLayoutId(), container, false)
    }

    protected abstract fun getLayoutId(): Int

    fun track(track: String, value: Any? = null) {
        if (value == null)
            FirebaseTracker.instance.track(track)
        else
            FirebaseTracker.instance.track(track + value.toString())
    }

    /**
     * 注册订阅成功广播接收器
     * 所有加载广告的页面都需要注册[订阅成功广播接收器],订阅成功时隐藏原生广告和横幅广告，显示插页广告和视频广告时需要判断是否订阅
     *
     * @param receiver
     */
    protected open fun registerBroadcastReceiver(receiver: BroadcastReceiver) {
        mReceiver = receiver
        val intentFilter = IntentFilter()
        intentFilter.addAction(Constants.NO_ADS)
        LocalBroadcastManager.getInstance(context!!).registerReceiver(receiver, intentFilter)
    }

    override fun onDestroy() {
        if (mReceiver != null){
            LocalBroadcastManager.getInstance(context!!).unregisterReceiver(mReceiver!!)
        }
        super.onDestroy()
    }
}