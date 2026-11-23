package com.example.chefsito.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chefsito.data.remote.CommunityRecipe
import com.example.chefsito.domain.model.CommunityRecipesUiState
import com.example.chefsito.domain.usecase.GetCommunityRecipesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
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

class CommunityViewModel(
    private val getCommunityRecipesUseCase: GetCommunityRecipesUseCase
) : ViewModel() {

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
            getCommunityRecipesUseCase().collect { state ->
                _uiState.value = when (state) {
                    is CommunityRecipesUiState.Loading -> CommunityUiState.Loading
                    is CommunityRecipesUiState.Success -> CommunityUiState.Success(state.recipes)
                    is CommunityRecipesUiState.Error -> CommunityUiState.Error(state.message, state.fallback)
                }
            }
        }
    }

    fun retry() = loadRecipes()

    /** Recarga forzada desde la API. */
    fun refresh() {
        viewModelScope.launch {
            getCommunityRecipesUseCase.refresh()
            loadRecipes()
        }
    }
}
