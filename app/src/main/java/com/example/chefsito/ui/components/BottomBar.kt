package com.example.chefsito.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chefsito.ui.theme.*

private data class NavItemData(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
)

@Composable
fun ChefsitoBottomBar(
    currentRoute: String,
    onNavigate: (String) -> Unit,
) {
    val items = listOf(
        NavItemData("home", "Inicio", Icons.Filled.Home, Icons.Outlined.Home),
        NavItemData("plan", "Planificar", Icons.Filled.DateRange, Icons.Outlined.DateRange),
        NavItemData("favorites", "Favoritos", Icons.Filled.Favorite, Icons.Outlined.FavoriteBorder),
        NavItemData("profile", "Perfil", Icons.Filled.Person, Icons.Outlined.Person),
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, TintaComic),
        color = BlancoPuro
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { (route, label, selectedIcon, unselectedIcon) ->
                val isSelected = currentRoute == route
                val containerColor = if (isSelected) NaranjaCoral else BlancoPuro
                val contentColor = if (isSelected) BlancoPuro else TintaComic

                Box(
                    modifier = Modifier
                        .border(
                            width = 2.dp,
                            color = TintaComic,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .background(
                            color = containerColor,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { onNavigate(route) }
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = if (isSelected) selectedIcon else unselectedIcon,
                            contentDescription = label,
                            tint = contentColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = label,
                            color = contentColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}
