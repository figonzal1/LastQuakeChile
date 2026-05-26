package cl.figonzal.lastquakechile.core.data.remote

import cl.figonzal.lastquakechile.core.domain.DomainError
import com.skydoves.sandwich.StatusCode

internal fun StatusCode?.toDomainError(): DomainError = when (this) {
    StatusCode.NotFound -> DomainError.HttpError
    StatusCode.RequestTimeout -> DomainError.Timeout
    StatusCode.InternalServerError,
    StatusCode.ServiceUnavailable,
    StatusCode.Unknown -> DomainError.ServerError

    null -> DomainError.Unknown
    else -> DomainError.HttpError
}

internal fun String.toDomainError(): DomainError = when {
    contains("unable to resolve host", ignoreCase = true) ||
            contains("failed to connect", ignoreCase = true) ||
            contains("no address associated", ignoreCase = true) -> DomainError.NoConnection

    contains("timeout", ignoreCase = true) ||
            contains("10000ms") -> DomainError.Timeout

    else -> DomainError.Unknown
}
