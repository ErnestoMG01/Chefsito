package com.example.chefsito.ui.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.chefsito.data.remote.CommunityRecipe
import com.example.chefsito.ui.theme.*
import com.example.chefsito.ui.viewmodel.CommunityUiState
import com.example.chefsito.utils.SessionManager
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    profileImagePath: String? = null,
    communityState: CommunityUiState = CommunityUiState.Loading,
    onRetryCommunity: () -> Unit = {},
    onNavigateToBudget: () -> Unit = {},
    onNavigateToPantry: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
) {
    val mexicoTimeZone = remember { TimeZone.getTimeZone("America/Mexico_City") }
    val context = LocalContext.current
    val userName = remember { SessionManager.getUserName(context) ?: "Chef" }

    val formattedCurrentDate = remember {
        val sdf = SimpleDateFormat("EEEE, d MMM", Locale.forLanguageTag("es-ES"))
        sdf.timeZone = mexicoTimeZone
        sdf.format(Date()).uppercase()
    }

    val greetingText = remember(userName) {
        val saludo = when (Calendar.getInstance(mexicoTimeZone)[Calendar.HOUR_OF_DAY]) {
            in 5..11 -> "Buenos días"
            in 12..19 -> "Buenas tardes"
            else -> "Buenas noches"
        }
        "$saludo, $userName"
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FondoCrema)
            .verticalScroll(rememberScrollState())
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
            Column(modifier = Modifier.weight(1f)) {
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
                if (profileImagePath != null && File(profileImagePath).exists()) {
                    Image(
                        painter = rememberAsyncImagePainter(model = File(profileImagePath)),
                        contentDescription = "Foto de perfil",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Perfil",
                        tint = TintaComic,
                        modifier = Modifier.size(26.dp)
                    )
                }
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

        Spacer(modifier = Modifier.height(24.dp))

        CommunitySection(
            state = communityState,
            onRetry = onRetryCommunity
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun CommunitySection(
    state: CommunityUiState,
    onRetry: () -> Unit
) {
    Text(
        text = "🌐 Recetas de la Comunidad (Conectado vía API)",
        fontSize = 15.sp,
        fontWeight = FontWeight.Bold,
        color = TintaComic
    )
    Text(
        text = when (state) {
            is CommunityUiState.Loading -> "Consultando dummyjson.com…"
            is CommunityUiState.Success -> "Datos en vivo desde dummyjson.com"
            is CommunityUiState.Error -> "Sin conexión: mostrando respaldo local"
        },
        fontSize = 11.sp,
        color = TintaComic.copy(alpha = 0.65f)
    )

    Spacer(modifier = Modifier.height(10.dp))

    when (state) {
        is CommunityUiState.Loading -> {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, TintaComic, RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = BlancoPuro)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 3.dp,
                        color = NaranjaCoral
                    )
                    Text(
                        text = "Cargando recetas de la comunidad…",
                        fontSize = 12.sp,
                        color = TintaComic
                    )
                }
            }
        }

        is CommunityUiState.Error -> {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, TintaComic, RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = NaranjaCoral.copy(alpha = 0.12f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "⚠ ${state.message}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TintaComic
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = onRetry,
                        modifier = Modifier.border(1.5.dp, TintaComic, RoundedCornerShape(10.dp)),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NaranjaCoral),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text("Reintentar", color = BlancoPuro, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            state.fallback.forEach { recipe ->
                CommunityRecipeCard(recipe = recipe, isLocal = true)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        is CommunityUiState.Success -> {
            state.recipes.forEach { recipe ->
                CommunityRecipeCard(recipe = recipe, isLocal = false)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun CommunityRecipeCard(
    recipe: CommunityRecipe,
    isLocal: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, TintaComic, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = BlancoPuro)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (isLocal) "🍽" else "🌎",
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = recipe.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TintaComic
                )
            }
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = "${recipe.cuisine} · ${recipe.difficulty} · ${recipe.prepTimeMinutes} min",
                fontSize = 11.sp,
                color = NaranjaCoral,
                fontWeight = FontWeight.Bold
            )
            if (recipe.ingredients.isNotEmpty()) {
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "${recipe.ingredients.size} ingredientes · ${recipe.instructions.size} pasos",
                    fontSize = 11.sp,
                    color = TintaComic.copy(alpha = 0.65f)
                )
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
