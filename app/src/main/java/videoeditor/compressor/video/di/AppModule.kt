package videoeditor.compressor.video.di

import android.app.Application
import android.content.Context
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import videoeditor.compressor.video.models.AppInfo
import videoeditor.compressor.video.tasks.ProcessInfoTracker
import javax.inject.Singleton

@Module
class AppModule(private val application: Application, private val info: AppInfo) {

    @Provides
    fun provideContext(): Application {
        return application
    }

    @Provides
    fun provideAppInfo(): AppInfo {
        return info
    }

    @Provides
    fun context(): Context {
        return application.applicationContext
    }

    @Provides
    fun gson(): Gson {
        return Gson()
    }


    @Singleton
    @Provides
    fun provideProcessTracker(appInfo: AppInfo, gson: Gson): ProcessInfoTracker {
        return ProcessInfoTracker(appInfo, gson)
    }
}
