package com.financasdacasa.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.financasdacasa.data.api.ApiService
import com.financasdacasa.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val apiService: ApiService,
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        val TOKEN_KEY = stringPreferencesKey("auth_token")
        val USER_ID_KEY = stringPreferencesKey("user_id")
        val USER_NAME_KEY = stringPreferencesKey("user_name")
        val USER_EMAIL_KEY = stringPreferencesKey("user_email")
        val FAMILY_ID_KEY = stringPreferencesKey("family_id")
    }

    val isLoggedIn: Flow<Boolean> = dataStore.data.map { prefs ->
        !prefs[TOKEN_KEY].isNullOrEmpty()
    }

    val currentUser: Flow<User?> = dataStore.data.map { prefs ->
        val id = prefs[USER_ID_KEY]
        val name = prefs[USER_NAME_KEY]
        val email = prefs[USER_EMAIL_KEY]
        if (id != null && name != null && email != null) {
            User(id, email, name, "")
        } else null
    }

    val selectedFamilyId: Flow<String?> = dataStore.data.map { prefs ->
        prefs[FAMILY_ID_KEY]
    }

    suspend fun login(email: String, password: String): Result<AuthResponse> {
        return try {
            val response = apiService.login(LoginRequest(email, password))
            if (response.isSuccessful) {
                response.body()?.let { authResponse ->
                    saveAuthData(authResponse)
                    Result.success(authResponse)
                } ?: Result.failure(Exception("Resposta vazia"))
            } else {
                Result.failure(Exception("Credenciais inválidas"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(email: String, password: String, name: String): Result<AuthResponse> {
        return try {
            val response = apiService.register(RegisterRequest(email, password, name))
            if (response.isSuccessful) {
                response.body()?.let { authResponse ->
                    saveAuthData(authResponse)
                    Result.success(authResponse)
                } ?: Result.failure(Exception("Resposta vazia"))
            } else {
                Result.failure(Exception("Erro ao criar conta"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun logout() {
        dataStore.edit { prefs ->
            prefs.remove(TOKEN_KEY)
            prefs.remove(USER_ID_KEY)
            prefs.remove(USER_NAME_KEY)
            prefs.remove(USER_EMAIL_KEY)
            prefs.remove(FAMILY_ID_KEY)
        }
    }

    suspend fun saveSelectedFamily(familyId: String) {
        dataStore.edit { prefs ->
            prefs[FAMILY_ID_KEY] = familyId
        }
    }

    private suspend fun saveAuthData(authResponse: AuthResponse) {
        dataStore.edit { prefs ->
            prefs[TOKEN_KEY] = authResponse.token
            prefs[USER_ID_KEY] = authResponse.user.id
            prefs[USER_NAME_KEY] = authResponse.user.name
            prefs[USER_EMAIL_KEY] = authResponse.user.email
        }
    }
}
