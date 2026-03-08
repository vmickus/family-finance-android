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
import com.financasdacasa.app.ui.navigation.InviteNavGraph
import com.financasdacasa.app.ui.navigation.MainNavGraph
import com.financasdacasa.app.ui.screens.auth.VerifyEmailScreen
import com.financasdacasa.app.ui.screens.subscription.PaywallScreen
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

        val inviteToken = intent?.data?.let { uri ->
            if (uri.pathSegments.size >= 2 && uri.pathSegments[0] == "invite") {
                uri.pathSegments[1]
            } else null
        }

        setContent {
            FinancasTheme {
                val authState by sessionManager.authState.collectAsState()
                val subscriptionExpired by sessionManager.subscriptionExpired.collectAsState()

                when (val state = authState) {
                    is AuthState.Loading -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                    is AuthState.Unauthenticated -> {
                        if (inviteToken != null) {
                            InviteNavGraph(token = inviteToken)
                        } else {
                            AuthNavGraph()
                        }
                    }
                    is AuthState.Authenticated -> {
                        if (!state.user.emailVerified) {
                            VerifyEmailScreen()
                        } else if (subscriptionExpired) {
                            PaywallScreen(
                                onSubscribe = { /* Google Play Billing in MICK-3 */ },
                                onRetry = { sessionManager.clearSubscriptionExpired() },
                            )
                        } else if (inviteToken != null) {
                            InviteNavGraph(token = inviteToken)
                        } else {
                            MainNavGraph()
                        }
                    }
                }
            }
        }
    }
}
