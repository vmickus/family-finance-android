package com.financasdacasa.app.ui.screens.subscription

import android.app.Activity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.composables.icons.lucide.Check
import com.composables.icons.lucide.Lock
import com.composables.icons.lucide.Lucide
import com.financasdacasa.app.R

@Composable
fun PaywallScreen(
    viewModel: PaywallViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var selectedPlan by remember { mutableStateOf("monthly") }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.error) {
        val error = uiState.error ?: return@LaunchedEffect
        val message = when (error) {
            "VERIFICATION_FAILED" -> context.getString(R.string.paywall_error_verification)
            "ALREADY_OWNED" -> context.getString(R.string.paywall_error_already_owned)
            else -> context.getString(R.string.paywall_error_purchase)
        }
        snackbarHostState.showSnackbar(message)
        viewModel.clearError()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Spacer(Modifier.weight(1f))

            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = MaterialTheme.shapes.extraLarge,
                modifier = Modifier.size(56.dp),
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        Lucide.Lock,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(28.dp),
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Text(
                stringResource(R.string.paywall_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(8.dp))

            Text(
                stringResource(R.string.paywall_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(32.dp))

            if (uiState.isLoading) {
                CircularProgressIndicator()
            } else {
                val monthlyPrice = uiState.monthlyProduct?.formattedPrice ?: ""
                val annualPrice = uiState.annualProduct?.let { product ->
                    // Show monthly equivalent: extract currency from formatted price
                    val monthly = product.priceMicros / 12_000_000.0
                    val currencySymbol = product.formattedPrice.replace(Regex("[\\d.,\\s]"), "")
                    "$currencySymbol${String.format("%.2f", monthly)}"
                } ?: ""

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    PlanCard(
                        price = monthlyPrice,
                        period = stringResource(R.string.paywall_per_month),
                        badge = null,
                        selected = selectedPlan == "monthly",
                        onSelect = { selectedPlan = "monthly" },
                        modifier = Modifier.weight(1f),
                    )
                    PlanCard(
                        price = annualPrice,
                        period = stringResource(R.string.paywall_per_month),
                        badge = stringResource(R.string.paywall_annual_discount),
                        selected = selectedPlan == "annual",
                        onSelect = { selectedPlan = "annual" },
                        modifier = Modifier.weight(1f),
                    )
                }

                if (selectedPlan == "annual" && uiState.annualProduct != null) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        stringResource(R.string.paywall_annual_total_dynamic, uiState.annualProduct!!.formattedPrice),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }

                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = {
                        val activity = context as? Activity ?: return@Button
                        viewModel.purchase(activity, selectedPlan)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isPurchasing,
                ) {
                    if (uiState.isPurchasing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(stringResource(R.string.paywall_subscribe))
                }
            }

            Spacer(Modifier.height(8.dp))

            TextButton(onClick = { viewModel.retry() }) {
                Text(stringResource(R.string.paywall_check_again))
            }

            Spacer(Modifier.weight(1f))
        }
    }
}

@Composable
private fun PlanCard(
    price: String,
    period: String,
    badge: String?,
    selected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val borderColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant

    Card(
        modifier = modifier.clickable { onSelect() },
        border = BorderStroke(if (selected) 2.dp else 1.dp, borderColor),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            } else {
                MaterialTheme.colorScheme.surface
            },
        ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (badge != null) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = MaterialTheme.shapes.small,
                ) {
                    Text(
                        badge,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
                Spacer(Modifier.height(8.dp))
            }

            Text(
                price,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )

            Text(
                period,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            if (selected) {
                Spacer(Modifier.height(8.dp))
                Icon(
                    Lucide.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}
