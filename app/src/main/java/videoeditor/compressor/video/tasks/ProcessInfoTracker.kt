package videoeditor.compressor.video.tasks

import com.google.gson.Gson
import videoeditor.compressor.video.features.compress.ProcessingInfo
import videoeditor.compressor.video.models.AppInfo
import java.io.File
import java.lang.Exception

class ProcessInfoTracker(private val appInfo: AppInfo, private val gson: Gson) {
    private val jsonPath = appInfo.filesDir + "/process.json"
    fun obtain(): ProcessingInfo? {
        return try {
            val inputTxt = File(jsonPath).bufferedReader().use { it.readText() }
            gson.fromJson(inputTxt, ProcessingInfo::class.java)
        } catch (ex: Exception) {
            null
        }
    }

    fun save(
        processingInfo: ProcessingInfo,
        onSuccess: (() -> Unit)? = null,
        onError: (() -> Unit)? = null
    ) {
        try {
            File(jsonPath).also {
                it.writeBytes(gson.toJson(processingInfo).toByteArray())
            }
            onSuccess?.invoke()
        } catch (ex: Exception) {
            onError?.invoke()
        }
    }

}