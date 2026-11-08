package com.example.chefsito.ui.recipe

import com.example.chefsito.data.local.entity.RecipeEntity

data class RecipeUiState(
    val recipes: List<RecipeEntity> = emptyList(),
    val favoriteRecipes: List<RecipeEntity> = emptyList(),
    val isLoading: Boolean = false,
    val isGeneratingRecipe: Boolean = false,
    val error: String? = null
)
