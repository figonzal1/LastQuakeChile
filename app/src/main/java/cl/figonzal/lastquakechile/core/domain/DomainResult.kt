package cl.figonzal.lastquakechile.core.domain

sealed class DomainResult<out T> {
    data class Success<out T>(val data: T) : DomainResult<T>()
    data class Error<out T>(val data: T, val error: DomainError) : DomainResult<T>()
}
