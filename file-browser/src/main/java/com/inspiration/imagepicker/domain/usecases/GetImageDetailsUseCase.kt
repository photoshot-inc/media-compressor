package com.inspiration.imagepicker.domain.usecases

import android.net.Uri
import com.inspiration.imagepicker.data.models.toFileModel
import com.inspiration.imagepicker.data.repositories.LocalMediaRepository
import com.inspiration.imagepicker.domain.models.FileModel
import devs.core.BaseUseCase
import devs.core.ResultData
import java.lang.Exception

class GetImageDetailsUseCase(private val repository: LocalMediaRepository) :
    BaseUseCase<Uri, ResultData<FileModel>> {

    override suspend fun process(params: Uri): ResultData<FileModel> {
        return try {
            ResultData.Success(repository.getImageDetails(params).toFileModel())
        } catch (ex: Exception) {
            ResultData.Error(ex)
        }
    }
}