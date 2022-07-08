package devs.core

sealed class ResultData<out T> {
    object Loading : ResultData<Nothing>()
    data class Success<out T>(val data: T) : ResultData<T>()
    data class Error(val throwable: Throwable? = null, val message: String? = null) :
        ResultData<Nothing>()
}