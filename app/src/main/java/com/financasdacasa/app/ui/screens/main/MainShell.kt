package com.financasdacasa.app.ui.screens.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.financasdacasa.app.R
import com.financasdacasa.app.util.resolveServerUrl
import com.financasdacasa.app.ui.components.BottomNavBar
import com.financasdacasa.app.ui.components.BottomNavTab
import com.financasdacasa.app.ui.components.OfflineBanner
import com.financasdacasa.app.ui.screens.budgets.BudgetsScreen
import com.financasdacasa.app.ui.screens.categories.CategoriesScreen
import com.financasdacasa.app.ui.screens.garden.GardenScreen
import com.financasdacasa.app.ui.screens.garden.GoalDetailScreen
import com.financasdacasa.app.ui.screens.home.HomeScreen
import com.financasdacasa.app.ui.screens.members.MembersScreen
import com.financasdacasa.app.ui.screens.more.MoreScreen
import com.financasdacasa.app.ui.screens.dashboard.CategoryTransactionsScreen
import com.financasdacasa.app.ui.screens.dashboard.DashboardScreen
import com.financasdacasa.app.ui.screens.dashboard.SpendingByCategoryScreen
import com.financasdacasa.app.ui.screens.privacy.PrivacyDataScreen
import com.financasdacasa.app.ui.screens.recurring.RecurringScreen
import com.financasdacasa.app.ui.screens.subscription.SubscriptionScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainShell(
    onNavigateToHouseSelection: () -> Unit = {},
    viewModel: MainShellViewModel = hiltViewModel(),
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val mainRoutes = BottomNavTab.entries.map { it.route }
    val showBottomBar = currentRoute in mainRoutes
    val showTopBar = currentRoute in mainRoutes
    val isOffline by viewModel.isOffline.collectAsState()
    val user by viewModel.currentUser.collectAsState()
    val houseName by viewModel.houseName.collectAsState()

    Scaffold(
        topBar = {
            if (showTopBar) {
                Column {
                    if (isOffline) {
                        OfflineBanner()
                    }
                    CenterAlignedTopAppBar(
                        navigationIcon = {
                            Image(
                                painter = painterResource(R.mipmap.ic_launcher_foreground),
                                contentDescription = null,
                                modifier = Modifier
                                    .padding(start = 4.dp)
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                            )
                        },
                        title = {
                            Text(
                                text = houseName ?: "",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.clickable(onClick = onNavigateToHouseSelection),
                            )
                        },
                        actions = {
                            Box(
                                modifier = Modifier
                                    .padding(end = 12.dp)
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .border(
                                        width = 1.5.dp,
                                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                        shape = CircleShape,
                                    )
                                    .clickable {
                                        navController.navigate("more") {
                                            launchSingleTop = true
                                        }
                                    },
                                contentAlignment = Alignment.Center,
                            ) {
                                val avatarUrl = resolveServerUrl(user?.avatarUrl)
                                if (avatarUrl != null) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(LocalContext.current)
                                            .data(avatarUrl)
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = stringResource(R.string.more_title),
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape),
                                        contentScale = ContentScale.Crop,
                                    )
                                } else {
                                    val initial = (user?.name?.firstOrNull() ?: '?').uppercaseChar()
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primary),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Text(
                                            initial.toString(),
                                            color = MaterialTheme.colorScheme.onPrimary,
                                            fontSize = 14.sp,
                                        )
                                    }
                                }
                            }
                        },
                    )
                }
            }
        },
        bottomBar = {
            if (showBottomBar) {
                BottomNavBar(navController)
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = BottomNavTab.HOME.route,
            modifier = Modifier.padding(padding),
        ) {
            composable(BottomNavTab.HOME.route) {
                HomeScreen(
                    onGoalClick = { goalId ->
                        navController.navigate("garden/$goalId") {
                            launchSingleTop = true
                        }
                    },
                )
            }
            composable(BottomNavTab.BUDGETS.route) {
                BudgetsScreen()
            }
            composable(BottomNavTab.GARDEN.route) {
                GardenScreen(
                    onGoalClick = { goalId ->
                        navController.navigate("garden/$goalId") {
                            launchSingleTop = true
                        }
                    },
                )
            }
            composable(BottomNavTab.DASHBOARD.route) {
                DashboardScreen(
                    onCategoryClick = { categoryId ->
                        navController.navigate("category-transactions/$categoryId") {
                            launchSingleTop = true
                        }
                    },
                    onViewAllCategories = {
                        navController.navigate("spending-by-category") {
                            launchSingleTop = true
                        }
                    },
                )
            }
            composable("spending-by-category") {
                SpendingByCategoryScreen(
                    onBack = { navController.popBackStack() },
                    onCategoryClick = { categoryId ->
                        navController.navigate("category-transactions/$categoryId") {
                            launchSingleTop = true
                        }
                    },
                )
            }
            composable("category-transactions/{categoryId}") {
                CategoryTransactionsScreen(
                    onBack = { navController.popBackStack() },
                )
            }
            composable("garden/{goalId}") {
                GoalDetailScreen(
                    onBack = { navController.popBackStack() },
                )
            }
            composable("more") {
                MoreScreen(
                    onBack = { navController.popBackStack() },
                    onMembers = {
                        navController.navigate("members") { launchSingleTop = true }
                    },
                    onCategories = {
                        navController.navigate("categories") { launchSingleTop = true }
                    },
                    onRecurring = {
                        navController.navigate("recurring") { launchSingleTop = true }
                    },
                    onSubscription = {
                        navController.navigate("subscription") { launchSingleTop = true }
                    },
                    onPrivacy = {
                        navController.navigate("privacy-data") { launchSingleTop = true }
                    },
                )
            }
            composable("categories") {
                CategoriesScreen(onBack = { navController.popBackStack() })
            }
            composable("recurring") {
                RecurringScreen(onBack = { navController.popBackStack() })
            }
            composable("subscription") {
                SubscriptionScreen(onBack = { navController.popBackStack() })
            }
            composable("privacy-data") {
                PrivacyDataScreen(
                    onBack = { navController.popBackStack() },
                )
            }
            composable("members") {
                MembersScreen(
                    onBack = { navController.popBackStack() },
                    onNavigateToHouseSelection = onNavigateToHouseSelection,
                )
            }
        }
    }
}
