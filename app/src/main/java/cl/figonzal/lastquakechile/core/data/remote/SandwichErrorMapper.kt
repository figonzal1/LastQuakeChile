package cl.figonzal.lastquakechile.core.data.remote

import cl.figonzal.lastquakechile.core.domain.DomainError
import com.skydoves.sandwich.StatusCode
import timber.log.Timber

/** Wraps a failed API call so Crashlytics groups it under a distinct, greppable exception type. */
class ApiException(endpoint: String, statusCode: StatusCode?, detail: String?) :
    Exception("$endpoint failed [$statusCode]: $detail")

/**
 * Reports a failed API call at the level its cause deserves — a 4xx/5xx means our backend failed
 * and is worth a non-fatal in Crashlytics; anything else (no connection, timeout) is the user's
 * environment and would just flood the panel with nothing actionable, so it's a breadcrumb — and
 * returns the receiver unchanged so call sites can report and emit in one line.
 */
internal fun DomainError.logApiFailure(endpoint: String, statusCode: StatusCode?, detail: String?): DomainError {
    when (this) {
        DomainError.ServerError, DomainError.HttpError ->
            Timber.e(ApiException(endpoint, statusCode, detail), "API failure")

        else -> Timber.w("API failure ($this) on $endpoint: $detail")
    }
    return this
}

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
