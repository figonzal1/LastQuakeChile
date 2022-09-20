package cl.figonzal.lastquakechile.core.data.remote

/**
 * Project Sealed class for api response status
 *
 * PD: Put parameters with null let us to provide custom parameters on extensions classes
 */
sealed class StatusAPI<T>(
    val data: T? = null,
    val apiError: ApiError? = null
) {
    class Success<T>(data: T) : StatusAPI<T>(data)
    class Error<T>(data: T, apiError: ApiError) : StatusAPI<T>(data, apiError)
}

/**
 * Sealed class for api error response
 */
sealed class ApiError {
    object NoWifiError : ApiError()
    object IoError : ApiError()
    object HttpError : ApiError()
    object ServerError : ApiError()
    object UnknownError : ApiError()
    object TimeoutError : ApiError()
    object ResourceNotFound : ApiError()
}