package com.example.chefsito.ui.screens.pantry

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chefsito.ui.viewmodel.ChefViewModel
import com.example.chefsito.ui.theme.*

@Composable
fun PantryScreen(
    viewModel: ChefViewModel,
    modifier: Modifier = Modifier,
) {
    // Recolectamos los ingredientes en tiempo real desde la base de datos local (Room)
    val pantryIngredients by viewModel.pantryIngredients.collectAsState()

    // Estados locales para el diálogo de agregar insumo
    var showAddDialog by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }
    var newPrice by remember { mutableStateOf("") }

    // Cálculo dinámico del costo total de los ingredientes en casa
    val totalPrice = pantryIngredients.sumOf { it.price }

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
            // Cabecera y Resumen
            item {
                Text(
                    text = "Mi Despensa",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TintaComic
                )
                Text(
                    text = "Marca lo que tienes y anota su precio",
                    fontSize = 12.sp,
                    color = TintaComic.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Tarjeta de Valor Total
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
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "En casa",
                                fontSize = 12.sp,
                                color = TintaComic.copy(alpha = 0.6f)
                            )
                            Text(
                                text = "${pantryIngredients.size} ingredientes",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TintaComic
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Valor total",
                                fontSize = 12.sp,
                                color = TintaComic.copy(alpha = 0.6f)
                            )
                            Text(
                                text = "$${totalPrice.toInt()} MXN",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = VerdeFresco
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Botón para abrir el diálogo de inserción
                Button(
                    onClick = { showAddDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .border(2.dp, TintaComic, RoundedCornerShape(10.dp)),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AmarilloMostaza)
                ) {
                    Text(
                        text = "+ Agregar ingrediente",
                        color = TintaComic,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "LISTA DE INGREDIENTES",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TintaComic.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Lista dinámica de Room
            items(pantryIngredients, key = { it.id }) { ingredient ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .border(2.dp, TintaComic, RoundedCornerShape(10.dp)),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = BlancoPuro)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = ingredient.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = TintaComic
                            )
                            Text(
                                text = ingredient.category,
                                fontSize = 11.sp,
                                color = NaranjaCoral,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "$${ingredient.price.toInt()} MXN",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = VerdeFresco
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            // Botón para eliminar el ingrediente de la base de datos
                            IconButton(
                                onClick = { viewModel.removeIngredient(ingredient) },
                                modifier = Modifier
                                    .size(32.dp)
                                    .border(1.5.dp, TintaComic, RoundedCornerShape(8.dp))
                                    .background(NaranjaCoral)
                            ) {
                                Text(text = "✕", color = BlancoPuro, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }

    // Diálogo emergente para ingresar nuevos insumos
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            modifier = Modifier.border(2.dp, TintaComic, RoundedCornerShape(16.dp)),
            containerColor = FondoCrema,
            title = {
                Text(text = "Agregar insumo a despensa", fontWeight = FontWeight.Bold, color = TintaComic)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("Nombre (ej. Pollo, Huevo)") },
                        shape = RoundedCornerShape(8.dp)
                    )
                    OutlinedTextField(
                        value = newPrice,
                        onValueChange = { newPrice = it },
                        label = { Text("Precio en MXN") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val priceParsed = newPrice.toDoubleOrNull() ?: 0.0
                        if (newName.isNotBlank()) {
                            viewModel.saveIngredient(newName, "Abarrotes / General", priceParsed)
                            newName = ""
                            newPrice = ""
                            showAddDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VerdeFresco)
                ) {
                    Text("Guardar", color = BlancoPuro, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancelar", color = TintaComic, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}