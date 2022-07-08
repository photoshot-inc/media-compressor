package videoeditor.compressor.video.service

import android.app.Service
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.linkedin.android.litr.MediaTransformer
import com.linkedin.android.litr.TransformationListener
import com.linkedin.android.litr.analytics.TrackTransformationInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import devs.core.logAsInfo
import devs.core.logError
import devs.core.utils.safeRun
import videoeditor.compressor.video.MainActivity
import videoeditor.compressor.video.R
import videoeditor.compressor.video.di.injector
import videoeditor.compressor.video.features.compress.ProcessStatus
import videoeditor.compressor.video.features.compress.ProcessingInfo
import videoeditor.compressor.video.helper.AppNotification
import videoeditor.compressor.video.tasks.ProcessInfoTracker
import java.io.File
import javax.inject.Inject

sealed class ServiceState {
    object Idle : ServiceState()
    object Processing : ServiceState()
    object Started : ServiceState()
    object Cancelled : ServiceState()
    data class Success(val data: ProcessingInfo) : ServiceState()
    data class Failed(val message: String? = null) : ServiceState()
}

class CompressionService : Service() {
    companion object {
        private const val TAG = "CompressionService"
        fun startService(context: Context) {
            ContextCompat.startForegroundService(
                context,
                Intent(context, CompressionService::class.java)
            )
        }
    }

    inner class MBinder : Binder() {
        fun getService(): CompressionService = this@CompressionService
    }

    private val binder by lazy { MBinder() }

    private var inProgress = false
    private val _status = MutableLiveData<ServiceState>(ServiceState.Idle)
    val state: LiveData<ServiceState> get() = _status
    private val _progress = MutableLiveData<Int>()
    val progress: LiveData<Int> get() = _progress
    private val processNotification: AppNotification by lazy {
        val notificationIntent: Intent =
            Intent(this, MainActivity::class.java)
//        notificationIntent.putExtra(BundleKeys.KEY_TAB_INDEX, (1).toInt())
        notificationIntent.flags =
            (Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        AppNotification(this, R.mipmap.ic_launcher, 1, notificationIntent)
    }

    fun getNotification(id: Int): AppNotification {
        val notificationIntent: Intent =
            Intent(this, MainActivity::class.java)
//        notificationIntent.putExtra(BundleKeys.KEY_TAB_INDEX, (1).toInt())
        notificationIntent.flags =
            (Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        return AppNotification(this, R.mipmap.ic_launcher, id, notificationIntent)

    }

    @Inject
    lateinit var tracker: ProcessInfoTracker

    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onBind(p0: Intent?): IBinder {
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        injector.inject(this)
        startForeground(1, processNotification.notification.build())
        checkForInCompleteTask()
    }

    private fun checkForInCompleteTask() {
        scope.launch {
            tracker.obtain().let {
                if (it == null) exitFromService()
                else {
                    val hasPendingTask = when (it.processStatus) {
                        ProcessStatus.IN_QUEUE -> {
                            compressVideo(it)
                            true
                        }
                        ProcessStatus.IN_PROGRESS -> {
                            false
                        }
                        ProcessStatus.CANCELLED -> {
                            false
                        }
                        ProcessStatus.SUCCESS -> {
                            _status.postValue(ServiceState.Success(it))
                            false
                        }
                        ProcessStatus.FAILED -> {
                            false
                        }
                    }
                    if (!hasPendingTask) exitFromService()
                }

            }
        }
    }

    private fun exitFromService() {
        stopForeground(true)
        stopSelf()
    }


    private fun compressVideo(processingInfo: ProcessingInfo) {
        if (inProgress) return
        inProgress = true
        _progress.postValue(-1)
        val compressor = MediaTransformer(this)
        val processId = 5.toString()
        compressor.transform(
            processId,
            Uri.parse(processingInfo.inputUri),
            processingInfo.outputPath,
            null,
            null,
            object : TransformationListener {
                override fun onStarted(id: String) {
                    "process started".logAsInfo()
                    _status.postValue(ServiceState.Started)
                }

                override fun onProgress(id: String, progress: Float) {
                    "progress $progress".logAsInfo()
                    _progress.postValue((progress * 100).toInt())
                    _status.postValue(ServiceState.Processing)
                }

                override fun onCompleted(
                    id: String,
                    trackTransformationInfos: MutableList<TrackTransformationInfo>?
                ) {
                    "process successful".logAsInfo()
                    inProgress = false
                    _status.postValue(ServiceState.Success(processingInfo))
                    processingInfo.processStatus = ProcessStatus.SUCCESS
                    tracker.save(processingInfo, onSuccess = {
                        getNotification(processingInfo.processId).showSuccessNotification(
                            "Compression successful",
                            processingInfo.outputPath
                        )
                        insertIntoGallery(File(processingInfo.outputPath), "video/mp4")
                        exitFromService()
                    })

                }

                override fun onCancelled(
                    id: String,
                    trackTransformationInfos: MutableList<TrackTransformationInfo>?
                ) {

                    inProgress = false
                    "process cancelled".logAsInfo()
                    _status.postValue(ServiceState.Cancelled)
                }

                override fun onError(
                    id: String,
                    cause: Throwable?,
                    trackTransformationInfos: MutableList<TrackTransformationInfo>?
                ) {

                    inProgress = false
                    cause?.logError("process failed")
                    _status.postValue(ServiceState.Failed("Something went wrong"))
                }
            },
            null
        )
    }

    private fun insertIntoGallery(file: File, mimeType: String) {
        safeRun {
            if (!file.exists()) return@safeRun
            val uriSavedVideo: Uri?
            val resolver: ContentResolver = contentResolver
            val values = ContentValues()
            val relativePath = file.absolutePath.replace("/storage/emulated/0/", "")
            if (Build.VERSION.SDK_INT >= 29) {
                values.put(MediaStore.Video.Media.RELATIVE_PATH, relativePath)
                values.put(MediaStore.Video.Media.TITLE, file.name)
                values.put(MediaStore.Video.Media.DISPLAY_NAME, file.name)
                values.put(MediaStore.Video.Media.MIME_TYPE, mimeType)
                values.put(MediaStore.Video.Media.DATE_ADDED, System.currentTimeMillis() / 1000)
                val collection: Uri =
                    MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                uriSavedVideo = resolver.insert(collection, values)
            } else {
                values.put(MediaStore.Video.Media.TITLE, file.name)
                values.put(MediaStore.Video.Media.DISPLAY_NAME, file.name)
                values.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
                values.put(
                    MediaStore.Video.Media.DATE_ADDED,
                    System.currentTimeMillis() / 1000
                )
                values.put(MediaStore.Video.Media.DATA, file.absolutePath)
                uriSavedVideo = contentResolver.insert(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    values
                )
            }
            if (Build.VERSION.SDK_INT >= 29) {
                values.put(MediaStore.Video.Media.DATE_TAKEN, System.currentTimeMillis())
                values.put(MediaStore.Video.Media.IS_PENDING, 1)
            }

            if (Build.VERSION.SDK_INT >= 29) {
                values.clear()
                values.put(MediaStore.Video.Media.IS_PENDING, 0)
                contentResolver.update(uriSavedVideo!!, values, null, null)
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }
}