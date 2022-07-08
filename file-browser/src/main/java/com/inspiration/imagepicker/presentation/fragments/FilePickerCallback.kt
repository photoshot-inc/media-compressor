package com.inspiration.imagepicker.presentation.fragments

import com.inspiration.imagepicker.domain.models.FileModel

interface FilePickerCallback {
        fun onImageClicked(image: FileModel, requestCode: Int)
    }