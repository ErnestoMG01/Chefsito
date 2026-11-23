package com.example.chefsito.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.chefsito.ui.theme.FondoCrema
import com.example.chefsito.ui.theme.NaranjaCoral
import com.example.chefsito.ui.theme.TintaComic

@Composable
fun ChefsitoIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconVector: androidx.compose.ui.graphics.vector.ImageVector = Icons.Default.Delete,
    contentDescription: String = "Eliminar",
    tint: androidx.compose.ui.graphics.Color = NaranjaCoral,
    showBorder: Boolean = true,
    borderColor: androidx.compose.ui.graphics.Color = TintaComic,
    backgroundColor: androidx.compose.ui.graphics.Color = FondoCrema,
    size: androidx.compose.ui.unit.Dp = 32.dp,
    iconSize: androidx.compose.ui.unit.Dp = 16.dp,
    cornerRadius: androidx.compose.ui.unit.Dp = 8.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .border(if (showBorder) 1.5.dp else 0.dp, borderColor, RoundedCornerShape(cornerRadius))
            .clip(RoundedCornerShape(cornerRadius))
            .background(backgroundColor)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = iconVector,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(iconSize)
        )
    }
}
