package com.facemoji.cut.billing

import android.app.Activity
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.android.billingclient.api.*
import com.facemoji.cut.MyApp
import com.facemoji.cut.R
import com.google.android.gms.common.util.CollectionUtils
import com.facemoji.cut.aide.Constants
import com.facemoji.cut.utils.SPManager

/**
 * @author : ydli
 * @time : 2020/11/16 16:32
 * @description :
 */
class BillingManager(private val myApp: MyApp) : PurchasesUpdatedListener{
    private var billingClient: BillingClient? = null
    private var params: SkuDetailsParams.Builder? = null
    private val subType: String = BillingClient.SkuType.INAPP //一次性商品
    private var skuDetailsList: List<SkuDetails>? = null
    private val product: String = "facemoji_life"
    var whetherToBuy = false

    init {
        whetherToBuy = SPManager.init().getBoolean(key,false)
        initBilling()
    }

    fun getBillingClient() :BillingClient = billingClient!!

    fun initBilling() {
        billingClient = BillingClient.newBuilder(myApp).enablePendingPurchases().setListener(this).build()
        if (billingClient == null){
            Log.d(TAG, "initBilling: billingClient == null")
            return
        }
        billingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingServiceDisconnected() {
                //初始化失败
                Log.d(TAG, "onBillingServiceDisconnected: ")
            }

            override fun onBillingSetupFinished(billingClient : BillingResult) {
                if (billingClient.responseCode == BillingClient.BillingResponseCode.OK && !whetherToBuy){
                    //初始化成功
                    initSub()
                }
            }
        })
    }

    fun initSub(){
        params = SkuDetailsParams.newBuilder()
        val ids : MutableList<String> = mutableListOf()
        ids.add(product)  //inApp 一次性商品
        //ids.add("smilemoji_year") //Sub 订阅类商品
        params!!.setSkusList(ids)?.setType(subType)
        billingClient?.querySkuDetailsAsync(params!!.build(), object : SkuDetailsResponseListener{
            override fun onSkuDetailsResponse(billingResult: BillingResult, skuDetailsList: List<SkuDetails>?) {
                if (CollectionUtils.isEmpty(skuDetailsList)){
                    //获取订阅资源失败提示用户网络问题或者未安装GP
                    Log.d(TAG, "onSkuDetailsResponse: null")
                    return
                }
                this@BillingManager.skuDetailsList = skuDetailsList
                Log.d(TAG, "onSkuDetailsResponse: success")
            }
        })
    }

    fun showSub(activity: Activity){
        if (whetherToBuy) {
            Toast.makeText(myApp, myApp.getString(R.string.whether_to_buy), Toast.LENGTH_SHORT).show()
            return
        }
        if (CollectionUtils.isEmpty(skuDetailsList)){
            Toast.makeText(myApp, myApp.getString(R.string.google_pay), Toast.LENGTH_SHORT).show()
            return
        }
        if (billingClient!!.isFeatureSupported(BillingClient.FeatureType.SUBSCRIPTIONS)
                .responseCode != BillingClient.BillingResponseCode.OK) {
            Log.d(TAG, "showSub: 不支持 google支付")
            return
        }
        val paramsFlow = BillingFlowParams.newBuilder().setSkuDetails(skuDetailsList!![0]).build()
        val billingResult = billingClient?.launchBillingFlow(activity, paramsFlow)
        if (billingResult?.responseCode == BillingClient.BillingResponseCode.OK) {
            Log.d(TAG, "showSub: billingResult == ok")
        }
    }

    /**
     * 查询最近的购买交易（网络）
     *
     * @return
     */
    fun queryPurchaseHistoryAsync(billingClient: BillingClient): Boolean {
        var query = false
        billingClient.queryPurchaseHistoryAsync(BillingClient.SkuType.INAPP
        ) { p0, p1 ->
            if (p0.responseCode == BillingClient.BillingResponseCode.OK && p1?.isNotEmpty()!!){
                for (i in p1.indices){
                    if (product == p1[i].sku) {
                        query = true
                        return@queryPurchaseHistoryAsync
                    }
                }
            }
        }
        return query
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: MutableList<Purchase>?) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK ){
            Log.d(TAG, "onPurchasesUpdated:")
            Toast.makeText(myApp, myApp.getString(R.string.google_success), Toast.LENGTH_SHORT).show()
            SPManager.init().setBoolean(key, true)
            whetherToBuy = true
            registerIntent()
            //消耗一次性物品，如果是永久购买类型则不用消耗
            billingClient?.consumeAsync(
                ConsumeParams.newBuilder().setPurchaseToken(purchases!![0].purchaseToken).build()) {
                    p0, _ ->
                if (p0.responseCode == BillingClient.BillingResponseCode.OK){
                    Log.d(TAG, "onConsumeResponse: 消费成功")
                }
            }
        }
    }

    /**
     * 发送控制订阅成功去除广告
     */
    private fun registerIntent() {
        val intent = Intent(Constants.NO_ADS)
        LocalBroadcastManager.getInstance(myApp).sendBroadcast(intent)
    }

    companion object {
        private const val TAG = "BillingManager"
        //本地储存
        const val key: String = "GooglePay_Product_Key"
    }
}