package com.example.chefsito.ui.screens.plan

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chefsito.ui.viewmodel.ChefViewModel
import com.example.chefsito.ui.theme.*

@Composable
fun PlanScreen(
    viewModel: ChefViewModel,
    onNavigateHome: () -> Unit,
    onNavigatePantry: () -> Unit,
    onNavigateFavorites: () -> Unit,
    onNavigateProfile: () -> Unit
) {
    val currentBudget by viewModel.userBudget.collectAsState()
    var budgetText by remember { mutableStateOf(currentBudget.toInt().toString()) }
    var selectedTab by remember { mutableStateOf("Semanal") }

    LaunchedEffect(currentBudget) {
        budgetText = currentBudget.toInt().toString()
    }

    // Usamos un solo Column que ocupa toda la pantalla de forma limpia
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FondoCrema)
            .padding(16.dp)
    ) {
        // Título de la pantalla y subtítulo
        Text(
            text = "Configurar Plan",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = TintaComic
        )
        Text(
            text = "Plan Semanal · 5 días de comidas planeadas",
            fontSize = 12.sp,
            color = TintaComic.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Tarjeta superior de "Despensa registrada"
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
                    Column {
                        Text(
                            text = "Despensa registrada",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TintaComic
                        )
                        Text(
                            text = "Ingredientes disponibles en casa",
                            fontSize = 11.sp,
                            color = TintaComic.copy(alpha = 0.6f)
                        )
                    }
                }

                TextButton(onClick = onNavigatePantry) {
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
            text = "¿TIPO DE PLAN?",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TintaComic.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Selector de Día / Semanal
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val tabs = listOf("Día", "Semanal")
            tabs.forEach { tab ->
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
            text = "MONTO DISPONIBLE",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TintaComic.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Campo de texto editable del presupuesto
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

        // Botones rápidos de montos ($50, $100, $150, $200)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val presetAmounts = listOf(50, 100, 150, 200)
            presetAmounts.forEach { amount ->
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

        // Botón principal para generar menú semanal
        Button(
            onClick = { /* Acción para generar menú */ },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .border(2.dp, TintaComic, RoundedCornerShape(12.dp)),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = NaranjaCoral)
        ) {
            Text(
                text = "Generar menú semanal →",
                color = BlancoPuro,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }
    }
}