package com.financasdacasa.app.data.local

import com.financasdacasa.app.data.interceptor.AuthEvent
import com.financasdacasa.app.data.model.User
import com.squareup.moshi.Moshi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
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
    private val database: FinancasDatabase,
) {
    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _subscriptionExpired = MutableStateFlow(false)
    val subscriptionExpired: StateFlow<Boolean> = _subscriptionExpired.asStateFlow()

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
                    CoroutineScope(Dispatchers.IO).launch { database.clearAllCaches() }
                    _authState.value = AuthState.Unauthenticated
                }
                AuthEvent.EmailNotVerified -> { /* handled by nav graph checking emailVerified */ }
                AuthEvent.SubscriptionExpired -> {
                    _subscriptionExpired.value = true
                }
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

    fun clearSubscriptionExpired() {
        _subscriptionExpired.value = false
    }

    fun logout() {
        tokenManager.clearAll()
        CoroutineScope(Dispatchers.IO).launch { database.clearAllCaches() }
        _subscriptionExpired.value = false
        _authState.value = AuthState.Unauthenticated
    }
}
