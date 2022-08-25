package cl.figonzal.lastquakechile.core.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Embedded wrapper for api resuts in
 */
@JsonClass(generateAdapter = true)
data class Embedded<T>(
    @Json(name = "_embedded")
    val embedded: T?
)