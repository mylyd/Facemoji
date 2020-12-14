package com.facemoji.cut.push

import android.annotation.TargetApi
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.os.Build
import android.text.TextUtils
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.facemoji.cut.R
import com.facemoji.cut.network.entity.push.LocalPushMessage

/**
 * @Author: jzhou
 * @Description: 推送通知数据处理
 * @CreateDate: 20-6-17 下午5:30
 */
object PushNotificationManager {
    private val TAG = PushNotificationManager::class.java.simpleName

    /**
     * 通知小图标
     *
     * android5.0 之后通知栏图标都修改了，小图标不能含有RGB图层，
     * 也就是说图片不能带颜色，只能用白色的图片,还只能是.png格式，否则显示的就成白色（灰色）方格了
     */
    private const val mNotificationIcon = R.mipmap.ic_notification

    fun showNotification(context: Context, isRepeatPush: Boolean, index: Int, message: LocalPushMessage?) {
        if (message == null) return
        if (!TextUtils.isEmpty(message.icon_url)) {
            loadIconBitmap(context, isRepeatPush, index, message)
        } else if (!TextUtils.isEmpty(message.preview_url)) {
            loadBannerBitmap(context, isRepeatPush, index, message)
        } else {
            //showCustomNotification(context, isRepeatPush, index, message)
            showNotification(context, isRepeatPush, index, message,null)
        }
    }

    private fun loadIconBitmap(context: Context, isRepeatPush: Boolean, index: Int,
        message: LocalPushMessage
    ) {
        showSystemNotification(context, isRepeatPush, index, message)
        /*Glide.with(context).asBitmap().load(message.icon_url)
            .into(object : CustomTarget<Bitmap?>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap?>?) {
                    Log.d(TAG, "icon download finish")
                    message.iconBitmap = resource
                    if (TextUtils.isEmpty(message.preview_url)) {
                        showSystemNotification(context, isRepeatPush, index, message)
                    } else {
                        loadBannerBitmap(context, isRepeatPush, index, message)
                    }
                }

                override fun onLoadCleared(placeholder: Drawable?) {}
                override fun onLoadFailed(errorDrawable: Drawable?) {
                    Log.d(TAG, "icon download failed")
                    if (TextUtils.isEmpty(message.preview_url)) {
                        showSystemNotification(context, isRepeatPush, index, message)
                    } else {
                        loadBannerBitmap(context, isRepeatPush, index, message)
                    }
                }
            })*/
    }

    private fun loadBannerBitmap(
        context: Context, isRepeatPush: Boolean, index: Int,
        message: LocalPushMessage
    ) {
        Glide.with(context).asBitmap().load(message.preview_url)
            .into(object : CustomTarget<Bitmap?>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap?>?) {
                    Log.d(TAG, "banner download finish")
                    message.bannerBitmap = resource
                    showSystemNotification(context, isRepeatPush, index, message)
                }

                override fun onLoadCleared(placeholder: Drawable?) {}
                override fun onLoadFailed(errorDrawable: Drawable?) {
                    Log.d(TAG, "banner download failed")
                    showSystemNotification(context, isRepeatPush, index, message)
                }
            })
    }

    private fun showSystemNotification(
        context: Context, isRepeatPush: Boolean, index: Int,
        message: LocalPushMessage
    ) {
        //适配不通过url设置大图标
        message.iconBitmap = when (message.id) {
            1101 -> {
                BitmapFactory.decodeResource(context.resources, R.mipmap.ic_icon_1)
            }
            1102 -> {
                BitmapFactory.decodeResource(context.resources, R.mipmap.ic_icon_2)
            }
            1103 -> {
                BitmapFactory.decodeResource(context.resources, R.mipmap.ic_icon_3)
            }
            else -> {
                message.iconBitmap ?: BitmapFactory.decodeResource(context.resources, mNotificationIcon)
            }
        }
        val appName = context.getString(R.string.app_name)
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val builder = NotificationCompat.Builder(context, appName)
        builder.setContentTitle(message.title) //设置通知栏标题
            .setContentText(message.desc) //设置通知栏显示内容
            .setContentIntent(buildPendingIntent(context, isRepeatPush, index, message.link)) //设置通知栏点击意图
            //.setNumber(number) //设置通知集合的数量
            .setTicker(message.title) //通知首次出现在通知栏，带上升动画效果的
            .setWhen(System.currentTimeMillis()) //通知产生的时间，会在通知信息里显示，一般是系统获取到的时间
            .setPriority(Notification.PRIORITY_DEFAULT) //设置该通知优先级
            .setAutoCancel(true) //设置这个标志当用户单击面板就可以让通知将自动取消
            .setOngoing(false) //ture，设置他为一个正在进行的通知。他们通常是用来表示一个后台任务,用户积极参与(如播放音乐)或以某种方式正在等待,因此占用设备(如一个文件下载,同步操作,主动网络连接)
            .setDefaults(Notification.DEFAULT_ALL) //向通知添加声音、闪灯和振动效果的最简单、最一致的方式是使用当前的用户默认设置，使用defaults属性，可以组合
            .setSmallIcon(mNotificationIcon) //设置通知小ICON
            .setLargeIcon(message.iconBitmap)
        if (message.bannerBitmap != null) {
            builder.setStyle(NotificationCompat.BigPictureStyle().bigPicture(message.bannerBitmap))
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { // 8.0及以上
            createNotificationChannel(manager, appName, appName)
        }
        manager.notify(1, builder.build())
    }

    private fun showCustomNotification(
        context: Context, isRepeatPush: Boolean, index: Int,
        message: LocalPushMessage
    ) {
        if (message.iconBitmap == null) {
            val iconBitmap = BitmapFactory.decodeResource(context.resources, mNotificationIcon)
            message.iconBitmap = iconBitmap
        }
        if (message.bannerBitmap == null) {
            val iconBitmap = BitmapFactory.decodeResource(context.resources, mNotificationIcon)
            message.bannerBitmap = iconBitmap
        }
        val remoteView =
            RemoteViews(context.packageName, R.layout.layout_push_notification)
        remoteView.setTextViewText(R.id.tv_title, message.title)
        remoteView.setTextViewText(R.id.tv_content, message.desc)
        remoteView.setImageViewBitmap(R.id.iv_icon, message.iconBitmap)
        remoteView.setImageViewBitmap(R.id.iv_banner, message.bannerBitmap)
        showNotification(context, isRepeatPush, index, message, remoteView)
    }

    private fun showNotification(
        context: Context, isRepeatPush: Boolean, index: Int,
        message: LocalPushMessage, remoteView: RemoteViews?) {
        val appName = context.getString(R.string.app_name)
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val builder = NotificationCompat.Builder(context, appName)
        builder.setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setCustomContentView(remoteView)
        builder.setContentIntent(
            buildPendingIntent(context, isRepeatPush, index, message.link)
        ) //设置通知栏点击意图
            //.setNumber(number) //设置通知集合的数量
            .setTicker(message.title) //通知首次出现在通知栏，带上升动画效果的
            .setWhen(System.currentTimeMillis()) //通知产生的时间，会在通知信息里显示，一般是系统获取到的时间
            .setPriority(Notification.PRIORITY_DEFAULT) //设置该通知优先级
            .setAutoCancel(true) //设置这个标志当用户单击面板就可以让通知将自动取消
            .setOngoing(false) //ture，设置他为一个正在进行的通知。他们通常是用来表示一个后台任务,用户积极参与(如播放音乐)或以某种方式正在等待,因此占用设备(如一个文件下载,同步操作,主动网络连接)
            .setDefaults(Notification.DEFAULT_ALL) //向通知添加声音、闪灯和振动效果的最简单、最一致的方式是使用当前的用户默认设置，使用defaults属性，可以组合
            .setSmallIcon(mNotificationIcon) //设置通知小ICON
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setCustomContentView(remoteView)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { // 8.0及以上
            createNotificationChannel(manager, appName, appName)
        }
        manager.notify(1, builder.build())
    }

    @TargetApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(
        notificationManager: NotificationManager, channelId: String, channelName: String) {
        val channel =
            NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT)
        notificationManager.createNotificationChannel(channel)
    }

    private fun buildPendingIntent(
        context: Context, isRepeatPush: Boolean, index: Int, link: String?): PendingIntent {
        val intent = Intent(context.applicationContext, LocalPushReceiver::class.java)
        intent.action = PushConstants.NOTIFICATION_BROADCAST_ACTION
        intent.putExtra(PushConstants.LOCAL_PUSH_MSG_INDEX, index)
        intent.putExtra(PushConstants.IS_REPEAT_PUSH, isRepeatPush)
        intent.putExtra(PushConstants.LOCAL_PUSH_LINK, link)
        return PendingIntent.getBroadcast(context, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }
}