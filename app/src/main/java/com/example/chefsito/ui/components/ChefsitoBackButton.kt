package com.example.chefsito.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chefsito.ui.theme.TintaComic

@Composable
fun ChefsitoBackButton(
    text: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TextButton(
        onClick = onBackClick,
        contentPadding = PaddingValues(0.dp),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "<",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TintaComic
            )
            Text(
                text = text,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TintaComic
            )
        }
    }
}