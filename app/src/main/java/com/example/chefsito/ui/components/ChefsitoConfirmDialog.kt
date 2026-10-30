package com.example.chefsito.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chefsito.ui.theme.*

@Composable
fun ChefsitoConfirmDialog(
    title: String,
    message: String,
    confirmText: String = "Borrar",
    dismissText: String = "Cancelar",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.border(2.dp, TintaComic, RoundedCornerShape(16.dp)),
        containerColor = FondoCrema,
        title = {
            Text(
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TintaComic
            )
        },
        text = {
            Text(
                text = message,
                fontSize = 14.sp,
                color = TintaComic.copy(alpha = 0.8f)
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                modifier = Modifier
                    .border(1.5.dp, TintaComic, RoundedCornerShape(10.dp)),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NaranjaCoral)
            ) {
                Text(
                    text = confirmText,
                    color = BlancoPuro,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier
                    .border(1.5.dp, TintaComic, RoundedCornerShape(10.dp)),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = BlancoPuro)
            ) {
                Text(
                    text = dismissText,
                    color = TintaComic,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    )
}