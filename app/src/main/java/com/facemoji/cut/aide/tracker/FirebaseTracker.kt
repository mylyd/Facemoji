package com.facemoji.cut.aide.tracker

import android.content.Context
import android.text.TextUtils
import com.google.firebase.analytics.FirebaseAnalytics

/**
 * @author : ydli
 * @time : 2020/11/11 8:56
 * @description :
 */
class FirebaseTracker {
    fun init(context: Context?) {
        mContext = context
    }

    fun track(eventName: String?) {
        var eventName = eventName
        checkNotNull(mContext) { "FirebaseTracker should be initialzed first." }
        //在这里截取40长度，因为firebase的eventName超过40会报错
        if (eventName!!.length > 40) eventName = eventName.substring(0, 39)
        else if (TextUtils.isEmpty(eventName)) return
        FirebaseAnalytics.getInstance(mContext!!).logEvent(eventName, null)
    }

    companion object {
        private var mContext: Context? = null
        private var mFirebaseTracker: FirebaseTracker? = null
        val instance: FirebaseTracker
            get() {
                if (mFirebaseTracker == null) {
                    synchronized(FirebaseTracker::class.java) {
                        if (mFirebaseTracker == null) {
                            mFirebaseTracker = FirebaseTracker()
                        }
                    }
                }
                return mFirebaseTracker!!
            }
    }
}