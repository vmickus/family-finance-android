package com.financasdacasa.app.ui.screens.subscription

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
    val context = LocalContext.current
    val snackbarHostState = SnackbarHostState()
    val cancelledMessage = stringResource(R.string.subscription_cancelled_success)
    val cancelErrorMessage = stringResource(R.string.error_subscription_cancel_failed)
    val upgradeErrorMessage = stringResource(R.string.error_subscription_upgrade_failed)

    LaunchedEffect(uiState.cancelSuccess) {
        if (uiState.cancelSuccess) {
            snackbarHostState.showSnackbar(cancelledMessage)
            viewModel.clearCancelSuccess()
        }
    }

    LaunchedEffect(uiState.cancelError) {
        if (uiState.cancelError != null) {
            snackbarHostState.showSnackbar(cancelErrorMessage)
            viewModel.clearCancelError()
        }
    }

    LaunchedEffect(uiState.upgradeError) {
        if (uiState.upgradeError != null) {
            snackbarHostState.showSnackbar(upgradeErrorMessage)
            viewModel.clearUpgradeError()
        }
    }

    LaunchedEffect(uiState.upgradeCheckoutUrl) {
        uiState.upgradeCheckoutUrl?.let { url ->
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            viewModel.clearUpgradeCheckoutUrl()
        }
    }

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
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
                    cancelDialogVisible = uiState.cancelDialogVisible,
                    isCancelling = uiState.isCancelling,
                    isUpgrading = uiState.isUpgrading,
                    onManagePlayStore = {
                        context.startActivity(
                            Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://play.google.com/store/account/subscriptions"),
                            )
                        )
                    },
                    onShowCancelDialog = { viewModel.showCancelDialog() },
                    onDismissCancelDialog = { viewModel.dismissCancelDialog() },
                    onConfirmCancel = { viewModel.cancel() },
                    onUpgradeGooglePlay = {
                        (context as? Activity)?.let { activity ->
                            viewModel.upgradeViaGooglePlay(activity)
                        }
                    },
                    onUpgradeMercadoPago = { viewModel.upgradeViaMercadoPago() },
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
    cancelDialogVisible: Boolean,
    isCancelling: Boolean,
    isUpgrading: Boolean,
    onManagePlayStore: () -> Unit,
    onShowCancelDialog: () -> Unit,
    onDismissCancelDialog: () -> Unit,
    onConfirmCancel: () -> Unit,
    onUpgradeGooglePlay: () -> Unit,
    onUpgradeMercadoPago: () -> Unit,
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
            GooglePlayManagedCard(onManagePlayStore)
        }

        if (status.provider == "mercadopago") {
            MercadoPagoManagedCard(
                isCancelling = isCancelling,
                onCancel = onShowCancelDialog,
            )
        }

        if (status.plan == "monthly" && status.status == "active") {
            UpgradeCard(
                provider = status.provider,
                isUpgrading = isUpgrading,
                onUpgradeGooglePlay = onUpgradeGooglePlay,
                onUpgradeMercadoPago = onUpgradeMercadoPago,
            )
        }

        if (history.isNotEmpty()) {
            PaymentHistoryCard(history)
        }

        Spacer(Modifier.height(8.dp))
    }

    if (cancelDialogVisible) {
        CancelConfirmDialog(
            isCancelling = isCancelling,
            onDismiss = onDismissCancelDialog,
            onConfirm = onConfirmCancel,
        )
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
private fun GooglePlayManagedCard(onManagePlayStore: () -> Unit) {
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
            Spacer(Modifier.height(16.dp))
            OutlinedButton(onClick = onManagePlayStore) {
                Icon(Lucide.ExternalLink, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.subscription_manage_play_store))
            }
        }
    }
}

@Composable
private fun MercadoPagoManagedCard(
    isCancelling: Boolean,
    onCancel: () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = MaterialTheme.shapes.extraLarge,
                modifier = Modifier.size(48.dp),
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        Lucide.CreditCard,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            Text(
                stringResource(R.string.subscription_managed_mercadopago),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                stringResource(R.string.subscription_managed_mercadopago_description),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(16.dp))
            OutlinedButton(
                onClick = onCancel,
                enabled = !isCancelling,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error,
                ),
            ) {
                if (isCancelling) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.width(8.dp))
                }
                Text(stringResource(R.string.subscription_cancel))
            }
        }
    }
}

@Composable
private fun UpgradeCard(
    provider: String?,
    isUpgrading: Boolean,
    onUpgradeGooglePlay: () -> Unit,
    onUpgradeMercadoPago: () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Surface(
                color = MaterialTheme.colorScheme.tertiaryContainer,
                shape = MaterialTheme.shapes.extraLarge,
                modifier = Modifier.size(48.dp),
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        Lucide.TrendingUp,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onTertiaryContainer,
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            Text(
                stringResource(R.string.subscription_upgrade_annual),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                stringResource(R.string.subscription_upgrade_description),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = {
                    if (provider == "google_play") onUpgradeGooglePlay()
                    else onUpgradeMercadoPago()
                },
                enabled = !isUpgrading,
            ) {
                if (isUpgrading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                    Spacer(Modifier.width(8.dp))
                }
                Text(stringResource(R.string.subscription_upgrade_annual))
            }
        }
    }
}

@Composable
private fun CancelConfirmDialog(
    isCancelling: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = { if (!isCancelling) onDismiss() },
        title = { Text(stringResource(R.string.subscription_cancel_confirm_title)) },
        text = { Text(stringResource(R.string.subscription_cancel_confirm_description)) },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = !isCancelling,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error,
                ),
            ) {
                if (isCancelling) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.width(8.dp))
                }
                Text(stringResource(R.string.subscription_cancel))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isCancelling) {
                Text(stringResource(R.string.cancel))
            }
        },
    )
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
