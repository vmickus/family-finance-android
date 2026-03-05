package com.financasdacasa.app.data.repository

import com.financasdacasa.app.data.api.AuthApi
import com.financasdacasa.app.data.model.AuthResponse
import com.financasdacasa.app.data.model.User
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val authApi: AuthApi,
) {
    suspend fun register(name: String, email: String, password: String, inviteToken: String? = null): AuthResponse {
        val body = buildMap {
            put("name", name)
            put("email", email)
            put("password", password)
            if (inviteToken != null) put("invite_token", inviteToken)
        }
        return authApi.register(body)
    }

    suspend fun login(email: String, password: String, inviteToken: String? = null): AuthResponse {
        val body = buildMap {
            put("email", email)
            put("password", password)
            if (inviteToken != null) put("invite_token", inviteToken)
        }
        return authApi.login(body)
    }

    suspend fun googleLogin(idToken: String): AuthResponse {
        return authApi.googleLogin(mapOf("id_token" to idToken))
    }

    suspend fun resendVerification() {
        authApi.resendVerification()
    }

    suspend fun getMe(): User {
        return authApi.getMe()
    }
}
