package com.example.chefsito.domain.model

import com.example.chefsito.data.remote.CommunityRecipe

/**
 * Estados de UI para la sección de recetas de la comunidad.
 * Separa la lógica de presentación de la lógica de datos.
 */
sealed interface CommunityRecipesUiState {
    data object Loading : CommunityRecipesUiState
    data class Success(val recipes: List<CommunityRecipe>) : CommunityRecipesUiState
    data class Error(val message: String, val fallback: List<CommunityRecipe>) : CommunityRecipesUiState
}