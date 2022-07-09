package videoeditor.compressor.video.models

import java.io.Serializable

data class VideoInfo(
    val title: String,
    val uri:String,
    val width: Int,
    val height: Int,
    val bitrate: Int
) : Serializable