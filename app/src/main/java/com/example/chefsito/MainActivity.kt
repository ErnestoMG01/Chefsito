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
import androidx.compose.runtime.collectAsState
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
import com.example.chefsito.ui.screens.recipe.RecipeDetailScreen
import com.example.chefsito.ui.screens.splash.SplashScreen
import com.example.chefsito.ui.theme.ChefsitoTheme
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.chefsito.ui.recipe.RecipeViewModel
import com.example.chefsito.ui.recipe.RecipeViewModelFactory
import com.example.chefsito.ChefsitoApplication
import com.example.chefsito.ui.viewmodel.ChefViewModel
import com.example.chefsito.ui.viewmodel.CommunityViewModel

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
            val context = androidx.compose.ui.platform.LocalContext.current
            val application = context.applicationContext as ChefsitoApplication
            
            // Instanciar RecipeViewModel a nivel global para compartirlo
            val recipeViewModel: RecipeViewModel = viewModel(
                factory = RecipeViewModelFactory(
                    recipeRepository = application.recipeRepository,
                    ingredientRepository = application.ingredientRepository
                )
            )

            // ViewModel de la sección "Recetas de la Comunidad" (API externa).
            val communityViewModel: CommunityViewModel = viewModel()
            
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
                                navController = navController,
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
                            val communityState by communityViewModel.uiState
                                .collectAsStateWithLifecycle()
                            val profileImagePath by chefViewModel.profileImagePath.collectAsState()

                            HomeScreen(
                                profileImagePath = profileImagePath,
                                communityState = communityState,
                                onRetryCommunity = { communityViewModel.retry() },
                                onNavigateToBudget = { navController.goTo(Routes.PLAN) },
                                onNavigateToPantry = { navController.goTo(Routes.PANTRY) },
                                onNavigateToProfile = { navController.goTo(Routes.PROFILE) }
                            )
                        }
                        composable(Routes.PLAN) {
                            PlanScreen(
                                viewModel = chefViewModel,
                                recipeViewModel = recipeViewModel,
                                onNavigateHome = { navController.goTo(Routes.HOME) },
                                onNavigatePantry = { navController.goTo(Routes.PANTRY) },
                                onNavigateFavorites = { navController.goTo(Routes.FAVORITES) },
                                onNavigateProfile = { navController.goTo(Routes.PROFILE) },
                                onNavigateRecipe = { recipeId ->
                                    navController.navigate(Routes.recipeDetail(recipeId))
                                },
                                onToggleFavorite = { recipeId ->
                                    recipeViewModel.toggleFavorite(recipeId)
                                }
                            )
                        }
                        composable(Routes.FAVORITES) {
                            val uiState by recipeViewModel.uiState.collectAsStateWithLifecycle()

                            // Solo recetas marcadas con el corazón (isFavorite = 1).
                            FavoritesScreen(
                                recipes = uiState.favoriteRecipes,
                                onToggleFavorite = { recipe ->
                                    recipeViewModel.updateRecipe(
                                        recipe.copy(isFavorite = !recipe.isFavorite)
                                    )
                                },
                                onNavigateRecipe = { recipeId ->
                                    navController.navigate(Routes.recipeDetail(recipeId))
                                }
                            )
                        }
                        composable(Routes.RECIPE_DETAIL) { backStackEntry ->
                            val recipeId = backStackEntry.arguments
                                ?.getString("recipeId")
                                ?.toLongOrNull() ?: 0L
                            val uiState by recipeViewModel.uiState.collectAsStateWithLifecycle()
                            val pantry by chefViewModel.pantryIngredients.collectAsState()
                            val budget by chefViewModel.userBudget.collectAsState()

                            val recipe = uiState.recipes.find { it.id == recipeId }
                            if (recipe != null) {
                                RecipeDetailScreen(
                                    recipe = recipe,
                                    pantry = pantry,
                                    budget = budget,
                                    onBack = { navController.popBackStack() }
                                )
                            }
                        }
                        composable(Routes.PROFILE) {
                            ProfileScreen(
                                viewModel = chefViewModel,
                                onNavigateHome = { navController.goTo(Routes.HOME) },
                                onNavigatePlan = { navController.goTo(Routes.PLAN) },
                                onNavigateFavorites = { navController.goTo(Routes.FAVORITES) },
                                onNavigateProfile = { navController.goTo(Routes.PROFILE) },
                                onLogout = {
                                    com.example.chefsito.utils.SessionManager.logout(context)
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


