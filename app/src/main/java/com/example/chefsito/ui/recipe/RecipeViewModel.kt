package com.example.chefsito.ui.recipe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.chefsito.data.domain.RecipeCostBreakdown
import com.example.chefsito.data.local.entity.RecipeEntity
import com.example.chefsito.data.repository.RecipeRepository
import com.example.chefsito.domain.model.RecipePlanResult
import com.example.chefsito.ui.recipe.DayPlan
import com.example.chefsito.ui.recipe.MealOptionGroup
import com.example.chefsito.domain.usecase.GenerateRecipePlanUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Estado interno del plan para no encadenar demasiados flujos en un solo combine. */
private data class PlanSnapshot(
    val planType: String = "Día",
    val recommendations: List<RecipeCostBreakdown> = emptyList(),
    val mealOptions: List<MealOptionGroup> = emptyList(),
    val weeklyPlans: List<DayPlan> = emptyList(),
    val selectedDay: String = ""
)

class RecipeViewModel(
    private val recipeRepository: RecipeRepository,
    private val generateRecipePlanUseCase: GenerateRecipePlanUseCase
) : ViewModel() {

    private val _isGeneratingRecipe = MutableStateFlow(false)
    private val _planState = MutableStateFlow(PlanSnapshot())
    private val _message = MutableStateFlow<String?>(null)
    private val _error = MutableStateFlow<String?>(null)

    private val baseState = combine(
        recipeRepository.getAllRecipes(),
        recipeRepository.getFavoriteRecipes(),
        _isGeneratingRecipe,
        _planState
    ) { allRecipes, favoriteRecipes, isGenerating, plan ->
        RecipeUiState(
            recipes = allRecipes,
            favoriteRecipes = favoriteRecipes,
            isLoading = false,
            isGeneratingRecipe = isGenerating,
            planType = plan.planType,
            recommendations = plan.recommendations,
            mealOptions = plan.mealOptions,
            weeklyPlans = plan.weeklyPlans,
            selectedDay = plan.selectedDay
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
     * Genera el plan de "Día" o "Semana" usando el caso de uso.
     * Delega toda la lógica compleja a GenerateRecipePlanUseCase.
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

            val result = generateRecipePlanUseCase.execute(budget, planType, dayOfWeek)

            try {
                if (result.isSuccess) {
                    val planResult: RecipePlanResult = result.getOrNull()!!
                    
                    // Convert domain models to UI models
                    val uiMealOptions = planResult.mealOptions.map { domainGroup ->
                        MealOptionGroup(domainGroup.mealType, domainGroup.options)
                    }
                    val uiWeeklyPlans = planResult.weeklyPlans.map { domainPlan ->
                        DayPlan(
                            domainPlan.dayOfWeek,
                            domainPlan.budget,
                            domainPlan.options.map { domainGroup ->
                                MealOptionGroup(domainGroup.mealType, domainGroup.options)
                            }
                        )
                    }

                    _planState.value = PlanSnapshot(
                        planType = planResult.planType,
                        recommendations = planResult.recommendations,
                        mealOptions = uiMealOptions,
                        weeklyPlans = uiWeeklyPlans,
                        selectedDay = planResult.selectedDay
                    )
                    _message.value = planResult.message
                } else {
                    _planState.value = PlanSnapshot(planType = planType)
                    _error.value = result.exceptionOrNull()?.message ?: "Error desconocido al generar el plan"
                }
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
    private val generateRecipePlanUseCase: GenerateRecipePlanUseCase
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RecipeViewModel::class.java)) {
            return RecipeViewModel(recipeRepository, generateRecipePlanUseCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
