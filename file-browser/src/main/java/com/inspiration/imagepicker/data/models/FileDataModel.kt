package com.inspiration.imagepicker.data.models

import com.inspiration.imagepicker.domain.models.FileModel
import devs.core.utils.safeRun
import java.lang.Exception

data class FileDataModel(
    val uri: String,
    val title: String,
    val width: String,
    val height: String,
    val folderName: String,
    val orientation: String?,
    val size: String,
    val pathOrRelativePath: String
)

fun FileDataModel.toFileModel(): FileModel {

    return FileModel(title, uri, pathOrRelativePath, size = size.safeToLong())
}

fun String.safeToLong(): Long {
    return try {
        this.toLong()
    } catch (ex: Exception) {
        0
    }
}
