package com.example.chefsito.data.repository

import com.example.chefsito.data.local.entity.RecipeEntity
import com.example.chefsito.data.remote.AiRecipeService
import kotlinx.coroutines.flow.first

class AiRecipeRepository(
    private val ingredientRepository: IngredientRepository,
    private val aiRecipeService: AiRecipeService,
    private val recipeRepository: RecipeRepository
) {
    suspend fun generateAndSaveRecipeFromPantry(): Result<RecipeEntity> {
        return try {
            // 1. Obtener ingredientes actuales de la despensa
            val allIngredients = ingredientRepository.getAllIngredients().first()
            val pantryIngredients = allIngredients.filter { it.isInPantry }
            
            if (pantryIngredients.isEmpty()) {
                return Result.failure(Exception("No hay ingredientes en la despensa para generar una receta."))
            }

            val ingredientNames = pantryIngredients.map { it.name }

            // 2. Llamar al servicio de Inteligencia Artificial
            val aiResponse = aiRecipeService.generateRecipe(ingredientNames).getOrThrow()

            // 3. Mapear la respuesta DTO a una Entidad de Room y guardarla localmente
            val recipeEntity = RecipeEntity(
                title = aiResponse.title,
                estimatedPrice = aiResponse.estimatedPrice,
                prepTimeMinutes = aiResponse.prepTimeMinutes,
                steps = aiResponse.steps
            )

            recipeRepository.insertRecipe(recipeEntity)
            Result.success(recipeEntity)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
