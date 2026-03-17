package com.financasdacasa.app.data.repository

import com.financasdacasa.app.data.api.AuthApi
import com.financasdacasa.app.data.model.AuthResponse
import com.financasdacasa.app.data.model.AvatarUploadResponse
import com.financasdacasa.app.data.model.DeactivationCheckResponse
import com.financasdacasa.app.data.model.User
import okhttp3.MultipartBody
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

    suspend fun uploadAvatar(part: MultipartBody.Part): AvatarUploadResponse {
        return authApi.uploadAvatar(part)
    }

    suspend fun deleteAvatar() {
        authApi.deleteAvatar()
    }

    suspend fun requestExport() {
        authApi.requestExport()
    }

    suspend fun deactivationCheck(): DeactivationCheckResponse {
        return authApi.deactivationCheck()
    }

    suspend fun deactivateAccount(password: String?) {
        val body = buildMap {
            if (password != null) put("password", password)
        }
        authApi.deactivate(body)
    }
}
