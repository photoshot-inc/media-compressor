package videoeditor.compressor.video.features.compress

import com.google.gson.annotations.SerializedName

data class ProcessingInfo(
    @SerializedName("processId")
    val processId: Int,
    @SerializedName("inputUri")
    val inputUri: String,
    @SerializedName("width")
    val width: Int,
    @SerializedName("height")
    val height: Int,
    @SerializedName("bitrate")
    val bitrate: Int,
    @SerializedName("outputPath")
    val outputPath: String,
    @SerializedName("processStatus")
    var processStatus: ProcessStatus = ProcessStatus.IN_QUEUE
)