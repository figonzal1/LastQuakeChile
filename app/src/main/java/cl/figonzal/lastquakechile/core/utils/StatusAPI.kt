package cl.figonzal.lastquakechile.core.utils

sealed class StatusAPI<T>(val data: T? = null, val message: String? = null) {
    class Success<T>(data: T?) : StatusAPI<T>(data)
    class Error<T>(message: String, data: T? = null) : StatusAPI<T>(data, message)
    class Loading<T>(data: T? = null) : StatusAPI<T>(data)
}