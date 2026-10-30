package com.example.chefsito.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recipes")
data class RecipeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val title: String,
    val estimatedPrice: Double,
    val prepTimeMinutes: Int,
    val isFavorite: Boolean = false,
    val steps: String // Puedes guardar los pasos separados por saltos de línea o JSON
)