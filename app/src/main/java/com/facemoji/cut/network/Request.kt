package com.facemoji.cut.network

import com.google.gson.JsonElement
import com.facemoji.cut.network.entity.GrayItem
import com.facemoji.cut.network.entity.push.LocalPushConfig
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.QueryMap

interface Request {
    companion object {
        const val HOST = "http://api.u-launcher.com/"
    }

    @GET("http://mobotoolpush.moboapps.io/ipo/api/gray/status")
    fun getSwitchConfig(@QueryMap params: MutableMap<String, String>): Call<GrayItem?>?

    @POST("client/v3/user/feedback.json")
    fun postFeedBack(@Body params: MutableMap<String, String?>): Call<JsonElement?>?

    @GET("client/v3/push/push_regularly.json")
    fun getLocalPushConfigRequest(@QueryMap params: Map<String, String?>): Call<LocalPushConfig>?
}