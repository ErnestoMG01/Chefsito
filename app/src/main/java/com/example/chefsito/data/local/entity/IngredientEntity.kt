package com.example.chefsito.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ingredients")
data class IngredientEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val category: String, // "Proteínas", "Vegetales", "Abarrotes", etc.
    val price: Double,
    val isInPantry: Boolean = true // True si está en casa, false si es faltante
)