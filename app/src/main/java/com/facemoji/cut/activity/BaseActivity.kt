package com.facemoji.cut.activity

import android.app.Activity
import android.app.Dialog
import android.content.*
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.facemoji.cut.BuildConfig
import com.facemoji.cut.R
import com.facemoji.cut.activity.BaseActivity.MessageDialogFragment.Companion.ARG_MESSAGE
import com.facemoji.cut.activity.BaseActivity.MessageDialogFragment.Companion.ARG_TITLE_ID
import com.facemoji.cut.aide.Constants
import com.facemoji.cut.aide.tracker.FirebaseTracker
import com.facemoji.cut.sticker.WhitelistCheck
import com.facemoji.cut.utils.Util

/**
 * @author : ydli
 * @time : 2020/11/17 15:14
 * @description :
 */
abstract class BaseActivity : FragmentActivity() {
    private val ADD_PACK = 200
    val EXTRA_STICKER_PACK_ID = "sticker_pack_id"
    val EXTRA_STICKER_PACK_AUTHORITY = "sticker_pack_authority"
    val EXTRA_STICKER_PACK_NAME = "sticker_pack_name"
    private var mReceiver: BroadcastReceiver? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(getLayoutId())
        init()
    }

    protected abstract fun getLayoutId(): Int

    protected abstract fun init()

    fun initActionBarHeight(layout: Int? = R.id.action_bar) {
        //动态根据全面屏适配沉浸式状态栏高度
        val view: View = findViewById(layout!!)
        view.setPadding(
            view.paddingLeft,
            Util.getStatusBarHeight(this),
            view.paddingRight,
            view.paddingBottom
        )
    }

    /**
     * 设置状态栏模式
     * @param setDark 状态栏颜色 true:黑色 false：白色
     * @return
     */
    open fun changStatusIconColor(setDark: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val decorView = window.decorView
            var vis = decorView.systemUiVisibility
            vis = if (setDark) {
                vis or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            } else {
                vis and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
            }
            decorView.systemUiVisibility = vis
        }
    }

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
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, intentFilter)
    }

    protected open fun addStickerPackToWhatsApp(identifier: String, stickerPackName: String) {
        try {
            if (!WhitelistCheck.isWhatsAppConsumerAppInstalled(packageManager) &&
                !WhitelistCheck.isWhatsAppSmbAppInstalled(packageManager)) {
                Toast.makeText(this, R.string.add_pack_fail_prompt_update_whatsapp, Toast.LENGTH_LONG).show()
                return
            }
            val stickerPackWhitelistedInWhatsAppConsumer: Boolean =
                WhitelistCheck.isStickerPackWhitelistedInWhatsAppConsumer(this, identifier)
            val stickerPackWhitelistedInWhatsAppSmb: Boolean =
                WhitelistCheck.isStickerPackWhitelistedInWhatsAppSmb(this, identifier)
            if (!stickerPackWhitelistedInWhatsAppConsumer && !stickerPackWhitelistedInWhatsAppSmb) {
                //ask users which app to add the pack to.
                launchIntentToAddPackToChooser(identifier, stickerPackName)
            } else if (!stickerPackWhitelistedInWhatsAppConsumer) {
                launchIntentToAddPackToSpecificPackage(
                    identifier,
                    stickerPackName,
                    WhitelistCheck.CONSUMER_WHATSAPP_PACKAGE_NAME
                )
            } else if (!stickerPackWhitelistedInWhatsAppSmb) {
                launchIntentToAddPackToSpecificPackage(
                    identifier,
                    stickerPackName,
                    WhitelistCheck.SMB_WHATSAPP_PACKAGE_NAME
                )
            } else {
                Toast.makeText(this, R.string.add_pack_fail_prompt_update_whatsapp,
                    Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            Log.e("sticker", "error adding sticker pack to WhatsApp", e)
            Toast.makeText(this, R.string.add_pack_fail_prompt_update_whatsapp, Toast.LENGTH_LONG).show()
        }
    }

    private fun launchIntentToAddPackToSpecificPackage(identifier: String, stickerPackName: String,
        whatsappPackageName: String) {
        val intent: Intent = createIntentToAddStickerPack(identifier, stickerPackName)
        intent.setPackage(whatsappPackageName)
        try {
            startActivityForResult(intent,ADD_PACK)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, R.string.add_pack_fail_prompt_update_whatsapp, Toast.LENGTH_LONG).show()
        }
    }

    private fun launchIntentToAddPackToChooser(identifier: String, stickerPackName: String) {
        val intent: Intent = createIntentToAddStickerPack(identifier, stickerPackName)
        try {
            startActivityForResult(Intent.createChooser(intent, getString(R.string.add_to_whatsapp)), ADD_PACK)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this,
                R.string.add_pack_fail_prompt_update_whatsapp, Toast.LENGTH_LONG).show()
        }
    }

    private fun createIntentToAddStickerPack(identifier: String, stickerPackName: String): Intent {
        val intent = Intent()
        intent.action = "com.whatsapp.intent.action.ENABLE_STICKER_PACK"
        intent.putExtra(EXTRA_STICKER_PACK_ID, identifier)
        intent.putExtra(EXTRA_STICKER_PACK_AUTHORITY, BuildConfig.CONTENT_PROVIDER_AUTHORITY)
        intent.putExtra(EXTRA_STICKER_PACK_NAME, stickerPackName)
        return intent
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode ==ADD_PACK) {
            if (resultCode == Activity.RESULT_CANCELED) {
                if (data != null) {
                    val validationError = data.getStringExtra("validation_error")
                    if (validationError != null) {
                        if (BuildConfig.DEBUG) {
                           MessageDialogFragment.newInstance(
                                R.string.title_validation_error,
                                validationError
                            ).show(supportFragmentManager, "validation error")
                        }
                        Log.e("sticker", "Validation failed:$validationError")
                    }
                } else {
                    // StickerPackNotAddedMessageFragment().show(getSupportFragmentManager(), "sticker_pack_not_added");
                }
            }
        }
    }

    class MessageDialogFragment : DialogFragment() {
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            @StringRes val title =
                arguments!!.getInt(ARG_TITLE_ID)
            val message =
                arguments!!.getString(ARG_MESSAGE)
            val dialogBuilder =
                AlertDialog.Builder(activity!!)
                    .setMessage(message)
                    .setCancelable(true)
                    .setPositiveButton(
                        android.R.string.ok
                    ) { dialog: DialogInterface?, which: Int -> dismiss() }
            if (title != 0) {
                dialogBuilder.setTitle(title)
            }
            return dialogBuilder.create()
        }

        internal companion object {
            private const val ARG_TITLE_ID = "title_id"
            private const val ARG_MESSAGE = "message"

            fun newInstance(@StringRes titleId: Int, message: String?): DialogFragment {
                val fragment: DialogFragment = MessageDialogFragment()
                val arguments = Bundle()
                arguments.putInt(ARG_TITLE_ID, titleId)
                arguments.putString(ARG_MESSAGE, message)
                fragment.arguments = arguments
                return fragment
            }
        }
    }

    override fun onDestroy() {
        if (mReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver!!)
        }
        super.onDestroy()
    }
}