package com.ngonim.autokeep.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ngonim.autokeep.data.AutoKeepRepository
import com.ngonim.autokeep.data.UserPreferences
import com.ngonim.autokeep.ui.components.AutoKeepBottomBar
import com.ngonim.autokeep.ui.dashboard.DashboardViewModel
import com.ngonim.autokeep.ui.expenses.ExpensesScreen
import com.ngonim.autokeep.ui.expenses.ExpensesViewModel
import com.ngonim.autokeep.ui.garage.GarageScreen
import com.ngonim.autokeep.ui.garage.GarageViewModel
import com.ngonim.autokeep.ui.home.HomeScreen
import com.ngonim.autokeep.ui.home.HomeViewModel
import com.ngonim.autokeep.ui.maintenance.MaintenanceDetailScreen
import com.ngonim.autokeep.ui.maintenance.MaintenanceDetailViewModel
import com.ngonim.autokeep.ui.more.MoreScreen
import com.ngonim.autokeep.ui.navigation.MainTab
import com.ngonim.autokeep.ui.navigation.Routes
import com.ngonim.autokeep.ui.onboarding.OnboardingScreen
import com.ngonim.autokeep.ui.reminders.RemindersScreen
import com.ngonim.autokeep.ui.settings.SettingsScreen
import com.ngonim.autokeep.ui.service.RecordServiceScreen
import com.ngonim.autokeep.ui.service.RecordServiceViewModel
import com.ngonim.autokeep.ui.service.ServiceHistoryScreen
import com.ngonim.autokeep.ui.service.ServiceHistoryViewModel
import com.ngonim.autokeep.ui.vehicle.AddVehicleScreen
import com.ngonim.autokeep.ui.vehicle.AddVehicleViewModel
import com.ngonim.autokeep.ui.vehicle.VehicleDetailsScreen
import kotlinx.coroutines.flow.first

@Composable
fun AutoKeepApp(
    repository: AutoKeepRepository,
    userPreferences: UserPreferences,
) {
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val route = backStack?.destination?.route
    val showBar = route?.startsWith("main") == true
    val vehicles by repository.observeVehicles().collectAsStateWithLifecycle(emptyList())
    val primaryId = vehicles.firstOrNull()?.id
    val selectedTab = runCatching {
        backStack?.arguments?.getString("tab")?.let { MainTab.valueOf(it) }
    }.getOrNull() ?: MainTab.HOME

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (showBar) {
                AutoKeepBottomBar(
                    selected = selectedTab,
                    onTab = { navController.navigate(Routes.main(it)) { launchSingleTop = true } },
                    onAdd = {
                        if (primaryId != null) {
                            navController.navigate(Routes.record(primaryId))
                        } else {
                            navController.navigate(Routes.addVehicle(Routes.FROM_GARAGE))
                        }
                    },
                )
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.START,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(Routes.START) {
                LaunchedEffect(Unit) {
                    val list = repository.observeVehicles().first()
                    val destination = if (list.isEmpty()) Routes.ONBOARDING else Routes.main(MainTab.HOME)
                    navController.navigate(destination) {
                        popUpTo(Routes.START) { inclusive = true }
                    }
                }
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            composable(Routes.ONBOARDING) {
                OnboardingScreen(
                    onGetStarted = { navController.navigate(Routes.addVehicle(Routes.FROM_ONBOARDING)) },
                    onAlreadyHaveAccount = {
                        if (primaryId == null) {
                            navController.navigate(Routes.addVehicle(Routes.FROM_ONBOARDING))
                        } else {
                            navController.navigate(Routes.main(MainTab.HOME)) {
                                popUpTo(Routes.ONBOARDING) { inclusive = true }
                            }
                        }
                    },
                )
            }
            composable(
                route = Routes.ADD_VEHICLE,
                arguments = listOf(navArgument("from") { type = NavType.StringType; defaultValue = Routes.FROM_GARAGE }),
            ) { entry ->
                val from = entry.arguments?.getString("from") ?: Routes.FROM_GARAGE
                val viewModel = viewModel<AddVehicleViewModel>(
                    factory = viewModelFactory { AddVehicleViewModel(repository) },
                )
                AddVehicleScreen(
                    viewModel = viewModel,
                    onBack = if (from == Routes.FROM_ONBOARDING) null else ({ navController.popBackStack() }),
                    onSaved = {
                        navController.navigate(Routes.main(MainTab.HOME)) {
                            popUpTo(Routes.ONBOARDING) { inclusive = true }
                        }
                    },
                )
            }
            composable(
                route = Routes.MAIN,
                arguments = listOf(navArgument("tab") { type = NavType.StringType }),
            ) { entry ->
                val tab = runCatching { MainTab.valueOf(entry.arguments?.getString("tab") ?: "HOME") }.getOrDefault(MainTab.HOME)
                when (tab) {
                    MainTab.HOME -> {
                        val viewModel = viewModel<HomeViewModel>(
                            factory = viewModelFactory { HomeViewModel(repository, userPreferences) },
                        )
                        HomeScreen(
                            viewModel = viewModel,
                            onVehicle = { navController.navigate(Routes.vehicleDetails(it)) },
                            onReminders = { navController.navigate(Routes.reminders(it)) },
                            onItem = { vehicleId, itemId -> navController.navigate(Routes.maintenanceDetail(vehicleId, itemId)) },
                            onAddVehicle = { navController.navigate(Routes.addVehicle(Routes.FROM_GARAGE)) },
                        )
                    }
                    MainTab.VEHICLES -> {
                        val viewModel = viewModel<GarageViewModel>(
                            factory = viewModelFactory { GarageViewModel(repository) },
                        )
                        GarageScreen(
                            viewModel = viewModel,
                            onVehicle = { navController.navigate(Routes.vehicleDetails(it)) },
                            onAddVehicle = { navController.navigate(Routes.addVehicle(Routes.FROM_GARAGE)) },
                        )
                    }
                    MainTab.HISTORY -> {
                        if (primaryId == null) {
                            Text("Add a vehicle to see history.", modifier = Modifier.padding(24.dp))
                        } else {
                            val viewModel = viewModel<ServiceHistoryViewModel>(
                                key = "history-tab-$primaryId",
                                factory = viewModelFactory { ServiceHistoryViewModel(primaryId, repository) },
                            )
                            ServiceHistoryScreen(viewModel = viewModel, embedded = true)
                        }
                    }
                    MainTab.MORE -> {
                        MoreScreen(
                            userPreferences = userPreferences,
                            onVehicles = { navController.navigate(Routes.main(MainTab.VEHICLES)) },
                            onReminders = { primaryId?.let { navController.navigate(Routes.reminders(it)) } },
                            onHistory = { navController.navigate(Routes.main(MainTab.HISTORY)) },
                            onExpenses = { primaryId?.let { navController.navigate(Routes.expenses(it)) } },
                            onSettings = { navController.navigate(Routes.SETTINGS) },
                        )
                    }
                }
            }
            composable(
                route = Routes.VEHICLE_DETAILS,
                arguments = listOf(navArgument("vehicleId") { type = NavType.LongType }),
            ) { entry ->
                val vehicleId = entry.arguments?.getLong("vehicleId") ?: return@composable
                val viewModel = viewModel<DashboardViewModel>(
                    key = "details-$vehicleId",
                    factory = viewModelFactory { DashboardViewModel(vehicleId, repository) },
                )
                VehicleDetailsScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onAddService = { navController.navigate(Routes.record(vehicleId)) },
                    onAddExpense = { navController.navigate(Routes.record(vehicleId, mode = Routes.MODE_EXPENSE)) },
                    onItem = { navController.navigate(Routes.maintenanceDetail(vehicleId, it)) },
                    onDeleted = { navController.popBackStack() },
                )
            }
            composable(
                route = Routes.MAINTENANCE_DETAIL,
                arguments = listOf(
                    navArgument("vehicleId") { type = NavType.LongType },
                    navArgument("itemId") { type = NavType.LongType },
                ),
            ) { entry ->
                val vehicleId = entry.arguments?.getLong("vehicleId") ?: return@composable
                val itemId = entry.arguments?.getLong("itemId") ?: return@composable
                val viewModel = viewModel<MaintenanceDetailViewModel>(
                    key = "item-$vehicleId-$itemId",
                    factory = viewModelFactory { MaintenanceDetailViewModel(vehicleId, itemId, repository) },
                )
                MaintenanceDetailScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onRecordService = { navController.navigate(Routes.record(vehicleId, itemId)) },
                )
            }
            composable(
                route = Routes.RECORD,
                arguments = listOf(
                    navArgument("vehicleId") { type = NavType.LongType },
                    navArgument("itemId") { type = NavType.LongType; defaultValue = 0L },
                    navArgument("mode") { type = NavType.StringType; defaultValue = Routes.MODE_SERVICE },
                ),
            ) { entry ->
                val vehicleId = entry.arguments?.getLong("vehicleId") ?: return@composable
                val itemId = entry.arguments?.getLong("itemId") ?: 0L
                val mode = RecordServiceViewModel.modeFrom(entry.arguments?.getString("mode"))
                val viewModel = viewModel<RecordServiceViewModel>(
                    key = "record-$vehicleId-$itemId-$mode",
                    factory = viewModelFactory { RecordServiceViewModel(vehicleId, itemId, mode, repository) },
                )
                RecordServiceScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onFinished = { navController.popBackStack() },
                )
            }
            composable(
                route = Routes.REMINDERS,
                arguments = listOf(navArgument("vehicleId") { type = NavType.LongType }),
            ) { entry ->
                val vehicleId = entry.arguments?.getLong("vehicleId") ?: return@composable
                val viewModel = viewModel<DashboardViewModel>(
                    key = "reminders-$vehicleId",
                    factory = viewModelFactory { DashboardViewModel(vehicleId, repository) },
                )
                RemindersScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onItem = { navController.navigate(Routes.maintenanceDetail(vehicleId, it)) },
                )
            }
            composable(
                route = Routes.EXPENSES,
                arguments = listOf(navArgument("vehicleId") { type = NavType.LongType }),
            ) { entry ->
                val vehicleId = entry.arguments?.getLong("vehicleId") ?: return@composable
                val viewModel = viewModel<ExpensesViewModel>(
                    key = "expenses-$vehicleId",
                    factory = viewModelFactory { ExpensesViewModel(vehicleId, repository) },
                )
                ExpensesScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onAddExpense = { navController.navigate(Routes.record(vehicleId, mode = Routes.MODE_EXPENSE)) },
                )
            }
            composable(Routes.SETTINGS) {
                SettingsScreen(
                    userPreferences = userPreferences,
                    onBack = { navController.popBackStack() },
                )
            }
        }
    }
}
