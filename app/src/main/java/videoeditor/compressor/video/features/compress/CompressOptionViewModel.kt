package videoeditor.compressor.video.features.compress

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Log
import androidx.lifecycle.*
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
    private val _selectedUris = MutableLiveData<List<Uri>>(listOf())
    val selectedFiles: LiveData<List<Uri>> get() = _selectedUris

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
        Log.d(TAG, "initialize: ${savedStateHandle.get<String>(IntentKeys.EXTRA_URI.str)}")
        val uri = savedStateHandle.get<String>(IntentKeys.EXTRA_URI.str)!!
        _selectedUris.postValue(listOf(Uri.parse(uri)))
        parseVideoInfo(uri)
    }

    private fun parseVideoInfo(uri: String) {
        val mediaMetadataRetriever = MediaMetadataRetriever()
        val descriptor = appInfo.context.contentResolver.openFileDescriptor(Uri.parse(uri), "r")
        mediaMetadataRetriever.setDataSource(descriptor!!.fileDescriptor)
        val width =
            mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
        val height =
            mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
        val bitrate =
            mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)
        val title =
            mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
        val info =
            VideoInfo(
                title ?: "unknown",
                width = width!!.toInt(),
                height!!.toInt(),
                bitrate = bitrate!!.toInt()
            )
        videoInfo.postValue(info)
    }

    fun compressVideo() {
        val outputDir = "/storage/emulated/0/download/video compressor"
        if (!File(outputDir).exists()) File(outputDir).mkdirs()
        val outputPath = "$outputDir/compressed_${System.currentTimeMillis()}.mp4"
        val processInfo =
            ProcessingInfo(
                System.currentTimeMillis().toInt(),
                _selectedUris.value!![0].toString(),
                100,
                100,
                100,
                outputPath
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

