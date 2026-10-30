package com.example.chefsito.ui.screens.pantry

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chefsito.data.local.entity.IngredientEntity
import com.example.chefsito.ui.components.ChefsitoBackButton
import com.example.chefsito.ui.components.ChefsitoConfirmDialog
import com.example.chefsito.ui.components.ChefsitoIconButton
import com.example.chefsito.ui.components.ChefsitoMessageBanner
import com.example.chefsito.ui.components.MessageType
import com.example.chefsito.ui.theme.*
import com.example.chefsito.ui.viewmodel.ChefViewModel

@Composable
fun PantryScreen(
    viewModel: ChefViewModel,
    modifier: Modifier = Modifier,
    onNavigatePlan: () -> Unit = {},
) {
    val pantryIngredients by viewModel.pantryIngredients.collectAsState()

    // Estados locales para el diálogo de agregar insumo
    var showAddDialog by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }
    var newPrice by remember { mutableStateOf("") }
    var dialogError by remember { mutableStateOf<String?>(null) }

    // Estados para controlar la edición de un ingrediente existente
    var showEditDialog by remember { mutableStateOf(false) }
    var selectedIngredient by remember { mutableStateOf<IngredientEntity?>(null) }
    var editPriceText by remember { mutableStateOf("") }

    // Estados para el diálogo de confirmación de borrado
    var showDeleteDialog by remember { mutableStateOf(false) }
    var ingredientToDelete by remember { mutableStateOf<IngredientEntity?>(null) }

    // Estados para el banner de mensajes (feedback)
    var feedbackMessage by remember { mutableStateOf<String?>(null) }
    var feedbackTitle by remember { mutableStateOf("") }
    var feedbackType by remember { mutableStateOf(MessageType.SUCCESS) }

    LaunchedEffect(feedbackMessage) {
        if (feedbackMessage != null) {
            kotlinx.coroutines.delay(3000)
            feedbackMessage = null
        }
    }

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
                ChefsitoBackButton(
                    text = "Configurar Plan",
                    onBackClick = onNavigatePlan
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (feedbackMessage != null) {
                    ChefsitoMessageBanner(
                        title = feedbackTitle,
                        message = feedbackMessage!!,
                        type = feedbackType
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

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

                Button(
                    onClick = {
                        dialogError = null
                        showAddDialog = true
                    },
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
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
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
                            TextButton(onClick = {
                                selectedIngredient = ingredient
                                editPriceText = ingredient.price.toString()
                                showEditDialog = true
                            }) {
                                Text(
                                    text = "$${ingredient.price.toInt()} MXN",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = VerdeFresco
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            ChefsitoIconButton(
                                onClick = {
                                    ingredientToDelete = ingredient
                                    showDeleteDialog = true
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = {
                dialogError = null
                showAddDialog = false
            },
            modifier = Modifier.border(2.dp, TintaComic, RoundedCornerShape(16.dp)),
            containerColor = FondoCrema,
            title = {
                Text(text = "Agregar insumo a despensa", fontWeight = FontWeight.Bold, color = TintaComic)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {

                    if (dialogError != null) {
                        ChefsitoMessageBanner(
                            title = "ERROR",
                            message = dialogError!!,
                            type = MessageType.WARNING
                        )
                    }

                    OutlinedTextField(
                        value = newName,
                        onValueChange = {
                            newName = it
                            dialogError = null
                        },
                        label = { Text("Nombre (ej. Pollo, Huevo)") },
                        singleLine = true,
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
                        val trimmedName = newName.trim()
                        val priceParsed = newPrice.toDoubleOrNull() ?: 0.0

                        when {
                            trimmedName.isBlank() -> {
                                dialogError = "El nombre del ingrediente no puede estar vacío."
                            }
                            pantryIngredients.any {
                                it.name.equals(trimmedName, ignoreCase = true)
                            } -> {
                                dialogError = "Ese ingrediente ya está en tu despensa."
                            }
                            else -> {
                                viewModel.saveIngredient(trimmedName, "Abarrotes / General", priceParsed)

                                feedbackTitle = "ÉXITO"
                                feedbackMessage = "$trimmedName se agregó a tu despensa."
                                feedbackType = MessageType.SUCCESS

                                newName = ""
                                newPrice = ""
                                dialogError = null
                                showAddDialog = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VerdeFresco)
                ) {
                    Text("Guardar", color = BlancoPuro, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    dialogError = null
                    showAddDialog = false
                }) {
                    Text("Cancelar", color = TintaComic, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    if (showEditDialog && selectedIngredient != null) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            modifier = Modifier.border(2.dp, TintaComic, RoundedCornerShape(16.dp)),
            containerColor = FondoCrema,
            title = {
                Text(text = "Editar precio de ${selectedIngredient?.name}", fontWeight = FontWeight.Bold, color = TintaComic)
            },
            text = {
                OutlinedTextField(
                    value = editPriceText,
                    onValueChange = { editPriceText = it },
                    label = { Text("Nuevo precio en MXN") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val current = selectedIngredient
                        if (current != null) {
                            val newPriceParsed = editPriceText.toDoubleOrNull() ?: current.price
                            viewModel.saveIngredient(
                                name = current.name,
                                category = current.category,
                                price = newPriceParsed,
                                id = current.id
                            )
                            feedbackTitle = "ACTUALIZADO"
                            feedbackMessage = "Se actualizó el precio de ${current.name}."
                            feedbackType = MessageType.SUCCESS
                        }
                        showEditDialog = false
                        selectedIngredient = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VerdeFresco)
                ) {
                    Text("Actualizar", color = BlancoPuro, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Cancelar", color = TintaComic, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    if (showDeleteDialog && ingredientToDelete != null) {
        ChefsitoConfirmDialog(
            title = "¿Borrar ${ingredientToDelete?.name}?",
            message = "También se eliminará su precio de tu despensa.",
            confirmText = "Borrar",
            dismissText = "Cancelar",
            onConfirm = {
                viewModel.removeIngredient(ingredientToDelete!!)
                feedbackTitle = "ELIMINADO"
                feedbackMessage = "${ingredientToDelete?.name} se borró de tu despensa."
                feedbackType = MessageType.DELETE
                showDeleteDialog = false
                ingredientToDelete = null
            },
            onDismiss = {
                showDeleteDialog = false
                ingredientToDelete = null
            }
        )
    }
}