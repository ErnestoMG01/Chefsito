package com.example.chefsito.data.repository

import com.example.chefsito.data.domain.RecipeCostBreakdown
import com.example.chefsito.data.domain.RecipeCosting
import com.example.chefsito.data.local.RecipeSeed
import com.example.chefsito.data.local.dao.RecipeDao
import com.example.chefsito.data.local.entity.IngredientEntity
import com.example.chefsito.data.local.entity.RecipeEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class RecipeRepositoryImpl(
    private val recipeDao: RecipeDao
) : RecipeRepository {

    override fun getAllRecipes(): Flow<List<RecipeEntity>> = recipeDao.getAllRecipes()

    override fun getFavoriteRecipes(): Flow<List<RecipeEntity>> = recipeDao.getFavoriteRecipes()

    override suspend fun getRecipeById(id: Long): RecipeEntity? = recipeDao.getRecipeById(id)

    override suspend fun getRecipeCount(): Int = recipeDao.getRecipeCount()

    override suspend fun insertRecipe(recipe: RecipeEntity) = recipeDao.insertRecipe(recipe)

    override suspend fun updateRecipe(recipe: RecipeEntity) = recipeDao.updateRecipe(recipe)

    override suspend fun deleteRecipe(recipe: RecipeEntity) = recipeDao.deleteRecipe(recipe)

    override suspend fun recommendRecipes(
        pantry: List<IngredientEntity>,
        budget: Double,
        dayOfWeek: String?
    ): List<RecipeCostBreakdown> {
        val catalog = recipeDao.getAllRecipes().first()
        if (catalog.isEmpty()) return emptyList()

        val normalizedDay = dayOfWeek?.let { RecipeCosting.normalize(it) }
        val breakdowns = catalog.map { recipe ->
            RecipeCosting.breakdown(recipe, pantry, budget)
        }

        val mealOrder = listOf("Desayuno", "Comida", "Cena")

        return mealOrder.mapNotNull { meal ->
            val candidates = breakdowns.filter {
                RecipeCosting.normalize(it.recipe.mealType) == RecipeCosting.normalize(meal)
            }
            if (candidates.isEmpty()) return@mapNotNull null

            // Primero las recetas asignadas explícitamente a ese día.
            val assignedToDay = if (normalizedDay.isNullOrBlank()) {
                emptyList()
            } else {
                candidates.filter {
                    RecipeCosting.normalize(it.recipe.dayOfWeek) == normalizedDay
                }
            }
            val pool = if (assignedToDay.isNotEmpty()) assignedToDay else candidates

            pool.sortedWith(
                compareByDescending<RecipeCostBreakdown> { it.matchCount }
                    .thenByDescending { it.fitsBudget }
                    .thenBy { it.missingCost }
                    .thenBy { it.totalCost }
                    .thenBy { it.recipe.title }
            ).firstOrNull()
        }
    }

    override suspend fun seedIfEmpty() {
        if (recipeDao.getRecipeCount() == 0) {
            recipeDao.insertRecipes(RecipeSeed.recipes)
        }
    }
}
