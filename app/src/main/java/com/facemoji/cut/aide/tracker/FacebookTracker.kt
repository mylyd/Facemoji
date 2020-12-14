package com.facemoji.cut.aide.tracker

import android.content.Context
import android.os.Bundle
import android.util.Log
import com.facebook.FacebookSdk
import com.facebook.appevents.AppEventsConstants
import com.facebook.appevents.AppEventsLogger

/**
 * @author : ydli
 * @time : 2020/11/11 8:56
 * @description :
 */
class FacebookTracker {
    fun init(context: Context?) {
        FacebookSdk.sdkInitialize(context)
        mAppEventsLogger = AppEventsLogger.newLogger(context)
    }

    fun track(eventName: String) {
        if (mAppEventsLogger == null) {
            //throw new IllegalStateException("FacebookTracker should be initialzed first.");
            return
        }
        try {
            mAppEventsLogger!!.logEvent(eventName)
        } catch (e: Exception) {
            //e.printStackTrace();
        }
        Log.d(TAG, "track $eventName")
    }

    /**
     * This function assumes logger is an instance of AppEventsLogger and has been
     * created using AppEventsLogger.newLogger() call.
     */
    fun unlockAchievementEvent(description: String?) {
        if (mAppEventsLogger == null) {
            //throw new IllegalStateException("FacebookTracker should be initialzed first.");
            return
        }
        try {
            val params = Bundle()
            params.putString(AppEventsConstants.EVENT_PARAM_DESCRIPTION, description)
            mAppEventsLogger!!.logEvent(
                AppEventsConstants.EVENT_NAME_UNLOCKED_ACHIEVEMENT,
                params
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * This function assumes logger is an instance of AppEventsLogger and has been
     * created using AppEventsLogger.newLogger() call.
     */
    fun viewContentEvent(description: String?) {
        if (mAppEventsLogger == null) {
            //throw new IllegalStateException("FacebookTracker should be initialzed first.");
            return
        }
        try {
            val params = Bundle()
            params.putString(AppEventsConstants.EVENT_PARAM_CONTENT_TYPE, "product")
            params.putString(AppEventsConstants.EVENT_PARAM_CONTENT, description)
            params.putString(AppEventsConstants.EVENT_PARAM_CONTENT_ID, "a")
            params.putString(AppEventsConstants.EVENT_PARAM_CURRENCY, "USD")
            mAppEventsLogger!!.logEvent(
                AppEventsConstants.EVENT_NAME_VIEWED_CONTENT,
                1.0,
                params
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        private const val TAG = "FacebookTracker"

        //event name
        private var mFacebookTracker: FacebookTracker? = null
        private var mAppEventsLogger: AppEventsLogger? = null
        val instance: FacebookTracker?
            get() {
                if (mFacebookTracker == null) {
                    synchronized(FacebookTracker::class.java) {
                        if (mFacebookTracker == null) {
                            mFacebookTracker = FacebookTracker()
                        }
                    }
                }
                return mFacebookTracker
            }
    }
}