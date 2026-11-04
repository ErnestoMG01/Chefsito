package com.example.chefsito.data.repository

import com.example.chefsito.data.local.entity.IngredientEntity
import kotlinx.coroutines.flow.Flow

interface IngredientRepository {
    fun getAllIngredients(): Flow<List<IngredientEntity>>
    suspend fun getIngredientById(id: Long): IngredientEntity?
    suspend fun insertIngredient(ingredient: IngredientEntity)
    suspend fun updateIngredient(ingredient: IngredientEntity)
    suspend fun deleteIngredient(ingredient: IngredientEntity)
}