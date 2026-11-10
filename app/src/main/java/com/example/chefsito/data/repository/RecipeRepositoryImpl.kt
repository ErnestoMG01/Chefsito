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
    ): List<RecipeCostBreakdown> =
        recommendRecipeOptions(
            pantry = pantry,
            budget = budget,
            dayOfWeek = dayOfWeek,
            maxPerMeal = 1
        )

    override suspend fun recommendRecipeOptions(
        pantry: List<IngredientEntity>,
        budget: Double,
        dayOfWeek: String?,
        maxPerMeal: Int
    ): List<RecipeCostBreakdown> {
        val catalog = recipeDao.getAllRecipes().first()
        if (catalog.isEmpty()) return emptyList()

        val normalizedDay = dayOfWeek?.let { RecipeCosting.normalize(it) }
        val breakdowns = catalog.map { recipe ->
            RecipeCosting.breakdown(recipe, pantry, budget)
        }

        val mealOrder = listOf("Desayuno", "Comida", "Cena")
        val limit = maxPerMeal.coerceAtLeast(1)

        return mealOrder.flatMap { meal ->
            val candidates = breakdowns.filter {
                RecipeCosting.normalize(it.recipe.mealType) == RecipeCosting.normalize(meal)
            }
            if (candidates.isEmpty()) return@flatMap emptyList()

            // Primero las recetas asignadas explícitamente a ese día; después el resto
            // del catálogo para garantizar variedad cuando haya pocas asignadas.
            val assignedToDay = if (normalizedDay.isNullOrBlank()) {
                emptyList()
            } else {
                candidates.filter {
                    RecipeCosting.normalize(it.recipe.dayOfWeek) == normalizedDay
                }
            }
            val assignedIds = assignedToDay.map { it.recipe.id }.toSet()

            val comparator = compareByDescending<RecipeCostBreakdown> { it.matchCount }
                .thenByDescending { it.fitsBudget }
                .thenBy { it.missingCost }
                .thenBy { it.totalCost }
                .thenBy { it.recipe.title }

            // Se conserva primero lo asignado al día y luego el resto del catálogo,
            // cada grupo ordenado con la misma prioridad.
            val orderedAssigned = assignedToDay.sortedWith(comparator)
            val orderedRest = candidates
                .filter { it.recipe.id !in assignedIds }
                .sortedWith(comparator)

            (orderedAssigned + orderedRest)
                .distinctBy { it.recipe.id }
                .take(limit)
        }
    }

    override suspend fun seedIfEmpty() {
        if (recipeDao.getRecipeCount() == 0) {
            recipeDao.insertRecipes(RecipeSeed.recipes)
        }
    }
}
