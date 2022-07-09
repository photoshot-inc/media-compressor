package videoeditor.compressor.video.features.compress

import android.media.MediaFormat
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.lifecycle.*
import com.inspiration.imagepicker.domain.models.FileModel
import com.linkedin.android.litr.MediaTransformer
import devs.core.OneTimeEvent
import devs.core.utils.safeRun
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import videoeditor.compressor.video.di.injector
import videoeditor.compressor.video.events.ActivityEvents
import videoeditor.compressor.video.events.broadcast
import videoeditor.compressor.video.models.AppInfo
import videoeditor.compressor.video.models.VideoInfo
import videoeditor.compressor.video.tasks.ProcessInfoTracker
import java.io.File
import javax.inject.Inject

object CompressScreenEventType {
    const val PROCESS_START_ERROR = 1
}

data class CompressProfileModel(
    val title: String,
    val message: String,
    val payload: Any,
    val resolution: Int = 100,
    val bitrate: Int = 100,
    val isPercent: Boolean = false,
    var selected: Boolean = false,
)

class CompressOptionViewModel(private val savedStateHandle: SavedStateHandle) : ViewModel() {
    companion object {
        private const val TAG = "CompressOptionViewModel"
    }

    private val _events = MutableLiveData<OneTimeEvent>()
    val events: LiveData<OneTimeEvent> get() = _events
    val videoInfo = MutableLiveData<VideoInfo?>(null)
    private val _selectedUris = MutableLiveData<List<VideoInfo>>(listOf())
    val selectedFiles: LiveData<List<VideoInfo>> get() = _selectedUris

    private var selectedProfile: CompressProfileModel? = null

    @Inject
    lateinit var appInfo: AppInfo

    @Inject
    lateinit var tracker: ProcessInfoTracker

    init {
        injector.inject(this)
        viewModelScope.launch(Dispatchers.IO) {
            safeRun {
                initialize(savedStateHandle)
            }
        }
    }

    private fun initialize(savedStateHandle: SavedStateHandle) {
        Log.d(TAG, "initialize: ${savedStateHandle.get<String>(IntentKeys.EXTRA_MODEL.str)}")
        val uri = savedStateHandle.get<FileModel>(IntentKeys.EXTRA_MODEL.str)!!
        parseVideoInfo(uri)
    }

    private fun parseVideoInfo(selectedFile: FileModel) {
        val mediaMetadataRetriever = MediaMetadataRetriever()
        val descriptor =
            appInfo.context.contentResolver.openFileDescriptor(Uri.parse(selectedFile.uri), "r")
        mediaMetadataRetriever.setDataSource(descriptor!!.fileDescriptor)
        val width =
            mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
        val height =
            mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
        val bitrate =
            mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)
        Log.d(TAG, "parseVideoInfo: $bitrate")
        val title =
            mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
        val duration =
            mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
        val rotation =
            mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)
        val fps =
            mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CAPTURE_FRAMERATE)
        Log.d(TAG, "parseVideoInfo: $fps")
        val info =
            VideoInfo(
                selectedFile.title,
                selectedFile.uri,
                width = width!!.toInt(), //if (false &&rotation != null && rotation.toInt() != 0) height!!.toInt() else
                height = height!!.toInt(),
                bitrate = (bitrate!!.toLong() * 0.125).toLong(),
                duration!!.toLong(),
                selectedFile.size
            )
        mediaMetadataRetriever.release()
        Log.d(TAG, "parseVideoInfo: $info")
        videoInfo.postValue(info)
        _selectedUris.postValue(listOf(info))
    }

    fun compressVideo(width: Int, height: Int, bitrate: Long) {
        val outputDir =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES).absolutePath + "/Media Compressor"
        if (!File(outputDir).exists()) File(outputDir).mkdirs()
        val outputPath = "$outputDir/compressed_${System.currentTimeMillis()}.mp4"
        val processInfo =
            ProcessingInfo(
                System.currentTimeMillis().toInt(),
                width,
                height,
                bitrate,
                outputPath,
                videoInfo.value!!
            )
        tracker.save(processInfo, onSuccess = {
            ActivityEvents.ShowProcessingScreenEvent.broadcast()
        }, onError = {
            _events.postValue(OneTimeEvent(CompressScreenEventType.PROCESS_START_ERROR, 0))
        })
    }

    fun onProfileUpdated(item: CompressProfileModel) {
        selectedProfile = item
    }
}

