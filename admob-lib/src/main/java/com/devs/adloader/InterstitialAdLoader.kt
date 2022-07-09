package com.devs.adloader

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

class InterstitialAdLoader(val context: Context, private val adUnitId: String) {
    companion object {
        private const val TAG = "AD_LOADER_DEBUG"
        private var interstitialAd: InterstitialAd? = null
    }

    init {
        reload()
    }

    var retryCnt = 0
    private fun reload() {
        Log.d(TAG, "reload: reloading interstitial ad")
        if (loadingAd || interstitialAd != null) {
            Log.d(TAG, "reload: already in progress exiting ")
            return
        }
        loadingAd = true
        Log.d(TAG, "reload: ")
        val adRequest = AdRequest.Builder().build()

        InterstitialAd.load(context, adUnitId, adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                loadingAd = false
                Log.d(TAG, "onAdFailedToLoad: InterstitialAd ${adError.code} ${adError.message}")
                if (retryCnt < 3) {
                    retryCnt++
                    reload()
                } else retryCnt = 0
            }

            override fun onAdLoaded(ad: InterstitialAd) {
                super.onAdLoaded(ad)
                Log.d(TAG, "interstitial ad loaded")
                loadingAd = false
                onAdLoadSuccess(ad)
                retryCnt = 0
            }
        })


    }

    val isAvailable: Boolean
        get() {
            if (interstitialAd == null) reload()
            return interstitialAd != null
        }
    private var _showing = false
    val showing: Boolean
        get() = _showing

    private var loadingAd = false

    fun show(activity: Activity) {
        if (isAvailable) {
            interstitialAd?.show(activity)
            interstitialAd = null
            reload()
        } else {
            reload()
        }
    }

    private fun onAdLoadSuccess(ad: InterstitialAd) {
        interstitialAd = ad
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() {
                super.onAdShowedFullScreenContent()
                _showing = true
            }

            override fun onAdDismissedFullScreenContent() {
                super.onAdDismissedFullScreenContent()
                _showing = false
            }

            override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                super.onAdFailedToShowFullScreenContent(p0)
                _showing = false
            }
        }
    }

}