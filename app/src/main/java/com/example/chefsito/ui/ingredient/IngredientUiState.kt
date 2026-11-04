package com.example.chefsito.ui.ingredient

import com.example.chefsito.data.local.entity.IngredientEntity

data class IngredientUiState(
    val ingredients: List<IngredientEntity> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)