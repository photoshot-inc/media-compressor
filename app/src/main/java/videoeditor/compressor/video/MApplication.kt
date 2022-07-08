package videoeditor.compressor.video

import android.app.Application
import com.devs.adloader.AdController
import com.devs.adloader.AdControllerCallback
import videoeditor.compressor.video.di.AppComponent
import videoeditor.compressor.video.models.AppInfo
import videoeditor.compressor.video.di.AppModule
import videoeditor.compressor.video.di.DaggerAppComponent

class MApplication : Application(), AdControllerCallback {
    val component: AppComponent
        get() = MApplication.instance.injector

    companion object {
        lateinit var instance: MApplication
    }

    lateinit var injector: AppComponent
    override fun onCreate() {
        super.onCreate()
        injector = DaggerAppComponent.builder()
            .appModule(AppModule(this, AppInfo(this)))
            .build()
//        injector.inject(this)
        AdController.initialize(this,this)
        instance = this
    }

    override fun getBannerAdId(): String {
        return "ca-app-pub-3940256099942544/6300978111"
    }

    override fun getInterstitialAdId(): String {
     return "ca-app-pub-3940256099942544/1033173712"
    }

    override fun getRewardedAdId(): String {
        return "ca-app-pub-3940256099942544/5224354917"
    }

    override fun getNativeAdId(): String {
       return "ca-app-pub-3940256099942544/2247696110"
    }
}