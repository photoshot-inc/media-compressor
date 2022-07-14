package com.devs.adloader

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import com.google.android.gms.ads.MobileAds

interface AdControllerCallback {
    fun getBannerAdId(): String
    fun getInterstitialAdId(): String
    fun getRewardedAdId(): String
    fun getNativeAdId(): String
}

class AdController private constructor() {
    private lateinit var callback: AdControllerCallback

    companion object {
        const val TYPE_BANNER = 1
        const val TYPE_NATIVE = 2
        const val TYPE_INTERSTITIAL = 3
        const val TYPE_REWARDED = 4
        private var instanceStatic: AdController? = null

        @SuppressLint("StaticFieldLeak")
        private var rewardedAdLoader: RewardedAdLoader? = null

        @SuppressLint("StaticFieldLeak")
        private var interstitialAd: InterstitialAdLoader? = null
        private var initialized = false
        val instance: AdController
            get() {
                if (instanceStatic == null) {
                    instanceStatic = AdController()
                }
                return instanceStatic!!
            }


        fun initialize(context: Context, callback: AdControllerCallback) {
            instance.callback = callback
            MobileAds.initialize(context.applicationContext) {
//                Toast.makeText(context, "MobileAds Initialized", Toast.LENGTH_SHORT).show()
                rewardedAdLoader = RewardedAdLoader(context.applicationContext, callback.getRewardedAdId())
                interstitialAd = InterstitialAdLoader(context.applicationContext, callback.getInterstitialAdId())
                initialized = true
                instance.initialized = true
                Log.d(AbstractLoader.TAG, "Mobile Ads initialized")
            }
        }
    }

    var enabled = true
    private var initialized = false
    val isInitialized
        get() = initialized
    var rewardedAdLoadSuccessCallback: RewardedAdLoadSuccessCallback?
        get() = rewardedAdLoader?.loadCallback
        set(value) {
            rewardedAdLoader?.loadCallback = value
        }

    fun canShowAd(type: Int): Boolean {
        if (!enabled) return false
        return when (type) {
            TYPE_REWARDED -> true
            TYPE_INTERSTITIAL -> true
            TYPE_BANNER -> true
            TYPE_NATIVE -> true
            else -> enabled
        }
    }

    internal fun loadNativeAd(view: ViewGroup, lifeCycleOwner: LifecycleOwner): NativeAdLoader? {
        if (!canShowAd(TYPE_NATIVE)) return null
        val nativeAdLoader = NativeAdLoader(view, lifeCycleOwner.lifecycle, callback.getNativeAdId())
        nativeAdLoader.load()
        return nativeAdLoader
    }

    internal fun loadBannerAd(view: ViewGroup, lifeCycleOwner: LifecycleOwner) {
        if (!canShowAd(TYPE_BANNER)) return
        val bannerLoader = BannerLoader(view, lifeCycleOwner.lifecycle, callback.getBannerAdId())
        bannerLoader.load()
    }

    internal fun showRewardedAd(activity: AppCompatActivity, callback: RewardedAdCallback) {
        if (!canShowAd(TYPE_REWARDED)) {
            callback.onRewardedAdNotAvailable()
            return
        }
        if (rewardedAdLoader?.isAvailable == true) {
            rewardedAdLoader?.show(activity, callback)
        }
    }

    internal fun isRewardedAdAvailable(): Boolean = rewardedAdLoader?.isAvailable ?: false

    internal fun showInterstitialAd(activity: Activity) {
        if (!canShowAd(TYPE_INTERSTITIAL)) {
            Log.d("TEST_INTERSTITIAL", "showInterstitialAd: returning not enabled")
            return
        }
        interstitialAd?.show(activity)
    }

//    internal fun showInterstitialAd(activity: AppCompatActivity) {
//        if (!canShowAd(3)) return
//        val interstitialAd = InterstitialAd(activity, callback.getInterstitialAdId())
//        interstitialAd.load()
//    }
}

fun Any.resolveLifecycleOwner(): LifecycleOwner? {
    return when (this) {
        is Fragment -> viewLifecycleOwner
        is AppCompatActivity -> this
        is LifecycleOwner -> this
        is View -> this.context.resolveLifecycleOwner()
        is ViewGroup -> context.resolveLifecycleOwner()
        else -> null
    }
}

object AdProvider {
    @JvmStatic()
    fun Activity.showRewardedAd(callback: RewardedAdCallback) {
        if (this is AppCompatActivity) {
            AdController.instance.showRewardedAd(this, callback)
        }
    }

    @JvmStatic
    fun Activity.isRewardedAdAvailable(): Boolean {
        return AdController.instance.isRewardedAdAvailable()
    }

    @JvmStatic()
    fun Activity.showInterstitialAd(delay: Long = 0) {
        Log.d("TEST_INTERSTITIAL", "showInterstitialAd: ")
        AdController.instance.showInterstitialAd(this)
    }

    @JvmStatic()
    fun Activity.loadNativeAd(viewGroup: ViewGroup): NativeAdLoader? {
        return resolveLifecycleOwner()?.let {
            return@let AdController.instance.loadNativeAd(viewGroup, it)
        }

    }

    @JvmStatic()
    fun Activity.loadBannerAd(viewGroup: ViewGroup) {
        viewGroup.visibility = View.GONE
        return
        resolveLifecycleOwner()?.let { AdController.instance.loadBannerAd(viewGroup, it) }

    }

    @JvmStatic()
    fun Fragment.loadNativeAd(viewGroup: ViewGroup): NativeAdLoader? {
        viewGroup.visibility = View.GONE
        return null
        return resolveLifecycleOwner()?.let {
            return@let AdController.instance.loadNativeAd(viewGroup, it)
        }

    }

    @JvmStatic()
    fun Fragment.loadBannerAd(viewGroup: ViewGroup) {
        viewGroup.visibility = View.GONE
        return
        resolveLifecycleOwner()?.let { AdController.instance.loadBannerAd(viewGroup, it) }

    }
}



