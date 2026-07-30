package com.tempo.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.tempo.app.ui.components.FloatingBottomNav
import com.tempo.app.ui.components.FloatingNavItem
import com.tempo.app.ui.screens.calendar.CalendarScreen
import com.tempo.app.ui.screens.habit.AddEditHabitScreen
import com.tempo.app.ui.screens.habit.HabitDetailScreen
import com.tempo.app.ui.screens.insights.InsightsScreen
import com.tempo.app.ui.screens.routine.AddEditRoutineScreen
import com.tempo.app.ui.screens.settings.SettingsScreen
import com.tempo.app.ui.screens.today.TodayScreen

private const val HABIT_ID_ARG = "habitId"
private const val ROUTINE_ID_ARG = "routineId"
private const val ROUTE_ADD_EDIT_HABIT = "habit/edit/{$HABIT_ID_ARG}?$ROUTINE_ID_ARG={$ROUTINE_ID_ARG}"
private const val ROUTE_HABIT_DETAIL = "habit/detail/{$HABIT_ID_ARG}"
private const val ROUTE_ADD_EDIT_ROUTINE = "routine/edit/{$ROUTINE_ID_ARG}"

private fun editHabitRoute(habitId: Long) = "habit/edit/$habitId"
private fun addHabitRoute() = "habit/edit/0"
private fun habitDetailRoute(habitId: Long) = "habit/detail/$habitId"
private fun editRoutineRoute(routineId: Long) = "routine/edit/$routineId"
private fun addRoutineRoute() = "routine/edit/0"

@Composable
fun TempoApp(navController: NavHostController = rememberNavController()) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = TempoDestination.entries.any { it.route == currentRoute }

    Scaffold(
        bottomBar = { if (showBottomBar) TempoBottomNavBar(navController) },
        containerColor = Color.Transparent,
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = TempoDestination.Today.route,
            modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding()),
        ) {
            composable(TempoDestination.Today.route) {
                TodayScreen(
                    onAddHabit = { navController.navigate(addHabitRoute()) },
                    onAddRoutine = { navController.navigate(addRoutineRoute()) },
                    onOpenHabit = { habitId -> navController.navigate(habitDetailRoute(habitId)) },
                    onOpenRoutine = { routineId -> navController.navigate(editRoutineRoute(routineId)) },
                )
            }
            composable(TempoDestination.Calendar.route) { CalendarScreen() }
            composable(TempoDestination.Insights.route) { InsightsScreen() }
            composable(TempoDestination.Settings.route) { SettingsScreen() }
            composable(
                route = ROUTE_ADD_EDIT_HABIT,
                arguments = listOf(
                    navArgument(HABIT_ID_ARG) { type = NavType.LongType; defaultValue = 0L },
                    navArgument(ROUTINE_ID_ARG) { type = NavType.LongType; defaultValue = -1L },
                ),
            ) {
                AddEditHabitScreen(onDone = { navController.popBackStack() })
            }
            composable(
                route = ROUTE_HABIT_DETAIL,
                arguments = listOf(navArgument(HABIT_ID_ARG) { type = NavType.LongType }),
            ) {
                HabitDetailScreen(
                    onBack = { navController.popBackStack() },
                    onEdit = { habitId -> navController.navigate(editHabitRoute(habitId)) },
                )
            }
            composable(
                route = ROUTE_ADD_EDIT_ROUTINE,
                arguments = listOf(navArgument(ROUTINE_ID_ARG) { type = NavType.LongType; defaultValue = 0L }),
            ) {
                AddEditRoutineScreen(
                    onDone = { navController.popBackStack() },
                    onAddHabitToRoutine = { routineId -> navController.navigate("habit/edit/0?routineId=$routineId") },
                )
            }
        }
    }
}

@Composable
private fun TempoBottomNavBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val items = TempoDestination.entries.map { destination ->
        val selected = currentRoute == destination.route
        FloatingNavItem(
            label = destination.label,
            icon = if (selected) destination.selectedIcon else destination.unselectedIcon,
            selected = selected,
            onClick = {
                navController.navigate(destination.route) {
                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            },
        )
    }
    FloatingBottomNav(items = items)
}
