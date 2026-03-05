package com.financasdacasa.app.ui.components

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.composables.icons.lucide.House
import com.composables.icons.lucide.LayoutDashboard
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Sprout
import com.composables.icons.lucide.Target
import com.financasdacasa.app.R

enum class BottomNavTab(val route: String, val labelRes: Int, val icon: ImageVector) {
    BUDGETS("budgets", R.string.nav_budgets, Lucide.Target),
    GARDEN("garden", R.string.nav_garden, Lucide.Sprout),
    HOME("home-tab", R.string.nav_home, Lucide.House),
    DASHBOARD("dashboard", R.string.nav_dashboard, Lucide.LayoutDashboard),
}

@Composable
fun BottomNavBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar {
        BottomNavTab.entries.forEach { tab ->
            NavigationBarItem(
                icon = { Icon(tab.icon, contentDescription = stringResource(tab.labelRes)) },
                label = { Text(stringResource(tab.labelRes)) },
                selected = currentRoute == tab.route,
                onClick = {
                    if (currentRoute != tab.route) {
                        navController.navigate(tab.route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
            )
        }
    }
}
