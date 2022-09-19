package com.smartcalculator.hcalculator

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.android.billingclient.api.*
import com.smartcalculator.hcalculator.iap.utilies.Prefs

class SplashActivity : AppCompatActivity() {
    private var billingClient: BillingClient? = null
    private var prefs: Prefs? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        prefs = Prefs(this)
        checkSubscription()
        Handler(Looper.getMainLooper()).postDelayed({
            val mainIntent = Intent(this, MainActivity::class.java)
            startActivity(mainIntent)
            finish()
        }, 2000)
    }

    private fun checkSubscription() {
        billingClient = BillingClient.newBuilder(this).enablePendingPurchases()
            .setListener { billingResult: BillingResult?, list: List<Purchase?>? -> }
            .build()
        val finalBillingClient = billingClient
        billingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingServiceDisconnected() {}
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    finalBillingClient?.queryPurchasesAsync(
                        QueryPurchasesParams.newBuilder()
                            .setProductType(BillingClient.ProductType.SUBS).build()
                    ) { billingResult1: BillingResult, list: List<Purchase> ->
                        if (billingResult1.responseCode == BillingClient.BillingResponseCode.OK) {
                            if (list.size > 0) {
                                prefs?.setPremium(1)
                                var i = 0
                                for (purchase in list) {
                                    Log.d("testOffer", purchase.originalJson)
                                    Log.d("testOffer", " index$i")
                                    i++
                                }
                            } else {
                                prefs?.setPremium(0)
                            }
                        }
                    }
                }
            }
        })
    }
}