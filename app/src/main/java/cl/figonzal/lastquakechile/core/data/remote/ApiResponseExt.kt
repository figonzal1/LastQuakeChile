package cl.figonzal.lastquakechile.core.data.remote

import com.skydoves.sandwich.ApiResponse

/**
 * Transforms the success body of an [ApiResponse] while preserving failure states unchanged.
 *
 * The cast on failure branches is safe because [ApiResponse.Failure] subtypes ([ApiResponse.Failure.Error]
 * and [ApiResponse.Failure.Exception]) carry no value of the success type T — they only hold
 * error/exception metadata. The type parameter is erased at runtime, so the cast never throws.
 *
 * Sandwich 2.x removed the top-level map operator; this extension restores that capability.
 */
@Suppress("UNCHECKED_CAST")
internal inline fun <T, V> ApiResponse<T>.mapSuccess(
    crossinline transform: T.() -> V
): ApiResponse<V> = when (this) {
    is ApiResponse.Success -> ApiResponse.Success(transform(data))
    else -> this as ApiResponse<V>
}
