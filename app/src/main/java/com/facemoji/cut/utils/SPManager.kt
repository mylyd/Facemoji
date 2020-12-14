package com.facemoji.cut.utils

import android.content.Context
import android.content.SharedPreferences
import com.facemoji.cut.MyApp
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.facemoji.cut.aide.Constants
import com.facemoji.cut.network.entity.AddPreview
import com.facemoji.cut.network.entity.push.LocalPushMessage

/**
 * @author : ydli
 * @time : 2020/11/11 8:56
 * @description :
 */
class SPManager() {
    private var mSharedPreferences: SharedPreferences? = null
    fun clear() {
        mSharedPreferences!!.edit().clear().apply()
    }

    fun setInt(key: String, value: Int) {
        mSharedPreferences!!.edit().putInt(key, value).apply()
    }

    fun getInt(key: String, defValue: Int): Int {
        return mSharedPreferences!!.getInt(key, defValue)
    }

    fun setLong(key: String, value: Long) {
        mSharedPreferences!!.edit().putLong(key, value).apply()
    }

    fun getLong(key: String, defValue: Long): Long {
        return mSharedPreferences!!.getLong(key, defValue)
    }

    fun setString(key: String, value: String) {
        mSharedPreferences!!.edit().putString(key, value).apply()
    }

    fun getString(key: String): String? {
        return mSharedPreferences?.getString(key, null)
    }

    fun setBoolean(key: String, value: Boolean) {
        mSharedPreferences!!.edit().putBoolean(key, value).apply()
    }

    fun getBoolean(key: String, defValue: Boolean): Boolean {
        return mSharedPreferences!!.getBoolean(key, defValue)
    }

    fun setList(key: String, list: List<String>) {
        val ret = Gson().toJson(list)
        mSharedPreferences!!.edit().putString(key, ret).apply()
    }

    /**
     * 存储LocalPush延时时间数据
     *
     * @param intervals
     */
    fun putLocalPushIntervalList(intervals: List<Int?>?) {
        val editor = mSharedPreferences!!.edit()
        val json = Gson().toJson(intervals)
        editor.putString(Constants.KEY_LOCAL_PUSH_INTERVAL_DATA, json).apply()
    }

    /**
     * 获取LocalPush延时时间数据
     *
     * @return
     */
    open fun getLocalPushIntervalList(): List<Int>? {
        val json = mSharedPreferences!!.getString(Constants.KEY_LOCAL_PUSH_INTERVAL_DATA, null)
        return Gson().fromJson<List<Int>>(json, object : TypeToken<List<Int?>?>() {}.type)
    }

    /**
     * 存储LocalPushItem数据
     *
     * @param items
     */
    fun putLocalPushMessageList(items: List<LocalPushMessage>) {
        val editor = mSharedPreferences!!.edit()
        val json = Gson().toJson(items)
        editor.putString(Constants.KEY_LOCAL_PUSH_MESSAGE_DATA, json).apply()
    }

    /**
     * 获取LocalPushItem数据
     *
     * @return
     */
    fun getLocalPushMessageList(): ArrayList<LocalPushMessage> {
        val json =
            mSharedPreferences!!.getString(Constants.KEY_LOCAL_PUSH_MESSAGE_DATA, null)
        return Gson().fromJson<ArrayList<LocalPushMessage>>(json, object : TypeToken<ArrayList<LocalPushMessage>>() {}.type)
    }

    /**
     * 存储LocalPushItem数据
     *
     * @param items
     */
    fun addPreview(items: MutableList<AddPreview>) {
        val editor = mSharedPreferences!!.edit()
        val json = Gson().toJson(items)
        editor.putString(Constants._ADD_WHATAPPS, json).apply()
    }

    /**
     * 获取LocalPushItem数据
     *
     * @return
     */
    fun getPreview(): MutableList<AddPreview>? {
        val json = mSharedPreferences!!.getString(Constants._ADD_WHATAPPS, null)
        return Gson().fromJson<ArrayList<AddPreview>>(json, object : TypeToken<ArrayList<AddPreview>>() {}.type)
    }

    companion object {
        private val sInstance: SPManager = SPManager()
        fun init(): SPManager = sInstance.init()
    }

    fun init(): SPManager {
        if (mSharedPreferences != null) return this@SPManager
        val sharedName = MyApp.instance.packageName + "_preferences"
        mSharedPreferences = MyApp.instance
            .getSharedPreferences(sharedName, Context.MODE_PRIVATE)
        return this@SPManager
    }
}