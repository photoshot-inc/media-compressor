package com.devs.adloader

import android.app.Activity
import android.util.DisplayMetrics
import android.util.Log
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import com.google.android.gms.ads.*

class BannerLoader(
    private val viewGroup: ViewGroup,
    lifecycle: Lifecycle,
    private val adId: String
) : AbstractLoader(lifecycle) {
    private fun getAdaptiveAdSize(): AdSize {
        try {
            val display = (viewGroup.context as Activity).windowManager!!.defaultDisplay
            val outMetrics = DisplayMetrics()
            display.getMetrics(outMetrics)

            val density = outMetrics.density

            var adWidthPixels = viewGroup.width.toFloat()
            if (adWidthPixels == 0f) {
                adWidthPixels = outMetrics.widthPixels.toFloat()
            }
            val adWidth = (adWidthPixels / density).toInt()
            return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(viewGroup.context, adWidth)
        } catch (ex: Exception) {
            return AdSize.SMART_BANNER
        }

    }

    private var adview: AdView? = null

    fun load() {
        AdRequest.Builder()
            .build().let { adRequest ->
                if (adview == null) {
                    adview = AdView(viewGroup.context.applicationContext).apply {
                        setAdSize(getAdaptiveAdSize())
                        adUnitId = adId
                        adListener = object : AdListener() {
                            override fun onAdLoaded() {
                                Log.d(TAG, "onAdLoaded: Banner")
                                super.onAdLoaded()
                                viewGroup.addView(adview)
                            }

                            override fun onAdFailedToLoad(p0: LoadAdError) {
                                super.onAdFailedToLoad(p0)
                                Log.d(TAG, "onAdFailedToLoad: Banner " + p0.message)

                            }

                        }
                    }
                }
                adview?.loadAd(adRequest)
            }
    }

    override fun destroy() {
        Log.d(TAG, "destroy: Banner")
        viewGroup.removeAllViews()
        (adview?.parent as? ViewGroup)?.removeAllViews()
        adview?.destroy()
        adview?.removeAllViews()
        adview?.adListener = object : AdListener() {}
        adview = null
    }

}
