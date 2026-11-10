package com.example.chefsito.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Ingrediente de la despensa del usuario.
 *
 * @property userCost Costo que el usuario pagó realmente por el ingrediente.
 * No es un precio estático del catálogo: lo captura el propio usuario.
 * @property isInPantry True si lo tiene en casa (disponible para cocinar).
 */
@Entity(tableName = "ingredients")
data class IngredientEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val category: String, // "Proteínas", "Vegetales", "Abarrotes", etc.
    val userCost: Double,
    val isInPantry: Boolean = true
)
