package com.inspiration.imagepicker.presentation.fragments

import android.app.Application
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.*
import androidx.savedstate.SavedStateRegistryOwner
import com.inspiration.imagepicker.FilePicker
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

data class FolderModel(val title: String, val path: String?, var selected: Boolean)
class FilePickerViewModel(savedStateHandle: SavedStateHandle, context: Application) :
    AndroidViewModel(context) {
    private val _files = MutableLiveData<ResultData<List<FileModel>>>(ResultData.Loading)
    val files: LiveData<ResultData<List<FileModel>>>
        get() = _files
    val allFiles = mutableListOf<FileModel>()
    private val _folders = MutableLiveData<List<FolderModel>>()
    val folders: LiveData<List<FolderModel>> get() = _folders
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
                if (data is ResultData.Success) {
                    allFiles.addAll(data.data)
                    val fs = allFiles
                        .map {
                            val path = it.path.substring(0, it.path.lastIndexOf("/"))
                            FolderModel(
                                path.split("/").lastOrNull() ?: "Root",
                                path,
                                false
                            )
                        }.distinctBy { it.path }.toMutableList()
                    fs.add(0,FolderModel("All Files", null, true))
                    _folders.postValue(fs)
                }
                _files.postValue(data)
            }
        }
    }

    fun getFolderName(path: String) {

    }

    fun loadFiles(folderPath: String?) {
        if (_files.value is ResultData.Success) {
            _files.postValue(ResultData.Success(allFiles.filter {
                folderPath == null || it.path.startsWith(folderPath)
            }.toList()))
        }

    }

    override fun onCleared() {
        super.onCleared()
    }
}