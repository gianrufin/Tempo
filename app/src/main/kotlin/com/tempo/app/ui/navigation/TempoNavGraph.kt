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
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.tempo.app.ui.screens.calendar.CalendarScreen
import com.tempo.app.ui.screens.habit.AddEditHabitScreen
import com.tempo.app.ui.screens.insights.InsightsScreen
import com.tempo.app.ui.screens.settings.SettingsScreen
import com.tempo.app.ui.screens.today.TodayScreen

private const val HABIT_ID_ARG = "habitId"
private const val ROUTE_ADD_EDIT_HABIT = "habit/{$HABIT_ID_ARG}"

private fun editHabitRoute(habitId: Long) = "habit/$habitId"
private fun addHabitRoute() = "habit/0"

@Composable
fun TempoApp(navController: NavHostController = rememberNavController()) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = TempoDestination.entries.any { it.route == currentRoute }

    Scaffold(
        bottomBar = { if (showBottomBar) TempoBottomNavBar(navController) },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = TempoDestination.Today.route,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(TempoDestination.Today.route) {
                TodayScreen(
                    onAddHabit = { navController.navigate(addHabitRoute()) },
                    onEditHabit = { habitId -> navController.navigate(editHabitRoute(habitId)) },
                )
            }
            composable(TempoDestination.Calendar.route) { CalendarScreen() }
            composable(TempoDestination.Insights.route) { InsightsScreen() }
            composable(TempoDestination.Settings.route) { SettingsScreen() }
            composable(
                route = ROUTE_ADD_EDIT_HABIT,
                arguments = listOf(navArgument(HABIT_ID_ARG) { type = NavType.LongType; defaultValue = 0L }),
            ) {
                AddEditHabitScreen(onDone = { navController.popBackStack() })
            }
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
