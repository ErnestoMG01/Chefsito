package com.example.chefsito.ui.recipe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.chefsito.data.domain.RecipeCostBreakdown
import com.example.chefsito.data.local.entity.RecipeEntity
import com.example.chefsito.data.repository.IngredientRepository
import com.example.chefsito.data.repository.RecipeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RecipeViewModel(
    private val recipeRepository: RecipeRepository,
    private val ingredientRepository: IngredientRepository
) : ViewModel() {

    private val _isGeneratingRecipe = MutableStateFlow(false)
    private val _recommendations = MutableStateFlow<List<RecipeCostBreakdown>>(emptyList())
    private val _selectedDay = MutableStateFlow("")
    private val _message = MutableStateFlow<String?>(null)
    private val _error = MutableStateFlow<String?>(null)

    private val baseState = combine(
        recipeRepository.getAllRecipes(),
        recipeRepository.getFavoriteRecipes(),
        _isGeneratingRecipe,
        _recommendations,
        _selectedDay
    ) { allRecipes, favoriteRecipes, isGenerating, recommendations, selectedDay ->
        RecipeUiState(
            recipes = allRecipes,
            favoriteRecipes = favoriteRecipes,
            isLoading = false,
            isGeneratingRecipe = isGenerating,
            recommendations = recommendations,
            selectedDay = selectedDay
        )
    }

    val uiState: StateFlow<RecipeUiState> = combine(
        baseState,
        _message,
        _error
    ) { state, message, error ->
        state.copy(message = message, error = error)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = RecipeUiState(isLoading = true)
    )

    fun addRecipe(recipe: RecipeEntity) {
        viewModelScope.launch { recipeRepository.insertRecipe(recipe) }
    }

    fun updateRecipe(recipe: RecipeEntity) {
        viewModelScope.launch { recipeRepository.updateRecipe(recipe) }
    }

    fun deleteRecipe(recipe: RecipeEntity) {
        viewModelScope.launch { recipeRepository.deleteRecipe(recipe) }
    }

    /** Alterna el corazón de una receta y la agrega/quita de Favoritos. */
    fun toggleFavorite(recipeId: Long) {
        viewModelScope.launch {
            val recipe = recipeRepository.getRecipeById(recipeId) ?: return@launch
            recipeRepository.updateRecipe(recipe.copy(isFavorite = !recipe.isFavorite))
        }
    }

    /**
     * Genera el plan del día seleccionado usando el motor determinista:
     * - Suma los costos reales (`userCost`) de lo que el usuario ya tiene.
     * - Suma costos estimados de los ingredientes faltantes.
     * - Compara el total contra el presupuesto del día.
     */
    fun generateRecipe(
        budget: Double = 100.0,
        planType: String = "Día",
        dayOfWeek: String? = null
    ) {
        viewModelScope.launch {
            _isGeneratingRecipe.value = true
            _error.value = null
            _message.value = null

            try {
                if (budget <= 0.0) {
                    _error.value = "El presupuesto debe ser mayor a $0.00"
                    return@launch
                }

                val pantry = ingredientRepository.getAllIngredients()
                    .first()
                    .filter { it.isInPantry }

                // En plan semanal el presupuesto se reparte entre 7 días.
                val effectiveBudget = if (planType == "Semanal") budget / 7.0 else budget

                val recommendations = recipeRepository.recommendRecipes(
                    pantry = pantry,
                    budget = effectiveBudget,
                    dayOfWeek = dayOfWeek
                )

                if (recommendations.isEmpty()) {
                    _recommendations.value = emptyList()
                    _error.value = "No encontramos recetas para ese día. Intenta con otro día."
                    return@launch
                }

                _recommendations.value = recommendations
                _selectedDay.value = dayOfWeek.orEmpty()

                val missingCount = recommendations.sumOf { it.missing.size }
                _message.value = buildString {
                    append("Plan de ${dayOfWeek ?: "hoy"}: ")
                    append("${recommendations.size} comidas")
                    if (missingCount == 0) {
                        append(" · ¡Tienes todo en casa!")
                    } else {
                        append(" · te faltan $missingCount ingrediente(s)")
                    }
                    append(" · presupuesto por día: $${effectiveBudget.toInt()}")
                }
            } catch (e: Exception) {
                _error.value = "No se pudo generar el plan: ${e.message ?: "error desconocido"}"
            } finally {
                _isGeneratingRecipe.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun clearMessage() {
        _message.value = null
    }
}

class RecipeViewModelFactory(
    private val recipeRepository: RecipeRepository,
    private val ingredientRepository: IngredientRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RecipeViewModel::class.java)) {
            return RecipeViewModel(recipeRepository, ingredientRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
