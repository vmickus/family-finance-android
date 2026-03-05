package com.financasdacasa.app.data.interceptor

import com.financasdacasa.app.data.local.TokenManager
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ResponseInterceptor @Inject constructor(
    private val tokenManager: TokenManager,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())

        when (response.code) {
            401 -> {
                tokenManager.clearAll()
                AuthEvent.emit(AuthEvent.TokenExpired)
            }
            403 -> {
                val body = response.peekBody(1024).string()
                if (body.contains("EMAIL_NOT_VERIFIED")) {
                    AuthEvent.emit(AuthEvent.EmailNotVerified)
                }
            }
            402 -> {
                val body = response.peekBody(1024).string()
                if (!body.contains("HOUSE_CREATION_BLOCKED")) {
                    AuthEvent.emit(AuthEvent.SubscriptionExpired)
                }
            }
        }

        return response
    }
}

object AuthEvent {
    sealed interface Event
    data object TokenExpired : Event
    data object EmailNotVerified : Event
    data object SubscriptionExpired : Event

    private val listeners = mutableListOf<(Event) -> Unit>()

    fun addListener(listener: (Event) -> Unit) {
        listeners.add(listener)
    }

    fun removeListener(listener: (Event) -> Unit) {
        listeners.remove(listener)
    }

    fun emit(event: Event) {
        listeners.forEach { it(event) }
    }
}
