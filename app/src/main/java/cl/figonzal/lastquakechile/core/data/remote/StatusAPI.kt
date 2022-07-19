package cl.figonzal.lastquakechile.core.data.remote

@Deprecated("Use new StatusAPI")
sealed class StatusAPI<T>(
    val data: T? = null,
    val message: String? = null,
    val apiError: ApiError? = null
) {
    class Success<T>(data: T?) : StatusAPI<T>(data)
    class Error<T>(message: String, data: T? = null) : StatusAPI<T>(data, message)
    class Loading<T>(data: T? = null) : StatusAPI<T>(data)
}

/**
 * Sealed class for api response status
 */
sealed class NewStatusAPI<T>(
    val data: T? = null,
    val apiError: ApiError? = null
) {
    class Success<T>(data: T) : NewStatusAPI<T>(data)
    class Error<T>(apiError: ApiError, data: T? = null) : NewStatusAPI<T>(data, apiError)
}

/**
 * Sealed class for api error response
 */
sealed class ApiError {
    object IoError : ApiError()
    object HttpError : ApiError()
}