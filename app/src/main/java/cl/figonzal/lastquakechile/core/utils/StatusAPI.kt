package cl.figonzal.lastquakechile.core.utils

sealed class StatusAPI<T>(
    val data: T? = null,
    val message: String? = null,
    val apiError: ApiError? = null
) {
    class Success<T>(data: T?) : StatusAPI<T>(data)
    class Error<T>(message: String, data: T? = null) : StatusAPI<T>(data, message)
    class Loading<T>(data: T? = null) : StatusAPI<T>(data)
}

sealed class NewStatusAPI<T>(
    val data: T? = null,
    val apiError: ApiError? = null
) {
    class Success<T>(data: T) : NewStatusAPI<T>(data)
    class Error<T>(data: T? = null, apiError: ApiError) : NewStatusAPI<T>(data, apiError)
}

sealed class ApiError {
    object IoError : ApiError()
    object HttpError : ApiError()
}