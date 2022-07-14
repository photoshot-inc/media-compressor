package com.inspiration.imagepicker

import com.inspiration.imagepicker.presentation.fragments.ImagePickerFragment

object FilePicker {
    const val TYPE_IMAGE = 0
    const val TYPE_VIDEO = 1
    const val KEY_FILE_TYPE = "file.type"
    const val KEY_TITLE = "key.title"
    fun getFragment(type: Int, requestCode: Int): ImagePickerFragment {
        return ImagePickerFragment.newInstance(type, requestCode, "Select")
    }

    fun getVideoPicker(requestCode: Int): ImagePickerFragment {
        return ImagePickerFragment.newInstance(TYPE_VIDEO, requestCode, "Select Video")
    }
}