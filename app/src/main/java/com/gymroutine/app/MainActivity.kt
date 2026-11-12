package com.gymroutine.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.gymroutine.app.ui.screens.DietScreen
import com.gymroutine.app.ui.screens.HomeScreen
import com.gymroutine.app.ui.screens.PlansScreen
import com.gymroutine.app.ui.theme.GymPlannerTheme
import com.gymroutine.app.ui.viewmodel.GymViewModel

// Define route objects for type-safe navigation
sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Diet : Screen("diet")
    object Plans : Screen("plans")
}

class MainActivity : ComponentActivity() {
    private val viewModel: GymViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GymPlannerTheme {
                val navController = rememberNavController()
                NavHost(
                    navController = navController,
                    startDestination = Screen.Home.route
                ) {
                    composable(Screen.Home.route) {
                        HomeScreen(viewModel = viewModel, navController = navController)
                    }
                    composable(Screen.Diet.route) {
                        DietScreen(
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }
                    composable(Screen.Plans.route) {
                        PlansScreen(
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // If the app stayed open past midnight, switch to the new day
        viewModel.onAppResumed()
    }
}
