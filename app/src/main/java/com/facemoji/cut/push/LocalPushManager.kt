package com.facemoji.cut.push

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.google.android.gms.common.util.CollectionUtils
import com.facemoji.cut.aide.Constants
import com.facemoji.cut.aide.GrayStatus
import com.facemoji.cut.network.CommonCallback
import com.facemoji.cut.network.Request
import com.facemoji.cut.network.RetrofitNetwork
import com.facemoji.cut.network.entity.push.LocalPushConfig
import com.facemoji.cut.network.entity.push.LocalPushConfig.DataBean
import com.facemoji.cut.network.entity.push.LocalPushMessage
import com.facemoji.cut.utils.AssetsUtil
import com.facemoji.cut.utils.SPManager
import com.facemoji.cut.utils.Util
import java.util.*

/**
 * @Author: ydli
 * @Description: 推送消息管理
 * @CreateDate:
 */
class LocalPushManager private constructor() {

    fun requestConfig(context: Context) {
        initLocalPushConfig(context)
        val queryParams: MutableMap<String, String> = HashMap()
        queryParams["packageName"] = context.packageName
        queryParams["versionCode"] = Util.getVersionCode(context)
        queryParams["message_type"] = PushConstants.Smilemoji
        RetrofitNetwork.INSTANCE.getRequest<Request>().getLocalPushConfigRequest(queryParams)
            ?.enqueue(object : CommonCallback<LocalPushConfig?>() {
                override fun onResponse(response: LocalPushConfig?) {
                    if (response?.ret === 0) {
                        response.data?.let { saveLocalPushConfig(it) }
                    }
                    startPush(context)
                }

                override fun onFailure(t: Throwable?, isServerUnavailable: Boolean) {
                    Log.d("onFailure", "onFailure: ")
                    startPush(context)
                }
            })
    }

    private fun initLocalPushConfig(context: Context) {
        val intervals: List<Int>? = SPManager.init().getLocalPushIntervalList()
        if (CollectionUtils.isEmpty(intervals)) {
            val config: DataBean = AssetsUtil.getLocalPushList(context)
            saveLocalPushConfig(config)
        }
    }

    private fun saveLocalPushConfig(config: DataBean) {
        val intervals: List<Int>? = config.interval
        if (!CollectionUtils.isEmpty(intervals)) {
            SPManager.init().putLocalPushIntervalList(intervals)
        }
        val messages: List<List<LocalPushMessage>>? = config.messages
        if (!CollectionUtils.isEmpty(messages)) {
            val messageList: ArrayList<LocalPushMessage> = arrayListOf()
            for (i in messages?.indices!!) {
                messageList.addAll(messages[i])
            }
            SPManager.init().putLocalPushMessageList(messageList)
        }
    }

    companion object {
        private const val INTERVAL_MINUTES = 60 * 1000L
        //private const val INTERVAL_MINUTES = 1 * 1000L
        private const val REPEAT_TASK_REQUEST_CODE = 1010
        val instance = LocalPushManager()

        //发送一条通知
        fun startPush(context: Context) {
            if (!SPManager.init().getBoolean(Constants.KEY_SETTING_PUSH_MESSAGE, GrayStatus.push)) return
            val intervals: List<Int>? =
                SPManager.init().getLocalPushIntervalList()
            if (CollectionUtils.isEmpty(intervals)) return

            // 第一次启动时注册多个一次性推送任务
            val currentTime =
                System.currentTimeMillis() / INTERVAL_MINUTES
            val firstLaunchTime: Long =
                SPManager.init().getLong(PushConstants.FIRST_LAUNCH_TIME, -1)
            if (firstLaunchTime <= 0) {
                SPManager.init().setLong(PushConstants.FIRST_LAUNCH_TIME, currentTime)
                for (i in 0 until (intervals?.size?.minus(1)!!)) {
                    startSinglePush(context, intervals[i].toLong(), i)
                }
            }

            //if (hasNotStartRepeatPush(context)) {
            // 还没有注册过定时推送任务, 立即注册一个
            var intervalTime = intervals?.get(intervals.size - 1)?.toLong()
            if (firstLaunchTime > 0) {
                intervalTime = intervalTime?.minus((currentTime - firstLaunchTime) % intervalTime)
            }
            val index: Int = SPManager.init()
                .getInt(PushConstants.REPEAT_PUSH_MSG_INDEX, intervals?.size?.minus(1)!!)
            intervalTime?.let { startRepeatPush(context, it, index) }
            //}
        }

        fun startSinglePush(context: Context, delayTime: Long, index: Int) {
            startPush(context, false, delayTime, index, index)
        }

        @JvmStatic
        fun startRepeatPush(context: Context, intervalTime: Long, index: Int) {
            startPush(context, true, intervalTime, index, REPEAT_TASK_REQUEST_CODE)
            SPManager.init().setInt(PushConstants.REPEAT_PUSH_MSG_INDEX, index)
        }

        /**
         * @param context      上下文
         * @param delayTime    延时时间, 单位：分钟
         * @param index        推送消息的位置
         * @param requestCode  请求码，如果相同会覆盖上一个推送请求
         * @param isRepeatPush 是否是定时推送
         */
        private fun startPush(
            context: Context, isRepeatPush: Boolean, delayTime: Long, index: Int, requestCode: Int
        ) {
            var delayTime = delayTime
            val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            delayTime *= INTERVAL_MINUTES
            val pendingIntent = buildPendingIntent(
                context,
                isRepeatPush,
                index,
                requestCode
            )

            //版本适配
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) { // 19及以上
                am.setExact(
                    AlarmManager.RTC_WAKEUP,
                    System.currentTimeMillis() + delayTime, pendingIntent
                )
            } else {
                am[AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + delayTime] = pendingIntent
            }
        }

        private fun buildPendingIntent(
            context: Context, isRepeatPush: Boolean,
            index: Int, requestCode: Int
        ): PendingIntent {
            val intent = Intent(context.applicationContext, LocalPushReceiver::class.java)
             intent.action = PushConstants.LOCAL_PUSH_BROADCAST_ACTION
            intent.putExtra(PushConstants.LOCAL_PUSH_MSG_INDEX, index)
            intent.putExtra(PushConstants.IS_REPEAT_PUSH, isRepeatPush)
            return PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        }

        /**
         * 还没有启动定时推送任务
         *
         * @param context
         * @return
         */
        private fun hasNotStartRepeatPush(context: Context): Boolean {
            val intent =
                Intent(context.applicationContext, LocalPushReceiver::class.java)
            intent.action = PushConstants.LOCAL_PUSH_BROADCAST_ACTION
            val pendingIntent = PendingIntent.getBroadcast(
                context, REPEAT_TASK_REQUEST_CODE,
                intent, PendingIntent.FLAG_NO_CREATE
            )
            return pendingIntent == null
        }
    }
}