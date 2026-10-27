package com.example.chefsito

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.chefsito.ui.components.ChefsitoBottomBar
import com.example.chefsito.ui.screens.FavoritesScreen
import com.example.chefsito.ui.screens.HomeScreen
import com.example.chefsito.ui.screens.PlanScreen
import com.example.chefsito.ui.screens.ProfileScreen
import com.example.chefsito.ui.theme.ChefsitoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ChefsitoTheme {
                var currentRoute by remember { mutableStateOf("home") }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        ChefsitoBottomBar(
                            currentRoute = currentRoute,
                            onNavigate = { route -> currentRoute = route },
                        )
                    },
                ) { innerPadding ->
                    val screenModifier = Modifier.padding(innerPadding)
                    when (currentRoute) {
                        "home" -> HomeScreen(
                            modifier = screenModifier,
                            onNavigateToBudget = { currentRoute = "plan" },
                            onNavigateToPantry = { currentRoute = "plan" },
                            onNavigateToProfile = { currentRoute = "profile" },
                        )
                        "plan" -> PlanScreen(
                            modifier = screenModifier,
                            onNavigatePantry = { currentRoute = "plan" },
                        )
                        "favorites" -> FavoritesScreen(
                            modifier = screenModifier,
                            onRecipeClick = { /* ver detalle receta */ },
                        )
                        "profile" -> ProfileScreen(
                            modifier = screenModifier,
                            onLogout = { currentRoute = "home" },
                        )
                        else -> HomeScreen(
                            modifier = screenModifier,
                            onNavigateToBudget = { currentRoute = "plan" },
                            onNavigateToPantry = { currentRoute = "plan" },
                            onNavigateToProfile = { currentRoute = "profile" },
                        )
                    }
                }
            }
        }
    }
}
