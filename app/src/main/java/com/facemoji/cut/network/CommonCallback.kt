package com.facemoji.cut.network
import java.io.IOException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

abstract class CommonCallback<T> : Callback<T?> {
    override fun onResponse(call: Call<T?>, response: Response<T?>) {
        when {
            !response.isSuccessful -> this.onFailure(IOException(response.toString()), false)
            response.body() == null -> this.onFailure(IOException(response.toString()), false)
            else -> onResponse(response.body())
        }
    }

    override fun onFailure(call: Call<T?>, t: Throwable) = this.onFailure(t, true)

    abstract fun onResponse(response: T?)

    /**
     * @param t
     * @param isServerUnavailable 服务不可用/连不上/超时/本地错误
     */
    abstract fun onFailure(t: Throwable?, isServerUnavailable: Boolean)

    companion object {
        private const val TAG = "CommonCallback"
    }
}