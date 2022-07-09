package com.devs.adloader

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import com.google.android.gms.ads.*
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.devs.adloader.nativeJava.NativeTemplateStyle
import com.devs.adloader.nativeJava.TemplateView


class NativeAdLoader(
    private val viewGroup: ViewGroup,
    lifecycle: Lifecycle,

    adId: String,
) : AbstractLoader(lifecycle) {
    private var nativeAd: NativeAd? = null
    val adLoader = AdLoader.Builder(viewGroup.context, adId)
        .forNativeAd { ad: NativeAd ->
            nativeAd = ad
            if (lifecycle.currentState == Lifecycle.State.DESTROYED) {
                nativeAd?.destroy()
                return@forNativeAd
            }
            Log.d(TAG, "native ad loaded inflating")
            val inflater = LayoutInflater.from(viewGroup.context)
            val adView = inflater.inflate(R.layout.templateview, viewGroup, false)
            val styles: NativeTemplateStyle = NativeTemplateStyle.Builder()
                .withMainBackgroundColor(ColorDrawable(Color.WHITE))
                .build()
            val template: TemplateView = adView.findViewById(R.id.my_template)
            template.setStyles(styles)
            template.setNativeAd(ad)
            viewGroup.removeAllViews()
            viewGroup.addView(adView)
        }
        .withAdListener(object : AdListener() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
            }
        })
        .withNativeAdOptions(
            NativeAdOptions.Builder()
                .build()
        )
        .build()

    fun load() {
        Log.d(TAG, "load: nativeAd")
        AdRequest.Builder()
            .build().let { adRequest ->
                adLoader.loadAd(adRequest)
            }
    }

    override fun destroy() {
        Log.d(TAG, "destroy: NativeAd")
        nativeAd?.destroy()
    }

    fun forceDestroy() {
        Log.d(TAG, "forceDestroy: NativeAd")
        lifecycle.removeObserver(this)
        nativeAd?.destroy()
    }

}