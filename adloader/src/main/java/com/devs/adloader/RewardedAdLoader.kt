package com.devs.adloader

import android.content.Context
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

interface RewardedAdCallback {
    fun onRewarded()
    fun onRewardedAdNotAvailable()
}

interface RewardedAdLoadSuccessCallback {
    fun onLoadSuccess()
}

class RewardedAdLoader(val context: Context, private val adUnitId: String) {
    companion object {
        private const val TAG = "AD_LOADER_DEBUG"
        private var rewardedAd: RewardedAd? = null
    }

    init {
        reload()
    }

    var loadCallback: RewardedAdLoadSuccessCallback? = null
    var retryCnt = 0
    private fun reload() {
        if (loadingAd || rewardedAd!=null) {
            Log.d(TAG, "reload: already in progress exiting ")
            return
        }
        loadingAd = true
        Log.d(TAG, "reloading rewarded ad")
        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(context, adUnitId, adRequest, object : RewardedAdLoadCallback() {
            override fun onAdLoaded(p0: RewardedAd) {
                onAdLoadSuccess(p0)
                loadingAd = false
                Log.d(TAG, "onAdLoaded: RewardedAd")
                retryCnt = 0
                super.onAdLoaded(p0)
            }

            override fun onAdFailedToLoad(p0: LoadAdError) {
                loadingAd = false
                Log.d(TAG, "onAdFailedToLoad:RewardedAd code ${p0.code} ${p0.message}")
                if (retryCnt < 3) {
                    retryCnt++
                    reload()
                } else retryCnt = 0
                super.onAdFailedToLoad(p0)
            }
        })


    }

    val isAvailable: Boolean
        get() {
            if (rewardedAd == null) reload()
            return rewardedAd != null
        }
    private var _showing = false
    val showing: Boolean
        get() = _showing

    private var loadingAd = false

    fun show(activity: AppCompatActivity, callback: RewardedAdCallback) {
        if (isAvailable) {
            rewardedAd?.show(activity) {
                _showing = false
                if (activity.lifecycle.currentState != Lifecycle.State.DESTROYED) {
                    Log.d(TAG, "show: onRewarded notify")
                    callback.onRewarded()
                } else {
                    Log.d("TAG", "show: rewardedAd but activity destroyed")
                }
            } ?: callback.onRewardedAdNotAvailable()
            Log.d("TEST_TEST", "show: reward removed ")
            rewardedAd = null
        }
        reload()
    }

    private fun onAdLoadSuccess(ad: RewardedAd) {
        rewardedAd = ad
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
        loadCallback?.onLoadSuccess()
    }

}