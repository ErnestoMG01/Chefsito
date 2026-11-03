package com.example.chefsito

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
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
import com.example.chefsito.ui.navigation.Routes
import com.example.chefsito.ui.navigation.routesWithBottomBar
import com.example.chefsito.ui.screens.auth.AuthScreen
import com.example.chefsito.ui.screens.favorites.FavoritesScreen
import com.example.chefsito.ui.screens.home.HomeScreen
import com.example.chefsito.ui.screens.pantry.PantryScreen
import com.example.chefsito.ui.screens.plan.PlanScreen
import com.example.chefsito.ui.screens.profile.ProfileScreen
import com.example.chefsito.ui.screens.splash.SplashScreen
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
        // Pantalla completa moderna: la app dibuja detrás de las barras del sistema
        // y cada componente respeta los insets (gestos o 3 botones). Íconos oscuros
        // porque el fondo de la app es claro.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        )
        super.onCreate(savedInstanceState)
        setContent {
            ChefsitoTheme {
                val navController = rememberNavController()

                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route ?: Routes.HOME

                val showBottomBar = currentRoute in routesWithBottomBar

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
                        startDestination = Routes.SPLASH,
                        modifier = Modifier
                            .padding(innerPadding)
                            .consumeWindowInsets(innerPadding)
                            .imePadding() // el teclado no tapa los campos
                    ) {
                        composable(Routes.SPLASH) {
                            SplashScreen(
                                onSplashFinished = {
                                    navController.navigate(Routes.AUTH) {
                                        popUpTo(Routes.SPLASH) { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable(Routes.AUTH) {
                            AuthScreen(
                                onLoginSuccess = { _ ->
                                    navController.navigate(Routes.HOME) {
                                        popUpTo(Routes.AUTH) { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable(Routes.HOME) {
                            HomeScreen(
                                onNavigateToBudget = { navController.goTo(Routes.PLAN) },
                                onNavigateToPantry = { navController.goTo(Routes.PANTRY) },
                                onNavigateToProfile = { navController.goTo(Routes.PROFILE) }
                            )
                        }
                        composable(Routes.PLAN) {
                            PlanScreen(
                                viewModel = chefViewModel,
                                onNavigateHome = { navController.goTo(Routes.HOME) },
                                onNavigatePantry = { navController.goTo(Routes.PANTRY) },
                                onNavigateFavorites = { navController.goTo(Routes.FAVORITES) },
                                onNavigateProfile = { navController.goTo(Routes.PROFILE) }
                            )
                        }
                        composable(Routes.FAVORITES) {
                            FavoritesScreen(
                                onRecipeClick = { /* Detalle de receta */ }
                            )
                        }
                        composable(Routes.PROFILE) {
                            ProfileScreen(
                                viewModel = chefViewModel,
                                onNavigateHome = { navController.goTo(Routes.HOME) },
                                onNavigatePlan = { navController.goTo(Routes.PLAN) },
                                onNavigateFavorites = { navController.goTo(Routes.FAVORITES) },
                                onNavigateProfile = { navController.goTo(Routes.PROFILE) },
                                onLogout = {
                                    navController.navigate(Routes.AUTH) {
                                        popUpTo(0) { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable(Routes.PANTRY) {
                            PantryScreen(
                                viewModel = chefViewModel,
                                onNavigatePlan = { navController.goTo(Routes.PLAN) }
                            )
                        }
                    }
                }
            }
        }
    }
}