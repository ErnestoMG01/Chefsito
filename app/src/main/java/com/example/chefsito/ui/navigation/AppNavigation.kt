package com.example.chefsito.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.ui.graphics.vector.ImageVector

/** Rutas de la app en un solo lugar (evita escribir strings sueltos). */
object Routes {
    const val SPLASH = "splash"
    const val AUTH = "auth"
    const val HOME = "home"
    const val PLAN = "plan"
    const val FAVORITES = "favorites"
    const val PROFILE = "profile"
    const val PANTRY = "pantry"
}

/** Un destino de la barra inferior. */
data class BottomNavItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
)

/** Pestañas que muestra la barra inferior. Para agregar una, solo se añade aquí. */
val bottomNavItems = listOf(
    BottomNavItem(Routes.HOME, "Inicio", Icons.Filled.Home, Icons.Outlined.Home),
    BottomNavItem(Routes.PLAN, "Planificar", Icons.Filled.DateRange, Icons.Outlined.DateRange),
    BottomNavItem(Routes.FAVORITES, "Favoritos", Icons.Filled.Favorite, Icons.Outlined.FavoriteBorder),
    BottomNavItem(Routes.PROFILE, "Perfil", Icons.Filled.Person, Icons.Outlined.Person),
)

/** Pantallas donde se muestra la barra inferior ("pantry" cuelga de Inicio/Planificar). */
val routesWithBottomBar = bottomNavItems.map { it.route } + Routes.PANTRY
