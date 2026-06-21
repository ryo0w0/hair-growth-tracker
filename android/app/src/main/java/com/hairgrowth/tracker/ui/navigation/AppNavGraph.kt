package com.hairgrowth.tracker.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.hairgrowth.tracker.ui.screens.DashboardScreen
import com.hairgrowth.tracker.ui.screens.HistoryScreen
import com.hairgrowth.tracker.ui.screens.RulerScreen
import com.hairgrowth.tracker.ui.screens.SettingsScreen

sealed class Screen(val route: String, val label: String) {
    object Dashboard : Screen("dashboard", "ホーム")
    object History : Screen("history", "履歴")
    object Ruler : Screen("ruler", "定規")
    object Settings : Screen("settings", "設定")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavGraph() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar {
                listOf(
                    Triple(Screen.Dashboard, Icons.Filled.Home, "ホーム"),
                    Triple(Screen.History, Icons.Filled.History, "履歴"),
                    Triple(Screen.Ruler, Icons.Filled.Straighten, "定規"),
                    Triple(Screen.Settings, Icons.Filled.Settings, "設定")
                ).forEach { (screen, icon, label) ->
                    NavigationBarItem(
                        icon = { Icon(icon, contentDescription = label) },
                        label = { Text(label) },
                        selected = currentRoute == screen.route,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(Screen.Dashboard.route) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Dashboard.route) { DashboardScreen() }
            composable(Screen.History.route) { HistoryScreen() }
            composable(Screen.Ruler.route) { RulerScreen() }
            composable(Screen.Settings.route) { SettingsScreen() }
        }
    }
}
