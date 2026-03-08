package com.financasdacasa.app.ui.screens.subscription

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.composables.icons.lucide.*
import com.financasdacasa.app.R
import com.financasdacasa.app.data.model.PaymentEvent
import com.financasdacasa.app.data.model.SubscriptionStatusResponse
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionScreen(
    onBack: () -> Unit,
    viewModel: SubscriptionViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.subscription_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Lucide.ArrowLeft, contentDescription = null)
                    }
                },
            )
        },
    ) { padding ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }
            uiState.error != null -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            stringResource(R.string.error_subscription_load_failed),
                            color = MaterialTheme.colorScheme.error,
                        )
                        Spacer(Modifier.height(8.dp))
                        OutlinedButton(onClick = { viewModel.loadData() }) {
                            Text(stringResource(R.string.subscription_retry))
                        }
                    }
                }
            }
            uiState.status != null -> {
                SubscriptionContent(
                    status = uiState.status!!,
                    history = uiState.history,
                    modifier = Modifier.padding(padding),
                )
            }
        }
    }
}

@Composable
private fun SubscriptionContent(
    status: SubscriptionStatusResponse,
    history: List<PaymentEvent>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        StatusCard(status)

        if (status.provider == "google_play") {
            GooglePlayManagedCard()
        }

        if (history.isNotEmpty()) {
            PaymentHistoryCard(history)
        }

        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun StatusCard(status: SubscriptionStatusResponse) {
    val (icon, containerColor, contentColor) = when (status.status) {
        "trialing" -> Triple(Lucide.Clock, MaterialTheme.colorScheme.tertiaryContainer, MaterialTheme.colorScheme.onTertiaryContainer)
        "active" -> Triple(Lucide.CircleCheck, MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.onPrimaryContainer)
        "past_due" -> Triple(Lucide.TriangleAlert, MaterialTheme.colorScheme.errorContainer, MaterialTheme.colorScheme.onErrorContainer)
        else -> Triple(Lucide.CircleX, MaterialTheme.colorScheme.errorContainer, MaterialTheme.colorScheme.onErrorContainer)
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                stringResource(R.string.subscription_current_plan),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(12.dp))

            Surface(
                color = containerColor,
                shape = MaterialTheme.shapes.medium,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Icon(icon, contentDescription = null, tint = contentColor)
                    Column {
                        Text(
                            statusLabel(status.status),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = contentColor,
                        )
                        if (status.status == "trialing") {
                            Text(
                                stringResource(R.string.subscription_trial_ends, status.trialDaysRemaining),
                                style = MaterialTheme.typography.bodySmall,
                                color = contentColor.copy(alpha = 0.8f),
                            )
                        }
                        if (status.currentPeriodEnd != null && status.status != "trialing") {
                            Text(
                                stringResource(R.string.subscription_period_end, formatDate(status.currentPeriodEnd)),
                                style = MaterialTheme.typography.bodySmall,
                                color = contentColor.copy(alpha = 0.8f),
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            Text(
                planLabel(status.plan),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun GooglePlayManagedCard() {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = MaterialTheme.shapes.extraLarge,
                modifier = Modifier.size(48.dp),
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        Lucide.Smartphone,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            Text(
                stringResource(R.string.subscription_managed_google_play),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                stringResource(R.string.subscription_managed_google_play_description),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun PaymentHistoryCard(history: List<PaymentEvent>) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                stringResource(R.string.subscription_history),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(12.dp))

            history.forEachIndexed { index, event ->
                if (index > 0) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text(
                            eventTypeLabel(event.eventType),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Text(
                            formatDateTime(event.createdAt),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    if (event.amount != null) {
                        Text(
                            formatCurrency(event.amount, event.currency),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun statusLabel(status: String): String = when (status) {
    "trialing" -> stringResource(R.string.subscription_status_trialing)
    "active" -> stringResource(R.string.subscription_status_active)
    "past_due" -> stringResource(R.string.subscription_status_past_due)
    "cancelled" -> stringResource(R.string.subscription_status_cancelled)
    "expired" -> stringResource(R.string.subscription_status_expired)
    else -> status
}

@Composable
private fun planLabel(plan: String): String = when (plan) {
    "trial" -> stringResource(R.string.subscription_plan_trial)
    "monthly" -> stringResource(R.string.subscription_plan_monthly)
    "annual" -> stringResource(R.string.subscription_plan_annual)
    else -> plan
}

@Composable
private fun eventTypeLabel(eventType: String): String = when (eventType) {
    "trial_started" -> stringResource(R.string.subscription_event_trial_started)
    "payment_received" -> stringResource(R.string.subscription_event_payment_received)
    "subscription_cancelled" -> stringResource(R.string.subscription_event_cancelled)
    "subscription_activated" -> stringResource(R.string.subscription_event_activated)
    "subscription_expired" -> stringResource(R.string.subscription_event_expired)
    "admin_granted" -> stringResource(R.string.subscription_event_admin_granted)
    "plan_changed" -> stringResource(R.string.subscription_event_plan_changed)
    "manual_payment_registered" -> stringResource(R.string.subscription_event_manual_payment)
    "payment_approved" -> stringResource(R.string.subscription_event_payment_approved)
    "google_play_verified" -> stringResource(R.string.subscription_event_google_play_verified)
    else -> eventType.replace("_", " ")
}

private fun formatDate(dateStr: String): String {
    return try {
        val date = LocalDate.parse(dateStr.take(10))
        date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT))
    } catch (_: Exception) {
        dateStr.take(10)
    }
}

private fun formatDateTime(dateStr: String): String {
    return try {
        val dt = ZonedDateTime.parse(dateStr)
        dt.toLocalDate().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT))
    } catch (_: Exception) {
        try {
            val dt = LocalDateTime.parse(dateStr.take(19))
            dt.toLocalDate().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT))
        } catch (_: Exception) {
            dateStr.take(10)
        }
    }
}

private fun formatCurrency(amount: String, currency: String): String {
    val symbol = when (currency) {
        "BRL" -> "R$"
        "USD" -> "$"
        "EUR" -> "\u20AC"
        else -> "$currency "
    }
    val formatted = try {
        java.math.BigDecimal(amount).setScale(2, java.math.RoundingMode.HALF_UP).toPlainString()
    } catch (_: Exception) {
        amount
    }
    return "$symbol$formatted"
}
