package com.tempo.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.tempo.app.ui.screens.calendar.CalendarScreen
import com.tempo.app.ui.screens.insights.InsightsScreen
import com.tempo.app.ui.screens.settings.SettingsScreen
import com.tempo.app.ui.screens.today.TodayScreen

@Composable
fun TempoApp(navController: NavHostController = rememberNavController()) {
    Scaffold(
        bottomBar = { TempoBottomNavBar(navController) },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = TempoDestination.Today.route,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(TempoDestination.Today.route) { TodayScreen() }
            composable(TempoDestination.Calendar.route) { CalendarScreen() }
            composable(TempoDestination.Insights.route) { InsightsScreen() }
            composable(TempoDestination.Settings.route) { SettingsScreen() }
        }
    }
}

@Composable
private fun TempoBottomNavBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar {
        TempoDestination.entries.forEach { destination ->
            val selected = currentRoute == destination.route
            NavigationBarItem(
                selected = selected,
                onClick = {
                    navController.navigate(destination.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    Icon(
                        imageVector = if (selected) destination.selectedIcon else destination.unselectedIcon,
                        contentDescription = destination.label,
                    )
                },
                label = { Text(destination.label) },
            )
        }
    }
}
