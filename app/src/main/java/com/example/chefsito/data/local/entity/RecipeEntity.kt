package com.example.chefsito.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Receta del catálogo local y sincronizado desde la nube AWS de Chefsito.
 *
 * @property description Descripción general del platillo (pantalla de resumen).
 * @property mealType Tipo de comida: "Desayuno", "Comida" o "Cena".
 * @property dayOfWeek Día sugerido de la semana (Lunes..Domingo).
 * @property ingredients Ingredientes separados por saltos de línea con formato
 * `"Nombre|CostoEstimado"`. El costo estimado se usa SOLO cuando el usuario no
 * tiene ese ingrediente en su despensa.
 * @property steps Instrucciones paso a paso separadas por saltos de línea.
 * @property source Origen de la receta (`SEED_MEXICANO`, `AWS_CLOUD_CATALOG`, `THEMEALDB_LIVE`, etc.).
 * @property externalId Identificador único remoto para sincronización incremental sin duplicados.
 */
@Entity(tableName = "recipes")
data class RecipeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val title: String,
    val estimatedPrice: Double,
    val prepTimeMinutes: Int,
    val difficulty: String = "Fácil",
    val isFavorite: Boolean = false,
    val description: String = "",
    val mealType: String = "Comida",
    val dayOfWeek: String = "",
    val ingredients: String = "",
    val steps: String = "",
    val source: String = "SEED_MEXICANO",
    val externalId: String = ""
)
