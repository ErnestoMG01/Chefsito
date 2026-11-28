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

    /** Alterna el estado de favorito para el usuario activo (local + sincronización AWS). */
    suspend fun toggleFavoriteForCurrentUser(recipeId: Long)

    /**
     * Sincroniza el catálogo local de Room con la base de datos desplegada en AWS RDS
     * y opcionalmente dispara la ingesta dinámica de nuevas recetas desde proveedores externos.
     *
     * @return Número total de recetas disponibles tras la sincronización.
     */
    suspend fun syncWithCloudCatalog(fetchNewExternal: Boolean = false): Result<Int>

    /**
     * Crea una receta personalizada y la publica tanto en Room como en el servidor AWS
     * para que el catálogo siempre pueda recibir nuevas recetas actualizadas.
     */
    suspend fun createDynamicRecipe(
        title: String,
        description: String,
        mealType: String,
        dayOfWeek: String,
        prepTimeMinutes: Int,
        difficulty: String,
        rawIngredientsText: String,
        rawStepsText: String
    ): Result<RecipeEntity>

    /**
     * Motor determinista de recomendación.
     */
    suspend fun recommendRecipes(
        pantry: List<IngredientEntity>,
        budget: Double,
        dayOfWeek: String? = null
    ): List<RecipeCostBreakdown>

    /**
     * Devuelve varias alternativas por tipo de comida (Desayuno, Comida y Cena).
     */
    suspend fun recommendRecipeOptions(
        pantry: List<IngredientEntity>,
        budget: Double,
        dayOfWeek: String? = null,
        maxPerMeal: Int = 3
    ): List<RecipeCostBreakdown>

    /** Carga el catálogo base si la tabla está vacía y sincroniza con AWS si está disponible. */
    suspend fun seedIfEmpty()
}
