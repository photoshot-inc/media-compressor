package com.inspiration.imagepicker.domain.models

import java.io.Serializable

data class FileModel(val title: String, val uri: String, val path: String, val size: Long = 0):Serializable