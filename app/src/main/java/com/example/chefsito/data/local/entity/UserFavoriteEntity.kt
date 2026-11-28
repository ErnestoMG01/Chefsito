package com.example.chefsito.data.local.entity

import androidx.room.Entity

/**
 * Tabla relacional de favoritos por usuario (soporte multiusuario local y sincronizado con AWS).
 */
@Entity(
    tableName = "user_favorites",
    primaryKeys = ["userId", "recipeId"]
)
data class UserFavoriteEntity(
    val userId: Long,
    val recipeId: Long,
    val createdAt: Long = System.currentTimeMillis()
)
