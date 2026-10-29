package com.example.chefsito.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chefsito.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onNavigateToBudget: () -> Unit = {},
    onNavigateToPantry: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
) {
    val mexicoTimeZone = remember { TimeZone.getTimeZone("America/Mexico_City") }

    val formattedCurrentDate = remember {
        val sdf = SimpleDateFormat("EEEE, d MMM", Locale.forLanguageTag("es-ES"))
        sdf.timeZone = mexicoTimeZone
        sdf.format(Date()).uppercase()
    }

    val greetingText = remember {
        when (Calendar.getInstance(mexicoTimeZone)[Calendar.HOUR_OF_DAY]) {
            in 5..11 -> "¡Hola, Buenos días!"
            in 12..19 -> "¡Hola, Buenas tardes!"
            else -> "¡Hola, Buenas noches!"
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FondoCrema)
            .padding(16.dp),
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    text = formattedCurrentDate,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TintaComic.copy(alpha = 0.6f)
                )
                Text(
                    text = greetingText,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TintaComic
                )
            }
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .border(2.dp, TintaComic, CircleShape)
                    .clip(CircleShape)
                    .background(AmarilloMostaza)
                    .clickable { onNavigateToProfile() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Perfil",
                    tint = TintaComic,
                    modifier = Modifier.size(26.dp)
                )
            }
        }

        // Hero Banner
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, TintaComic, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = AmarilloMostaza)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = TintaComic,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Hora de cocinar",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TintaComic
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "¿Qué vamos a cocinar hoy?",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TintaComic
                )
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onNavigateToBudget,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(2.dp, TintaComic, RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NaranjaCoral)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Armar mi menú de hoy",
                            color = BlancoPuro,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = BlancoPuro,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "ACCESOS RÁPIDOS",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = TintaComic.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Card(
                modifier = Modifier
                    .weight(1f)
                    .border(2.dp, TintaComic, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = BlancoPuro),
                onClick = onNavigateToBudget
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Icon(
                        imageVector = Icons.Default.AccountBalanceWallet,
                        contentDescription = "Presupuesto",
                        tint = VerdeFresco,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Configurar Presupuesto",
                        fontWeight = FontWeight.Bold,
                        color = TintaComic,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Card(
                modifier = Modifier
                    .weight(1f)
                    .border(2.dp, TintaComic, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = BlancoPuro),
                onClick = onNavigateToPantry
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = "Insumos",
                        tint = NaranjaCoral,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Gestionar Insumos",
                        fontWeight = FontWeight.Bold,
                        color = TintaComic,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    ChefsitoTheme {
        HomeScreen()
    }
}
