package com.facemoji.cut.push

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.facemoji.cut.activity.StartActivity
import com.google.android.gms.common.util.CollectionUtils
import com.facemoji.cut.aide.Constants
import com.facemoji.cut.aide.GrayStatus
import com.facemoji.cut.network.entity.push.LocalPushMessage
import com.facemoji.cut.push.LocalPushManager.Companion.startRepeatPush
import com.facemoji.cut.utils.SPManager

/**
 * @Author: ydli
 * @Description: 推送消息广播接收器
 * @CreateDate:
 */
class LocalPushReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (PushConstants.LOCAL_PUSH_BROADCAST_ACTION == intent.action) {
            onPushReceive(context, intent)
        } else if (PushConstants.NOTIFICATION_BROADCAST_ACTION == intent.action) {
            onNotificationReceive(context, intent)
        }
    }

    /**
     * 执行推送任务发送的广播
     *
     * @param context
     * @param intent
     */
    private fun onPushReceive(context: Context, intent: Intent) {
        if (!SPManager.init().getBoolean(Constants.KEY_SETTING_PUSH_MESSAGE, GrayStatus.push)) return
        val messages: List<LocalPushMessage?> =
            SPManager.init().getLocalPushMessageList()
        if (CollectionUtils.isEmpty(messages)) return
        val isRepeatPush =
            intent.getBooleanExtra(PushConstants.IS_REPEAT_PUSH, false)
        var index = intent.getIntExtra(PushConstants.LOCAL_PUSH_MSG_INDEX, 0)
        if (index >= messages.size) {
            index %= messages.size
        }
        PushNotificationManager.showNotification(context, isRepeatPush, index, messages[index])
        /* if (index == 0 && !isRepeatPush) {
             FirebaseTracker.instance.track(MyTrack.PUSH_FRIST_SHOW)
         } else if (index == 1 && !isRepeatPush) {
             FirebaseTracker.instance.track(MyTrack.PUSH_SECOND_SHOW)
         } else {
             FirebaseTracker.instance.track(MyTrack.PUSH_THIRD_SHOW)
         }*/
        if (isRepeatPush) {
            val intervals: List<Int>? = SPManager.init().getLocalPushIntervalList()
            if (CollectionUtils.isEmpty(intervals)) return
            intervals?.get(intervals.size - 1)?.toLong()?.let {
                startRepeatPush(context, it, ++index)
            }
        }
    }

    /**
     * 点击通知消息发送的广播
     *
     * @param context
     * @param intent
     */
    private fun onNotificationReceive(context: Context, intent: Intent) {
        val link = intent.getStringExtra(PushConstants.LOCAL_PUSH_LINK)
        val index = intent.getIntExtra(PushConstants.LOCAL_PUSH_MSG_INDEX, 0)
        val isRepeatPush = intent.getBooleanExtra(PushConstants.IS_REPEAT_PUSH, false)
        try {
            val autoStart = Intent(context, StartActivity::class.java)
            autoStart.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            autoStart.putExtra("NOTIFICATION_KEY", "notification")
            context.startActivity(autoStart)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        /*if (index == 0 && !isRepeatPush) {
            FirebaseTracker.getInstance().track(MyTracker.PUSH_FRIST_SHOW)
        } else if (index == 1 && !isRepeatPush) {
            FirebaseTracker.getInstance().track(MyTracker.PUSH_SECOND_SHOW)
        } else {
            FirebaseTracker.getInstance().track(MyTracker.PUSH_THIRD_SHOW)
        }*/
    }
}