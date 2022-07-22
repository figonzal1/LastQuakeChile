package cl.figonzal.lastquakechile.core.data.remote

/**
 * Project Sealed class for api response status
 */
sealed class StatusAPI<T>(
    val data: T? = null,
    val apiError: ApiError? = null
) {
    class Success<T>(data: T) : StatusAPI<T>(data)
    class Error<T>(apiError: ApiError, data: T? = null) : StatusAPI<T>(data, apiError)
}

/**
 * Sealed class for api error response
 */
sealed class ApiError {
    object IoError : ApiError()
    object HttpError : ApiError()
    object ServerError : ApiError()
    object UnknownError : ApiError()
    object TimeoutError : ApiError()
}