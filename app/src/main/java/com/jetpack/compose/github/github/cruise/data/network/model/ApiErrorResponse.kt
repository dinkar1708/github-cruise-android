package com.jetpack.compose.github.github.cruise.data.network.model

import com.squareup.moshi.Json

/**
 * Created by Dinakar Maurya on 2024/05/14.
 */
data class ApiErrorResponse(
    @Json(name = "message") val message: String,
    @Json(name = "documentation_url") val documentationUrl: String
)
