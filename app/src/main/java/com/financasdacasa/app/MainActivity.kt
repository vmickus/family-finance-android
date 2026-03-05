package com.financasdacasa.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.financasdacasa.app.data.local.AuthState
import com.financasdacasa.app.data.local.SessionManager
import com.financasdacasa.app.ui.navigation.AuthNavGraph
import com.financasdacasa.app.ui.navigation.MainNavGraph
import com.financasdacasa.app.ui.screens.auth.VerifyEmailScreen
import com.financasdacasa.app.ui.theme.FinancasTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sessionManager.initialize()
        enableEdgeToEdge()
        setContent {
            FinancasTheme {
                val authState by sessionManager.authState.collectAsState()

                when (val state = authState) {
                    is AuthState.Loading -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                    is AuthState.Unauthenticated -> {
                        AuthNavGraph()
                    }
                    is AuthState.Authenticated -> {
                        if (!state.user.emailVerified) {
                            VerifyEmailScreen()
                        } else {
                            MainNavGraph()
                        }
                    }
                }
            }
        }
    }
}
