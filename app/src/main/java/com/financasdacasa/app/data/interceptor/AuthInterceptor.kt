package com.financasdacasa.app.data.interceptor

import com.financasdacasa.app.data.local.TokenManager
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthInterceptor @Inject constructor(
    private val tokenManager: TokenManager,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val builder = original.newBuilder()
            .header("X-Platform", "android")

        tokenManager.getToken()?.let { token ->
            builder.header("Authorization", "Bearer $token")
        }

        tokenManager.getHouseId()?.let { houseId ->
            builder.header("X-House-ID", houseId)
        }

        return chain.proceed(builder.build())
    }
}
