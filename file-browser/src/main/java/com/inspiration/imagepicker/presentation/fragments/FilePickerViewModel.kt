package com.inspiration.imagepicker.presentation.fragments

import android.app.Application
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.*
import androidx.savedstate.SavedStateRegistryOwner
import com.inspiration.imagepicker.domain.models.FileModel
import com.inspiration.imagepicker.domain.usecases.GetImagesUseCase
import com.inspiration.imagepicker.domain.usecases.GetVideosUseCase
import com.inspiration.imagepicker.frameworks.repositories.LocalMediaRepositoryImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import devs.core.ResultData

class SaveStateImagePickerVmFactory(
    private val arguments: Bundle?,
    private val owner: SavedStateRegistryOwner,
    private val context: Application
) : AbstractSavedStateViewModelFactory(owner, null) {
    override fun <T : ViewModel?> create(
        key: String,
        modelClass: Class<T>,
        state: SavedStateHandle
    ): T {
        arguments?.keySet()?.forEach {
            state.set(it, arguments.get(it))
        }
        return FilePickerViewModel(state, context) as T
    }

}

class FilePickerViewModel(savedStateHandle: SavedStateHandle, context: Application) :
    AndroidViewModel(context) {
    private val _files = MutableLiveData<ResultData<List<FileModel>>>(ResultData.Loading)
    val files: LiveData<ResultData<List<FileModel>>>
        get() = _files
    private val type = savedStateHandle.get<Int>(FilePicker.KEY_FILE_TYPE)
    private val imagePickerUseCase by lazy {
        GetImagesUseCase(
            LocalMediaRepositoryImpl(
                getApplication()
            )
        )
    }
    private val getVideosUseCase by lazy {
        GetVideosUseCase(
            LocalMediaRepositoryImpl(
                getApplication()
            )
        )
    }

    init {
        Log.d("TAGTAG", "imagePickerVm  $type")
        viewModelScope.launch(Dispatchers.IO) {
            if (type == FilePicker.TYPE_VIDEO || type == FilePicker.TYPE_IMAGE) {
                _files.postValue(ResultData.Loading)
                val data = if (type == FilePicker.TYPE_IMAGE) imagePickerUseCase.process(null)
                else getVideosUseCase.process(null)
                _files.postValue(data)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
    }
}