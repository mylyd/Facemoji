package com.facemoji.cut.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.facemoji.cut.R
import com.facemoji.cut.aide.MyTrack
import com.facemoji.cut.network.CommonCallback
import com.facemoji.cut.utils.Util
import com.google.gson.JsonElement

class FeedbackActivity : BaseActivity(), View.OnClickListener {
    private var imageBack: ImageView? = null
    private var email: EditText? = null
    private var opinions: EditText? = null
    private var submit: TextView? = null
    private var opinionsSize: TextView? = null
    override fun getLayoutId(): Int = R.layout.activity_feedback

    override fun init() {
        initActionBarHeight()
        Util.setViewPadding(findViewById(R.id.activity_layout),this)
        //设置状态栏图标颜色
        changStatusIconColor(true)
        imageBack = findViewById(R.id.action_back)
        email = findViewById(R.id.email)
        opinions = findViewById(R.id.opinions)
        submit = findViewById(R.id.feedback_submit)
        opinionsSize = findViewById(R.id.opinions_size)
        imageBack?.setOnClickListener(this)
        submit?.setOnClickListener(this)
        setEditActionListener()
    }

    override fun onClick(view: View?) {
        when (view) {
            imageBack -> {
                onBackPressed()
            }
            submit -> {
                track(MyTrack.click_likeit_feedback_submit)
                val email: String = email?.text.toString()
                val suggestion: String = opinions?.text.toString()
                if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(suggestion)) {
                    submit?.isEnabled = false
                    requestFeedBack(email, suggestion)
                    onBackPressed()
                } else {
                    Toast.makeText(
                        this@FeedbackActivity,
                        getString(R.string.feedback_error),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    /**
     * 提交反馈信息
     *
     * @param email
     * @param suggestion
     */
    private fun requestFeedBack(emails: String, suggestion: String) {
        Util.requestFeedBack(this@FeedbackActivity, emails, suggestion,
            object : CommonCallback<JsonElement?>() {
                override fun onResponse(response: JsonElement?) {
                    Toast.makeText(this@FeedbackActivity,
                        getString(R.string.feedback_success), Toast.LENGTH_SHORT).show()
                    submit?.isEnabled = true
                    email?.text = null
                    opinions?.text = null
                    opinionsSize?.text = getString(R.string.opinions_text_count)
                }

                override fun onFailure(t: Throwable?, isServerUnavailable: Boolean) {
                    submit?.isEnabled = true
                    Toast.makeText(this@FeedbackActivity,
                        getString(R.string.feedback_failed), Toast.LENGTH_SHORT).show()
                }
            })
    }

    /**
     * 监听软件盘，控制软键盘回车按钮完成搜索功能
     */
    private fun setEditActionListener() {
        email?.setOnEditorActionListener { _: TextView?, actionId: Int, _: KeyEvent? ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                hideKeyboard(email!!)
            }
            false
        }

        opinions?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                //内容输出后
            }

            @SuppressLint("SetTextI18n")
            override fun beforeTextChanged(text: CharSequence?, start: Int, before: Int, count: Int) {
                //内容输出前
                opinionsSize?.text = "${count}/200"
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                //内容输出中
            }

        })
    }

    /**
     * 隐藏软键盘
     *
     * @param view :一般为EditText
     */
    fun hideKeyboard(view: View) {
        val manager = view.context
            .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        manager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    /**
     * 显示软键盘
     *
     * @param view :一般为EditText
     */
    private fun showKeyboard(view: View, editText: EditText) {
        view.postDelayed({
            val manager = view.context
                .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            manager.showSoftInput(editText, 0)
        }, 10)
    }

    companion object {
        fun newStart(context: Context) {
            val intent = Intent(context, FeedbackActivity::class.java)
            context.startActivity(intent)
        }
    }
}