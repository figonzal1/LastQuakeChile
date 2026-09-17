package cl.figonzal.lastquakechile.core.domain

sealed class DomainResult<out T> {
    data class Success<out T>(val data: T) : DomainResult<T>()
    data class Error<out T>(val data: T, val error: DomainError) : DomainResult<T>()
}

/** Loggable summary of a paginated result without dumping the whole list into the log line. */
fun <T> DomainResult<List<T>>.describe(): String = when (this) {
    is DomainResult.Success -> "Success(${data.size} items)"
    is DomainResult.Error -> "Error($error, ${data.size} cached items)"
}
