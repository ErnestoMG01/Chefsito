package com.example.chefsito.ui.recipe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.chefsito.data.local.entity.RecipeEntity
import com.example.chefsito.data.repository.AiRecipeRepository
import com.example.chefsito.data.repository.RecipeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RecipeViewModel(
    private val recipeRepository: RecipeRepository,
    private val aiRecipeRepository: AiRecipeRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    private val _isGeneratingRecipe = MutableStateFlow(false)
    private val _error = MutableStateFlow<String?>(null)

    val uiState: StateFlow<RecipeUiState> = combine(
        recipeRepository.getAllRecipes(),
        recipeRepository.getFavoriteRecipes(),
        _isLoading,
        _isGeneratingRecipe,
        _error
    ) { allRecipes, favoriteRecipes, isLoading, isGenerating, error ->
        RecipeUiState(
            recipes = allRecipes,
            favoriteRecipes = favoriteRecipes,
            isLoading = isLoading,
            isGeneratingRecipe = isGenerating,
            error = error
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = RecipeUiState(isLoading = true)
    )

    fun addRecipe(recipe: RecipeEntity) {
        viewModelScope.launch {
            recipeRepository.insertRecipe(recipe)
        }
    }

    fun updateRecipe(recipe: RecipeEntity) {
        viewModelScope.launch {
            recipeRepository.updateRecipe(recipe)
        }
    }

    fun deleteRecipe(recipe: RecipeEntity) {
        viewModelScope.launch {
            recipeRepository.deleteRecipe(recipe)
        }
    }

    fun generateAiRecipe() {
        viewModelScope.launch {
            _isGeneratingRecipe.value = true
            _error.value = null
            
            val result = aiRecipeRepository.generateAndSaveRecipeFromPantry()
            
            result.onFailure { exception ->
                _error.value = exception.message ?: "Ocurrió un error al generar la receta"
            }
            
            _isGeneratingRecipe.value = false
        }
    }

    fun clearError() {
        _error.value = null
    }
}

class RecipeViewModelFactory(
    private val recipeRepository: RecipeRepository,
    private val aiRecipeRepository: AiRecipeRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RecipeViewModel::class.java)) {
            return RecipeViewModel(recipeRepository, aiRecipeRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
