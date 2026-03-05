package com.financasdacasa.app.data.local

import com.financasdacasa.app.data.interceptor.AuthEvent
import com.financasdacasa.app.data.model.User
import com.squareup.moshi.Moshi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

sealed interface AuthState {
    data object Loading : AuthState
    data object Unauthenticated : AuthState
    data class Authenticated(val user: User) : AuthState
}

@Singleton
class SessionManager @Inject constructor(
    private val tokenManager: TokenManager,
    private val moshi: Moshi,
) {
    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val userAdapter = moshi.adapter(User::class.java)

    fun initialize() {
        val token = tokenManager.getToken()
        val userJson = tokenManager.getUserJson()
        if (token != null && userJson != null) {
            val user = runCatching { userAdapter.fromJson(userJson) }.getOrNull()
            if (user != null) {
                _authState.value = AuthState.Authenticated(user)
            } else {
                _authState.value = AuthState.Unauthenticated
            }
        } else {
            _authState.value = AuthState.Unauthenticated
        }

        AuthEvent.addListener { event ->
            when (event) {
                AuthEvent.TokenExpired -> {
                    tokenManager.clearAll()
                    _authState.value = AuthState.Unauthenticated
                }
                AuthEvent.EmailNotVerified -> { /* handled by nav graph checking emailVerified */ }
                AuthEvent.SubscriptionExpired -> { /* handled in Phase 5 */ }
            }
        }
    }

    fun login(token: String, user: User) {
        tokenManager.saveToken(token)
        tokenManager.saveUserJson(userAdapter.toJson(user))
        _authState.value = AuthState.Authenticated(user)
    }

    fun updateUser(user: User) {
        tokenManager.saveUserJson(userAdapter.toJson(user))
        _authState.value = AuthState.Authenticated(user)
    }

    fun selectHouse(houseId: String) {
        tokenManager.saveHouseId(houseId)
    }

    fun getSelectedHouseId(): String? = tokenManager.getHouseId()

    fun logout() {
        tokenManager.clearAll()
        _authState.value = AuthState.Unauthenticated
    }
}
