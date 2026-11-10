package com.example.chefsito.ui.recipe

import com.example.chefsito.data.domain.RecipeCostBreakdown
import com.example.chefsito.data.local.entity.RecipeEntity

data class RecipeUiState(
    val recipes: List<RecipeEntity> = emptyList(),
    val favoriteRecipes: List<RecipeEntity> = emptyList(),
    val isLoading: Boolean = false,
    val isGeneratingRecipe: Boolean = false,
    val recommendations: List<RecipeCostBreakdown> = emptyList(),
    val selectedDay: String = "",
    val message: String? = null,
    val error: String? = null
)
