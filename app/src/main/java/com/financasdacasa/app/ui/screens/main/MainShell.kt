package com.financasdacasa.app.ui.screens.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Settings
import com.financasdacasa.app.R
import com.financasdacasa.app.ui.components.BottomNavBar
import com.financasdacasa.app.ui.components.BottomNavTab
import com.financasdacasa.app.ui.screens.budgets.BudgetsScreen
import com.financasdacasa.app.ui.screens.categories.CategoriesScreen
import com.financasdacasa.app.ui.screens.garden.GardenScreen
import com.financasdacasa.app.ui.screens.garden.GoalDetailScreen
import com.financasdacasa.app.ui.screens.home.HomeScreen
import com.financasdacasa.app.ui.screens.members.MembersScreen
import com.financasdacasa.app.ui.screens.more.MoreScreen
import com.financasdacasa.app.ui.screens.dashboard.CategoryTransactionsScreen
import com.financasdacasa.app.ui.screens.dashboard.DashboardScreen
import com.financasdacasa.app.ui.screens.recurring.RecurringScreen
import com.financasdacasa.app.ui.screens.subscription.SubscriptionScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainShell() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val mainRoutes = BottomNavTab.entries.map { it.route }
    val showBottomBar = currentRoute in mainRoutes
    val showTopBar = currentRoute in mainRoutes

    Scaffold(
        topBar = {
            if (showTopBar) {
                TopAppBar(
                    title = {},
                    actions = {
                        IconButton(onClick = {
                            navController.navigate("more") {
                                launchSingleTop = true
                            }
                        }) {
                            Icon(Lucide.Settings, contentDescription = stringResource(R.string.more_title))
                        }
                    },
                )
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
                HomeScreen()
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
            composable("members") {
                MembersScreen(
                    onBack = { navController.popBackStack() },
                    onNavigateToHouseSelection = {
                        navController.popBackStack(BottomNavTab.HOME.route, inclusive = true)
                    },
                )
            }
        }
    }
}
