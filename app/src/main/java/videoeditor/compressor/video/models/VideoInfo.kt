package videoeditor.compressor.video.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class VideoInfo(
    @SerializedName("title")
    val title: String,
    @SerializedName("uri")
    val uri: String,
    @SerializedName("width")
    val width: Int,
    @SerializedName("height")
    val height: Int,
    @SerializedName("bitrate")
    val bitrate: Long,
    @SerializedName("duration")
    val duration: Long,
    @SerializedName("size")
    val size: Long
) : Serializable