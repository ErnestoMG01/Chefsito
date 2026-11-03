package com.example.chefsito.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chefsito.ui.navigation.BottomNavItem
import com.example.chefsito.ui.navigation.bottomNavItems
import com.example.chefsito.ui.theme.*

/** Por debajo de este ancho la barra apila ícono y texto para que todo quepa. */
private val CompactWidth = 380.dp

/**
 * Barra de navegación inferior reutilizable.
 *
 * - El fondo blanco se extiende DETRÁS de la barra del sistema (gestos o 3 botones)
 *   y el contenido se sube lo necesario con [navigationBarsPadding], así nunca se
 *   encima con la píldora de gestos ni con los botones.
 * - Se adapta al ancho: en teléfonos angostos muestra ícono arriba y texto abajo.
 */
@Composable
fun ChefsitoBottomBar(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier,
    items: List<BottomNavItem> = bottomNavItems,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .border(2.dp, TintaComic),
        color = BlancoPuro
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding() // respeta gestos / botones del sistema
        ) {
            val compact = maxWidth < CompactWidth

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(if (compact) 4.dp else 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEach { item ->
                    BottomBarItem(
                        item = item,
                        selected = currentRoute == item.route,
                        compact = compact,
                        onClick = { onNavigate(item.route) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun BottomBarItem(
    item: BottomNavItem,
    selected: Boolean,
    compact: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(12.dp)
    val containerColor = if (selected) NaranjaCoral else BlancoPuro
    val contentColor = if (selected) BlancoPuro else TintaComic
    val icon = if (selected) item.selectedIcon else item.unselectedIcon

    Box(
        modifier = modifier
            .heightIn(min = 48.dp) // tamaño mínimo táctil recomendado
            .clip(shape)
            .background(containerColor, shape)
            .border(2.dp, TintaComic, shape)
            .clickable(role = Role.Tab, onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        if (compact) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                BarIcon(icon, contentColor, size = 20.dp)
                BarLabel(item.label, contentColor, fontSize = 10)
            }
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                BarIcon(icon, contentColor, size = 18.dp)
                Spacer(modifier = Modifier.width(4.dp))
                BarLabel(item.label, contentColor, fontSize = 12)
            }
        }
    }
}

@Composable
private fun BarIcon(icon: ImageVector, tint: Color, size: Dp) {
    Icon(
        imageVector = icon,
        contentDescription = null, // el texto ya describe el botón
        tint = tint,
        modifier = Modifier.size(size)
    )
}

@Composable
private fun BarLabel(text: String, color: Color, fontSize: Int) {
    Text(
        text = text,
        color = color,
        fontWeight = FontWeight.Bold,
        fontSize = fontSize.sp,
        maxLines = 1,
        overflow = TextOverflow.Clip
    )
}
