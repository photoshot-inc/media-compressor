package videoeditor.compressor.video.features.compress

import com.google.gson.annotations.SerializedName
import videoeditor.compressor.video.models.VideoInfo

data class ProcessingInfo(
    @SerializedName("processId")
    val processId: Int,
    @SerializedName("width")
    val outputWidth: Int,
    @SerializedName("height")
    val outputHeight: Int,
    @SerializedName("bitrate")
    val outputBitrate: Long,
    @SerializedName("outputPath")
    val outputPath: String,
    @SerializedName("inputInfo")
    val inputVideoInfo: VideoInfo,
    @SerializedName("processStatus")
    var processStatus: ProcessStatus = ProcessStatus.IN_QUEUE
)