package com.inspiration.imagepicker.domain

import android.util.Log

object NcConstants {
    private const val NATIVE_AD_INTERVAL_IN_FILE_PICKER = 9

    const val MEDIA_PICK_REQUEST_CODE = 113
    fun isNativeAdPosition(position: Int): Boolean {
        return (position >= NATIVE_AD_INTERVAL_IN_FILE_PICKER && (position+1-(position/ NATIVE_AD_INTERVAL_IN_FILE_PICKER)) % NATIVE_AD_INTERVAL_IN_FILE_PICKER == 0)
    }

    fun getRealPositionIgnoringAd(position: Int): Int {
        return position - (position / NATIVE_AD_INTERVAL_IN_FILE_PICKER)
    }

    fun getTotalItemCountWithNativeAd(size: Int): Int {
        return size + (size / NATIVE_AD_INTERVAL_IN_FILE_PICKER)
    }
}


private fun Any.printLog() {
    Log.d("GenericLog", this.toString())
}

