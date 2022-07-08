package videoeditor.compressor.video.di

import dagger.Component
import videoeditor.compressor.video.MApplication
import videoeditor.compressor.video.features.compress.CompressOptionViewModel
import videoeditor.compressor.video.service.CompressionService
import javax.inject.Singleton

@Singleton
@Component(modules = [(AppModule::class)])
interface AppComponent {

    @Component.Builder
    interface Builder {
        fun appModule(appModule: AppModule): Builder
        fun build(): AppComponent
    }

//    fun inject(mApplication: MApplication)
    fun inject(mApplication: CompressOptionViewModel)
    fun inject(service: CompressionService)
}
