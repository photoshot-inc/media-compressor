package com.inspiration.imagepicker.data.models

import com.inspiration.imagepicker.domain.models.FileModel

data class FileDataModel(
    val uri:String,
    val title: String,
    val width: String,
    val height: String,
    val folderName: String,
    val orientation: String?,
    val size: String,
    val pathOrRelativePath: String
)
fun FileDataModel.toFileModel(): FileModel {
    return FileModel(title,uri,pathOrRelativePath)
}
