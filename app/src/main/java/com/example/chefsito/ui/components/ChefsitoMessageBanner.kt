package com.example.chefsito.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chefsito.ui.theme.*

enum class MessageType {
    SUCCESS,
    DELETE,
    WARNING
}

@Composable
fun ChefsitoMessageBanner(
    title: String,
    message: String,
    type: MessageType,
    modifier: Modifier = Modifier
) {
    val (backgroundColor, iconVector) = when (type) {
        MessageType.SUCCESS -> VerdeFresco to Icons.Default.Check
        MessageType.DELETE -> NaranjaCoral to Icons.Default.Delete
        MessageType.WARNING -> AmarilloMostaza to Icons.Default.Warning
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .border(2.dp, TintaComic, RoundedCornerShape(12.dp))
            .background(backgroundColor, RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .border(1.5.dp, TintaComic, RoundedCornerShape(16.dp))
                .background(BlancoPuro, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = iconVector,
                contentDescription = null,
                tint = backgroundColor,
                modifier = Modifier.size(18.dp)
            )
        }

        Column {
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = BlancoPuro.copy(alpha = 0.9f)
            )
            Text(
                text = message,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = BlancoPuro
            )
        }
    }
}