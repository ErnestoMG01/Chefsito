package com.example.chefsito.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Ingrediente de la despensa del usuario.
 *
 * @property userId Identificador del usuario propietario de este insumo (aislamiento multiusuario).
 * @property userCost Costo que el usuario pagó realmente por el ingrediente.
 * No es un precio estático del catálogo: lo captura el propio usuario.
 * @property isInPantry True si lo tiene en casa (disponible para cocinar).
 */
@Entity(
    tableName = "ingredients",
    indices = [Index(value = ["userId", "name"])]
)
data class IngredientEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val userId: Long = 0L,
    val name: String,
    val category: String, // "Proteínas", "Vegetales", "Abarrotes", etc.
    val userCost: Double,
    val isInPantry: Boolean = true
)
