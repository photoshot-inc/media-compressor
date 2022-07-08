package com.inspiration.imagepicker.domain.usecases



import devs.core.BaseUseCase
import devs.core.ResultData
import com.inspiration.imagepicker.data.repositories.LocalMediaRepository
import com.inspiration.imagepicker.data.models.toFileModel
import com.inspiration.imagepicker.domain.models.FileModel
import java.lang.Exception

class GetImagesUseCase(private val repository: LocalMediaRepository) :
    BaseUseCase<Any?, ResultData<List<FileModel>>> {

    override suspend fun process(params: Any?): ResultData<List<FileModel>> {
        return try {
            ResultData.Success(repository.getAllImages().map { it.toFileModel() })
        } catch (ex: Exception) {
            ResultData.Error(ex)
        }
    }
}