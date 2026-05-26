package cl.figonzal.lastquakechile.core.domain

sealed class DomainError {
    data object NoConnection : DomainError()
    data object Timeout : DomainError()
    data object ServerError : DomainError()
    data object HttpError : DomainError()
    data object NoMoreData : DomainError()
    data object EmptyList : DomainError()
    data object Unknown : DomainError()
}
