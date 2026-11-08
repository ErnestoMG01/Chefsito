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
import androidx.compose.ui.unit.dp
import com.example.chefsito.ui.theme.FondoCrema
import com.example.chefsito.ui.theme.NaranjaCoral
import com.example.chefsito.ui.theme.TintaComic

@Composable
fun ChefsitoIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(32.dp)
            .border(1.5.dp, TintaComic, RoundedCornerShape(8.dp))
            .clip(RoundedCornerShape(8.dp))
            .background(FondoCrema)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Delete,
            contentDescription = "Eliminar",
            tint = NaranjaCoral,
            modifier = Modifier.size(16.dp)
        )
    }
}
