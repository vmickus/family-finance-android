package com.financasdacasa.app.ui.screens.more

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Grid2x2
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Repeat
import com.composables.icons.lucide.ArrowLeft
import com.financasdacasa.app.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreScreen(
    onBack: () -> Unit,
    onCategories: () -> Unit,
    onRecurring: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.more_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Lucide.ArrowLeft, contentDescription = null)
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth().clickable { onCategories() },
                shape = MaterialTheme.shapes.medium,
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Icon(Lucide.Grid2x2, contentDescription = null)
                    Text(stringResource(R.string.categories), style = MaterialTheme.typography.bodyLarge)
                }
            }
            Surface(
                modifier = Modifier.fillMaxWidth().clickable { onRecurring() },
                shape = MaterialTheme.shapes.medium,
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Icon(Lucide.Repeat, contentDescription = null)
                    Text(stringResource(R.string.recurring_title), style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}
