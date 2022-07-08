package com.inspiration.imagepicker.data.repositories

import android.net.Uri
import com.inspiration.imagepicker.data.models.FileDataModel

interface LocalMediaRepository {
    fun getAllImages(): List<FileDataModel>
    fun getAllVideos(): List<FileDataModel>
    fun getImageDetails(uri:Uri): FileDataModel
}