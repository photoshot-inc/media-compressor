package videoeditor.compressor.video.models

import android.app.Application
import android.content.res.Resources
import android.os.Environment

class AppInfo(val context: Application) {

    val size: Size

    init {
        val metrics = Resources.getSystem().displayMetrics
        size = Size(metrics.widthPixels, metrics.heightPixels)
    }

    val filesDir: String?
        get() = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)?.absolutePath

    val cacheDir: String
        get() = context.cacheDir.absolutePath
}