package com.example.chefsito.data.remote

import com.example.chefsito.data.remote.dto.AiRecipeDto

interface AiRecipeService {
    suspend fun generateRecipe(ingredients: List<String>): Result<AiRecipeDto>
}
