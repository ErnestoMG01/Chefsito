package com.example.chefsito.data.repository

import com.example.chefsito.data.domain.RecipeCosting
import com.example.chefsito.data.local.RecipeSeed
import com.example.chefsito.data.remote.CommunityApiService
import com.example.chefsito.data.remote.CommunityRecipe
import com.example.chefsito.data.remote.mapper.CommunityRecipeMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Implementación del repositorio de recetas de la comunidad.
 * Orquesta: API externa → éxito → datos reales | error → fallback local (RecipeSeed).
 * 
 * No expone detalles de implementación (DTOs, mappers) a la capa superior.
 */
class CommunityRepositoryImpl(
    private val apiService: CommunityApiService
) : CommunityRepository {

    override suspend fun getCommunityRecipes(): Result<List<CommunityRecipe>> = withContext(Dispatchers.IO) {
        try {
            val dtos = apiService.fetchRecipes()
            if (dtos.isNotEmpty()) {
                Result.success(CommunityRecipeMapper.toDomainList(dtos))
            } else {
                // API respondió vacío → fallback local
                Result.success(localFallback())
            }
        } catch (e: Exception) {
            // Error de red, timeout, parsing, etc. → fallback local
            Result.success(localFallback())
        }
    }

    override suspend fun refreshCommunityRecipes(): Result<List<CommunityRecipe>> = withContext(Dispatchers.IO) {
        // Igual que getCommunityRecipes pero siempre intenta la API primero
        // (podría agregarse cache con timestamp si se requiere)
        getCommunityRecipes()
    }

    /**
     * Fallback local usando el catálogo de RecipeSeed (recetas mexicanas pre-cargadas).
     * Convierte las recetas locales al modelo CommunityRecipe para consistencia en la UI.
     */
    private fun localFallback(): List<CommunityRecipe> =
        RecipeSeed.recipes.take(6).mapIndexed { index, recipe ->
            CommunityRecipe(
                id = 9_000 + index,
                name = recipe.title,
                cuisine = "Mexicana (local)",
                difficulty = recipe.difficulty,
                prepTimeMinutes = recipe.prepTimeMinutes,
                ingredients = RecipeCosting.parseIngredients(recipe).map { it.name },
                instructions = RecipeCosting.parseSteps(recipe),
                imageUrl = ""
            )
        }
}