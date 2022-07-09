package videoeditor.compressor.video.service

import android.app.Service
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.linkedin.android.litr.MediaTransformer
import com.linkedin.android.litr.TransformationListener
import com.linkedin.android.litr.analytics.TrackTransformationInfo
import com.linkedin.android.litr.io.MediaExtractorMediaSource
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
        AppNotification(this, 1, notificationIntent)
    }

    private fun getNotification(id: Int): AppNotification {
        val notificationIntent: Intent =
            Intent(this, MainActivity::class.java)
//        notificationIntent.putExtra(BundleKeys.KEY_TAB_INDEX, (1).toInt())
        notificationIntent.flags =
            (Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        return AppNotification(this, id, notificationIntent)

    }

    @Inject
    lateinit var tracker: ProcessInfoTracker

    private val scope = CoroutineScope(Dispatchers.IO)
    private var compressor: MediaTransformer? = null
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

    private fun getOutputVideoFormat(processingInfo: ProcessingInfo): MediaFormat {
        val mimeType = MediaFormat.MIMETYPE_VIDEO_AVC
        var width = processingInfo.outputWidth
        var height = processingInfo.outputHeight
        var frameRate = 30
        var bitrate = processingInfo.boutputBitrate
        val colorFormat = MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface


        val format = MediaFormat.createVideoFormat(mimeType, width, height)
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT, colorFormat)
        format.setInteger(MediaFormat.KEY_BIT_RATE, bitrate)
//        format.setInteger(MediaFormat.KEY_FRAME_RATE, frameRate)
//        format.setInteger(MediaFormat.KEY_CAPTURE_RATE, frameRate)

//        format.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, width * height)
        format.setInteger(MediaFormat.KEY_MAX_WIDTH, width)
        format.setInteger(MediaFormat.KEY_MAX_HEIGHT, height)
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 0)
        return format
    }

    private fun compressVideo(processingInfo: ProcessingInfo) {
        if (inProgress) return
        inProgress = true
        _progress.postValue(-1)
        _status.postValue(ServiceState.Started)
        compressor = MediaTransformer(this)
        compressor?.addTag(0, processingInfo)
        val processId = processingInfo.processId.toString()
//        val inputSource =
//            MediaExtractorMediaSource(this, Uri.parse(processingInfo.inputVideoInfo.uri))
//        for (i in 0 until inputSource.trackCount) {
//            Log.d(TAG, "compressVideo: ${Gson().toJson(inputSource.getTrackFormat(i))}")
//        }
        val outputFormat: MediaFormat = getOutputVideoFormat(processingInfo)
        val listener = object : TransformationListener {
            override fun onStarted(id: String) {
                "process started".logAsInfo()
                _status.postValue(ServiceState.Started)
                processNotification.update(
                    title = processingInfo.inputVideoInfo.title,
                    "Compressing"
                )
            }

            override fun onProgress(id: String, progress: Float) {
                "progress $progress".logAsInfo()
                _progress.postValue((progress * 100).toInt())
                _status.postValue(ServiceState.Processing)
                processNotification.updateProgress((progress * 100).toInt())
            }

            override fun onCompleted(
                id: String,
                trackTransformationInfos: MutableList<TrackTransformationInfo>?
            ) {
                "process successful".logAsInfo()
                onFinishedProcess(
                    processingInfo,
                    ProcessStatus.SUCCESS,
                    ServiceState.Success(processingInfo)
                )
            }

            override fun onCancelled(
                id: String,
                trackTransformationInfos: MutableList<TrackTransformationInfo>?
            ) {
                "process cancelled".logAsInfo()
                onFinishedProcess(processingInfo, ProcessStatus.CANCELLED, ServiceState.Cancelled)

            }

            override fun onError(
                id: String,
                cause: Throwable?,
                trackTransformationInfos: MutableList<TrackTransformationInfo>?
            ) {
                cause?.logError("process failed")
                onFinishedProcess(
                    processingInfo,
                    ProcessStatus.FAILED,
                    ServiceState.Failed("Something went wrong")
                )
            }
        }
        compressor?.transform(
            processId,
            Uri.parse(processingInfo.inputVideoInfo.uri),
            processingInfo.outputPath,
            outputFormat,
            null,
            listener,
            null
        )
        /* compressor.transform(
             processId,
             Uri.parse(processingInfo.inputUri),
             processingInfo.outputPath,
             null,
             null,
             listener,
             null
         )*/
    }

    fun onFinishedProcess(
        processingInfo: ProcessingInfo,
        status: ProcessStatus,
        serviceState: ServiceState
    ) {

        inProgress = false
        _status.postValue(serviceState)
        processingInfo.processStatus = status
        tracker.save(processingInfo, onSuccess = {
            when (status) {
                ProcessStatus.IN_QUEUE -> {}
                ProcessStatus.IN_PROGRESS -> {}
                ProcessStatus.CANCELLED -> {
                    getNotification(processingInfo.processId).showSuccessNotification(
                        "Video Compressor",
                        "Cancelled by user"
                    )
                }
                ProcessStatus.SUCCESS -> {
                    getNotification(processingInfo.processId).showSuccessNotification(
                        "Compression successful",
                        "saved in: " + processingInfo.outputPath
                    )
                    insertIntoGallery(File(processingInfo.outputPath), "video/mp4")
                    exitFromService()
                }
                ProcessStatus.FAILED -> {
                    getNotification(processingInfo.processId).showSuccessNotification(
                        "Video Compressor",
                        "Video Compression failed"
                    )
                }
            }
            compressor?.removeTag(0);
            compressor?.release()
            compressor = null
            exitFromService()
        })
    }

    fun cancel() {
        if (compressor == null) return
        val info = (compressor?.getTag(0) as? ProcessingInfo) ?: return
        compressor?.cancel(info.processId.toString())
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