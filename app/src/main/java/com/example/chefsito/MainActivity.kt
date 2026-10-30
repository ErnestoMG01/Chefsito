package com.example.chefsito

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.chefsito.ui.components.ChefsitoBottomBar
import com.example.chefsito.ui.screens.favorites.FavoritesScreen
import com.example.chefsito.ui.screens.home.HomeScreen
import com.example.chefsito.ui.screens.pantry.PantryScreen
import com.example.chefsito.ui.screens.plan.PlanScreen
import com.example.chefsito.ui.screens.profile.ProfileScreen
import com.example.chefsito.ui.theme.ChefsitoTheme
import com.example.chefsito.ui.viewmodel.ChefViewModel

private fun NavHostController.goTo(route: String) {
    navigate(route) {
        popUpTo(graph.startDestinationId) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

class MainActivity : ComponentActivity() {

    // ViewModel que conecta la base de datos Room con la UI
    private val chefViewModel: ChefViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ChefsitoTheme {
                val navController = rememberNavController()

                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route ?: "home"

                val showBottomBar = currentRoute in listOf("home", "plan", "favorites", "profile", "pantry")

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        if (showBottomBar) {
                            ChefsitoBottomBar(
                                currentRoute = currentRoute,
                                onNavigate = { route -> navController.goTo(route) }
                            )
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "home",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("home") {
                            HomeScreen(
                                onNavigateToBudget = { navController.goTo("plan") },
                                onNavigateToPantry = { navController.goTo("pantry") },
                                onNavigateToProfile = { navController.goTo("profile") }
                            )
                        }
                        composable("plan") {
                            PlanScreen(
                                viewModel = chefViewModel,
                                onNavigateHome = { navController.goTo("home") },
                                onNavigatePantry = { navController.goTo("pantry") },
                                onNavigateFavorites = { navController.goTo("favorites") },
                                onNavigateProfile = { navController.goTo("profile") }
                            )
                        }
                        composable("favorites") {
                            FavoritesScreen(
                                onRecipeClick = { /* Detalle de receta */ }
                            )
                        }
                        composable("profile") {
                            ProfileScreen(
                                viewModel = chefViewModel,
                                onNavigateHome = { navController.goTo("home") },
                                onNavigatePlan = { navController.goTo("plan") },
                                onNavigateFavorites = { navController.goTo("favorites") },
                                onNavigateProfile = { navController.goTo("profile") }
                            )
                        }
                        composable("pantry") {
                            PantryScreen(
                                viewModel = chefViewModel,
                                onNavigatePlan = { navController.goTo("plan") }
                            )
                        }
                    }
                }
            }
        }
    }
}
