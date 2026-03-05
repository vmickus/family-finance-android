package com.financasdacasa.app.util

import com.financasdacasa.app.data.model.ApiError
import com.squareup.moshi.Moshi
import retrofit2.HttpException

fun HttpException.apiError(): ApiError? {
    val body = response()?.errorBody()?.string() ?: return null
    return runCatching {
        Moshi.Builder().build().adapter(ApiError::class.java).fromJson(body)
    }.getOrNull()
}

fun HttpException.apiErrorCode(): String? = apiError()?.code
