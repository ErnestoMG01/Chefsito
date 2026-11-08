package com.example.chefsito.data.repository

import com.example.chefsito.data.local.entity.RecipeEntity
import kotlinx.coroutines.flow.Flow

interface RecipeRepository {
    fun getAllRecipes(): Flow<List<RecipeEntity>>
    fun getFavoriteRecipes(): Flow<List<RecipeEntity>>
    suspend fun getRecipeById(id: Long): RecipeEntity?
    suspend fun insertRecipe(recipe: RecipeEntity)
    suspend fun updateRecipe(recipe: RecipeEntity)
    suspend fun deleteRecipe(recipe: RecipeEntity)
}
