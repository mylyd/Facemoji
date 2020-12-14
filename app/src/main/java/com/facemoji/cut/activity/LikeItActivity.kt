package com.facemoji.cut.activity

import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.facemoji.cut.R
import com.facemoji.cut.aide.MyTrack
import com.facemoji.cut.utils.GradeUtils

class LikeItActivity : BaseActivity(), View.OnClickListener {
    private var image: ImageView? = null
    private var dislike: TextView? = null
    private var start: TextView? = null
    private var back: ImageView? = null

    override fun getLayoutId(): Int = R.layout.activity_like

    override fun init() {
        image = findViewById(R.id.like_bg)
        back = findViewById(R.id.action_back)
        dislike = findViewById(R.id.tv_like_dislike)
        start = findViewById(R.id.tv_like_start)
        dislike?.setOnClickListener(this)
        start?.setOnClickListener(this)
        back?.setOnClickListener(this)
        Glide.with(this).load(R.mipmap.five_start).into(image!!)
    }

    companion object {

        fun newStart(context: Context) {
            val intent = Intent(context, LikeItActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun onClick(view: View?) {
        when(view){
            back -> {
                onBackPressed()
            }
            dislike ->{
                track(MyTrack.click_likeit_dislike)
                FeedbackActivity.newStart(this)
            }
            start -> {
                track(MyTrack.click_likeit_5star)
                GradeUtils.gotoGooglePlay(this)
            }
        }
    }
}