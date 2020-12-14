package com.facemoji.cut.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.CompoundButton
import android.widget.ImageView
import android.widget.Switch
import com.facemoji.cut.R
import com.facemoji.cut.aide.Constants
import com.facemoji.cut.aide.GrayStatus
import com.facemoji.cut.aide.MyTrack
import com.facemoji.cut.push.LocalPushManager
import com.facemoji.cut.utils.SPManager
import com.facemoji.cut.utils.Util

class SettingActivity : BaseActivity(), CompoundButton.OnCheckedChangeListener,
    View.OnClickListener {
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private var switch: Switch? = null
    private var back: ImageView? = null

    override fun getLayoutId(): Int = R.layout.activity_setting

    override fun init() {
        initActionBarHeight()
        Util.changStatusIconColor(this, true)
        Util.setViewPadding(findViewById(R.id.activity_layout), this)
        switch = findViewById(R.id.setting_switch)
        back = findViewById(R.id.action_back)
        back?.setOnClickListener(this)
        val isChecked = SPManager.init().getBoolean(Constants.KEY_SETTING_PUSH_MESSAGE, GrayStatus.push)
        switch?.isChecked = isChecked
        switch?.setOnCheckedChangeListener(this)
    }

    override fun onCheckedChanged(p0: CompoundButton?, isChecked: Boolean) {
        if (isChecked) {
            SPManager.init().setBoolean(Constants.KEY_SETTING_PUSH_MESSAGE,true)
            LocalPushManager.startPush(this)
        } else {
            track(MyTrack.click_setting_push)
            SPManager.init().setBoolean(Constants.KEY_SETTING_PUSH_MESSAGE,false)
        }
    }

    override fun onClick(view: View?) {
        when(view){
            back -> {
                onBackPressed()
            }
        }
    }

    companion object{
        fun newStart(context: Context) {
            val intent = Intent(context, SettingActivity::class.java)
            context.startActivity(intent)
        }
    }
}