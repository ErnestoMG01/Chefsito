package com.example.chefsito.ui.screens.plan

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chefsito.data.domain.RecipeCostBreakdown
import com.example.chefsito.ui.recipe.RecipeViewModel
import com.example.chefsito.ui.theme.*
import com.example.chefsito.ui.viewmodel.ChefViewModel
import java.util.Calendar
import java.util.TimeZone

private val weekDays = listOf(
    "Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo"
)

private fun todayName(): String {
    val calendar = Calendar.getInstance(TimeZone.getTimeZone("America/Mexico_City"))
    // Calendar: domingo = 1, lunes = 2 ... sábado = 7. Índice lunes = 0.
    val index = (calendar.get(Calendar.DAY_OF_WEEK) + 5) % 7
    return weekDays[index]
}

@Composable
fun PlanScreen(
    viewModel: ChefViewModel,
    recipeViewModel: RecipeViewModel,
    onNavigateHome: () -> Unit = {},
    onNavigatePantry: () -> Unit = {},
    onNavigateFavorites: () -> Unit = {},
    onNavigateProfile: () -> Unit = {},
    onNavigateRecipe: (Long) -> Unit = {},
    onToggleFavorite: (Long) -> Unit = {}
) {
    val currentBudget by viewModel.userBudget.collectAsState()
    val pantry by viewModel.pantryIngredients.collectAsState()
    val recipeUiState by recipeViewModel.uiState.collectAsState()

    var budgetText by remember { mutableStateOf(currentBudget.toInt().toString()) }
    var selectedTab by remember { mutableStateOf("Día") }
    var selectedDay by remember { mutableStateOf(todayName()) }
    val context = LocalContext.current

    LaunchedEffect(currentBudget) {
        budgetText = currentBudget.toInt().toString()
    }

    recipeUiState.error?.let { errorMessage ->
        LaunchedEffect(errorMessage) {
            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
            recipeViewModel.clearError()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FondoCrema)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Configurar Plan",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = TintaComic
        )
        Text(
            text = if (selectedTab == "Semanal") {
                "Presupuesto semanal repartido entre 7 días"
            } else {
                "Plan del día · Desayuno, Comida y Cena"
            },
            fontSize = 12.sp,
            color = TintaComic.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Despensa registrada
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
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .border(1.5.dp, TintaComic, RoundedCornerShape(8.dp))
                            .background(VerdeFresco, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🛒", fontSize = 16.sp)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Despensa registrada",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TintaComic
                        )
                        Text(
                            text = "${pantry.size} ingrediente(s) con costo capturado",
                            fontSize = 11.sp,
                            color = TintaComic.copy(alpha = 0.6f)
                        )
                    }
                }

                TextButton(
                    onClick = onNavigatePantry,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Editar",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = NaranjaCoral
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "¿QUÉ DÍA QUIERES PLANEAR?",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TintaComic.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            weekDays.forEach { day ->
                val isSelected = selectedDay == day
                Button(
                    onClick = { selectedDay = day },
                    modifier = Modifier
                        .height(40.dp)
                        .border(2.dp, TintaComic, RoundedCornerShape(10.dp)),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) NaranjaCoral else BlancoPuro
                    ),
                    contentPadding = PaddingValues(horizontal = 14.dp)
                ) {
                    Text(
                        text = day,
                        color = if (isSelected) BlancoPuro else TintaComic,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "¿TIPO DE PLAN?",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TintaComic.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Día", "Semanal").forEach { tab ->
                val isSelected = selectedTab == tab
                Button(
                    onClick = { selectedTab = tab },
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .border(2.dp, TintaComic, RoundedCornerShape(10.dp)),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) AmarilloMostaza else BlancoPuro
                    ),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = tab,
                        color = TintaComic,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = if (selectedTab == "Semanal") "PRESUPUESTO SEMANAL" else "PRESUPUESTO DEL DÍA",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TintaComic.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .border(2.dp, TintaComic, RoundedCornerShape(12.dp))
                .background(BlancoPuro, RoundedCornerShape(12.dp))
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = VerdeFresco
            )
            Spacer(modifier = Modifier.width(8.dp))
            BasicTextField(
                value = budgetText,
                onValueChange = { newValue ->
                    if (newValue.all { it.isDigit() }) {
                        budgetText = newValue
                        val parsed = newValue.toDoubleOrNull() ?: 0.0
                        viewModel.updateBudget(parsed)
                    }
                },
                modifier = Modifier.weight(1f),
                textStyle = TextStyle(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TintaComic
                ),
                cursorBrush = SolidColor(TintaComic),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )
            Text(
                text = "MXN",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TintaComic.copy(alpha = 0.5f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(50, 100, 150, 200).forEach { amount ->
                val isSelected = currentBudget == amount.toDouble()
                Button(
                    onClick = { viewModel.updateBudget(amount.toDouble()) },
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .border(1.5.dp, TintaComic, RoundedCornerShape(8.dp)),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) VerdeFresco else BlancoPuro
                    ),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = "$$amount",
                        color = if (isSelected) BlancoPuro else TintaComic,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                val budget = budgetText.toDoubleOrNull()
                if (budget == null || budget <= 0.0) {
                    Toast.makeText(context, "Ingresa un presupuesto válido", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                recipeViewModel.generateRecipe(
                    budget = budget,
                    planType = selectedTab,
                    dayOfWeek = selectedDay
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .border(2.dp, TintaComic, RoundedCornerShape(12.dp)),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = NaranjaCoral),
            enabled = !recipeUiState.isGeneratingRecipe
        ) {
            if (recipeUiState.isGeneratingRecipe) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = BlancoPuro,
                    strokeWidth = 3.dp
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Calculando costos...",
                    color = BlancoPuro,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            } else {
                Text(
                    text = "Generar plan de $selectedDay →",
                    color = BlancoPuro,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }

        recipeUiState.message?.let { message ->
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = message,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = VerdeFresco
            )
        }

        val recommendations = recipeUiState.recommendations
        if (recommendations.isNotEmpty()) {
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "PLAN DE ${recipeUiState.selectedDay.uppercase()}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TintaComic.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(8.dp))

            recommendations.forEach { breakdown ->
                val currentRecipe = recipeUiState.recipes
                    .find { it.id == breakdown.recipe.id }
                    ?: breakdown.recipe

                MealSectionHeader(mealType = breakdown.recipe.mealType)
                Spacer(modifier = Modifier.height(6.dp))
                RecommendationCard(
                    breakdown = breakdown,
                    isFavorite = currentRecipe.isFavorite,
                    onToggleFavorite = { onToggleFavorite(currentRecipe.id) },
                    onOpenRecipe = { onNavigateRecipe(currentRecipe.id) }
                )
                Spacer(modifier = Modifier.height(14.dp))
            }
        }
    }
}

@Composable
private fun MealSectionHeader(mealType: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .border(1.5.dp, TintaComic, RoundedCornerShape(20.dp))
                .background(mealColor(mealType))
                .padding(horizontal = 10.dp, vertical = 3.dp)
        ) {
            Text(
                text = mealType.uppercase(),
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                color = if (mealType == "Desayuno") TintaComic else BlancoPuro
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .height(2.dp)
                .background(TintaComic.copy(alpha = 0.15f))
        )
    }
}

@Composable
private fun RecommendationCard(
    breakdown: RecipeCostBreakdown,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onOpenRecipe: () -> Unit
) {
    val recipe = breakdown.recipe
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, TintaComic, RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = BlancoPuro)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = recipe.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TintaComic
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = recipe.description,
                        fontSize = 11.sp,
                        color = TintaComic.copy(alpha = 0.7f),
                        maxLines = 2
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier
                        .size(38.dp)
                        .border(2.dp, TintaComic, CircleShape)
                        .clip(CircleShape)
                        .background(if (isFavorite) NaranjaCoral else BlancoPuro)
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = if (isFavorite) "Quitar de favoritos" else "Agregar a favoritos",
                        tint = if (isFavorite) BlancoPuro else TintaComic,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BadgePill(text = "$${recipe.estimatedPrice.toInt()} estimado", color = BlancoPuro)
                BadgePill(text = "${recipe.prepTimeMinutes} min", color = BlancoPuro)
                BadgePill(text = recipe.difficulty, color = AmarilloMostaza)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Desglose de costos contra la despensa del usuario
            CostRow(
                label = "Ya tienes (${breakdown.owned.size})",
                value = "$${breakdown.ownedCost.toInt()}",
                color = VerdeFresco
            )
            CostRow(
                label = "Te falta (${breakdown.missing.size})",
                value = "$${breakdown.missingCost.toInt()}",
                color = NaranjaCoral
            )
            HorizontalDivider(color = TintaComic.copy(alpha = 0.12f))
            CostRow(
                label = "Costo total",
                value = "$${breakdown.totalCost.toInt()}",
                color = TintaComic,
                bold = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, TintaComic, RoundedCornerShape(8.dp))
                    .background(
                        if (breakdown.fitsBudget) VerdeFresco.copy(alpha = 0.18f)
                        else NaranjaCoral.copy(alpha = 0.15f),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = if (breakdown.fitsBudget) {
                        "✓ Dentro de tu presupuesto de $${breakdown.budget.toInt()}"
                    } else {
                        "⚠ Te pasas por $${(-breakdown.remainingBudget).toInt()} de tu presupuesto"
                    },
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (breakdown.fitsBudget) VerdeFresco else NaranjaCoral
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            TextButton(
                onClick = onOpenRecipe,
                contentPadding = PaddingValues(0.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Ver receta",
                        color = NaranjaCoral,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = NaranjaCoral,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CostRow(
    label: String,
    value: String,
    color: Color,
    bold: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = TintaComic.copy(alpha = 0.7f),
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal
        )
        Text(
            text = value,
            fontSize = 12.sp,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun BadgePill(text: String, color: Color) {
    Box(
        modifier = Modifier
            .border(1.5.dp, TintaComic, RoundedCornerShape(20.dp))
            .background(color)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (color == BlancoPuro || color == AmarilloMostaza) TintaComic else BlancoPuro
        )
    }
}

private fun mealColor(mealType: String): Color = when (mealType) {
    "Desayuno" -> AmarilloMostaza
    "Comida" -> NaranjaCoral
    "Cena" -> VerdeFresco
    else -> TintaComic
}
