package com.example.chefsito.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chefsito.ui.theme.*

@Composable
fun PlanScreen(
    modifier: Modifier = Modifier,
    onNavigatePantry: () -> Unit = {},
) {
    var isWeeklyPlan by remember { mutableStateOf(true) }
    var selectedBudget by remember { mutableStateOf("200") }

    val presetAmounts = listOf("50", "100", "150", "200")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FondoCrema)
    ) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
        ) {
            item {
                Text(
                    text = "Configurar Plan",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TintaComic
                )
                Text(
                    text = if (isWeeklyPlan) "Plan Semanal - 5 días de comidas planeadas" else "Plan del Día - Sugerencias para hoy",
                    fontSize = 12.sp,
                    color = TintaComic.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(16.dp))

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
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Kitchen,
                                contentDescription = "Despensa",
                                tint = VerdeFresco,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Despensa registrada",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = TintaComic
                                )
                                Text(
                                    text = "Ingredientes disponibles en casa",
                                    fontSize = 12.sp,
                                    color = TintaComic.copy(alpha = 0.6f)
                                )
                            }
                        }
                        TextButton(onClick = onNavigatePantry) {
                            Text(text = "Editar", color = NaranjaCoral, fontWeight = FontWeight.Bold)
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

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { isWeeklyPlan = false },
                        modifier = Modifier
                            .weight(1f)
                            .border(2.dp, TintaComic, RoundedCornerShape(10.dp)),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (!isWeeklyPlan) AmarilloMostaza else BlancoPuro
                        )
                    ) {
                        Text(
                            text = "Día",
                            color = TintaComic,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Button(
                        onClick = { isWeeklyPlan = true },
                        modifier = Modifier
                            .weight(1f)
                            .border(2.dp, TintaComic, RoundedCornerShape(10.dp)),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isWeeklyPlan) AmarilloMostaza else BlancoPuro
                        )
                    ) {
                        Text(
                            text = "Semanal",
                            color = TintaComic,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "MONTO DISPONIBLE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TintaComic.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(2.dp, TintaComic, RoundedCornerShape(12.dp))
                        .background(BlancoPuro)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.MonetizationOn,
                        contentDescription = "Monedas",
                        tint = VerdeFresco,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = selectedBudget,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = TintaComic,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "MXN",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TintaComic.copy(alpha = 0.6f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    presetAmounts.forEach { amount ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .border(2.dp, TintaComic, RoundedCornerShape(8.dp))
                                .background(if (selectedBudget == amount) VerdeFresco else BlancoPuro)
                                .clickable { selectedBudget = amount }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$$amount",
                                fontWeight = FontWeight.Bold,
                                color = if (selectedBudget == amount) BlancoPuro else TintaComic,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                Button(
                    onClick = { /* Generar plan */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .border(2.dp, TintaComic, RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NaranjaCoral)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = if (isWeeklyPlan) "Generar menú semanal" else "Ver sugerencias del día",
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
    }
}
