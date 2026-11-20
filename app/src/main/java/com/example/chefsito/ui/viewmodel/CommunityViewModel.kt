package com.example.chefsito.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chefsito.data.domain.RecipeCosting
import com.example.chefsito.data.local.RecipeSeed
import com.example.chefsito.data.remote.CommunityRecipe
import com.example.chefsito.data.remote.CommunityRecipeService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Estados de la sección "Recetas de la Comunidad".
 * - Loading: consultando la API.
 * - Success: datos reales recibidos de DummyJSON.
 * - Error: sin conexión o respuesta inválida; se muestra el catálogo local.
 */
sealed interface CommunityUiState {
    data object Loading : CommunityUiState
    data class Success(val recipes: List<CommunityRecipe>) : CommunityUiState
    data class Error(
        val message: String,
        val fallback: List<CommunityRecipe>
    ) : CommunityUiState
}

class CommunityViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<CommunityUiState>(CommunityUiState.Loading)
    val uiState: StateFlow<CommunityUiState> = _uiState.asStateFlow()

    private val _selectedRecipe = MutableStateFlow<CommunityRecipe?>(null)
    val selectedRecipe: StateFlow<CommunityRecipe?> = _selectedRecipe.asStateFlow()

    init {
        loadRecipes()
    }

    /** Guarda la receta de la API que el usuario quiere consultar en detalle. */
    fun selectRecipe(recipe: CommunityRecipe) {
        _selectedRecipe.value = recipe
    }

    fun clearSelection() {
        _selectedRecipe.value = null
    }

    fun loadRecipes() {
        viewModelScope.launch {
            _uiState.value = CommunityUiState.Loading
            runCatching { CommunityRecipeService.fetchRecipes() }
                .onSuccess { recipes ->
                    _uiState.value = if (recipes.isEmpty()) {
                        CommunityUiState.Error(
                            message = "La API respondió sin recetas. Mostrando catálogo local.",
                            fallback = localFallback()
                        )
                    } else {
                        CommunityUiState.Success(recipes)
                    }
                }
                .onFailure { error ->
                    _uiState.value = CommunityUiState.Error(
                        message = error.message ?: "No fue posible conectar con la API.",
                        fallback = localFallback()
                    )
                }
        }
    }

    fun retry() = loadRecipes()

    /** Fallback local para demostrar la UI aun sin internet. */
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
