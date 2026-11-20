package com.example.chefsito.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Ingrediente de la despensa del usuario.
 *
 * @property userCost Costo que el usuario pagó realmente por el ingrediente.
 * @property isInPantry True si lo tiene en casa.
 */
@Entity(tableName = "ingredients")
data class IngredientEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val category: String, // "Proteínas", "Vegetales", "Abarrotes", etc.
    val userCost: Double = 0.0,
    val isInPantry: Boolean = true
) {
    val price: Double get() = userCost
}
