package com.example.chefsito.data.repository

import com.example.chefsito.data.domain.RecipeCostBreakdown
import com.example.chefsito.data.local.entity.IngredientEntity
import com.example.chefsito.data.local.entity.RecipeEntity
import kotlinx.coroutines.flow.Flow

interface RecipeRepository {
    fun getAllRecipes(): Flow<List<RecipeEntity>>
    fun getFavoriteRecipes(): Flow<List<RecipeEntity>>
    suspend fun getRecipeById(id: Long): RecipeEntity?
    suspend fun getRecipeCount(): Int
    suspend fun insertRecipe(recipe: RecipeEntity)
    suspend fun updateRecipe(recipe: RecipeEntity)
    suspend fun deleteRecipe(recipe: RecipeEntity)

    /**
     * Motor determinista de recomendación.
     *
     * Para el día seleccionado elige, por cada tipo de comida, la receta que:
     * 1. Tiene más ingredientes que el usuario ya posee.
     * 2. Cabe mejor en el presupuesto disponible.
     * 3. Requiere menor costo de ingredientes faltantes.
     */
    suspend fun recommendRecipes(
        pantry: List<IngredientEntity>,
        budget: Double,
        dayOfWeek: String? = null
    ): List<RecipeCostBreakdown>

    /** Carga el catálogo base si la tabla está vacía. */
    suspend fun seedIfEmpty()
}
