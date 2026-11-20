package com.example.chefsito.ui.recipe

import com.example.chefsito.data.domain.RecipeCostBreakdown
import com.example.chefsito.data.local.entity.RecipeEntity

/** Alternativas disponibles para un tipo de comida (Desayuno, Comida o Cena). */
data class MealOptionGroup(
    val mealType: String,
    val options: List<RecipeCostBreakdown>
)

/** Plan compacto de un día dentro de la vista semanal. */
data class DayPlan(
    val dayOfWeek: String,
    val budget: Double,
    val options: List<MealOptionGroup>
) {
    val estimatedCost: Double
        get() = options.sumOf { group -> group.options.firstOrNull()?.totalCost ?: 0.0 }
}

data class RecipeUiState(
    val recipes: List<RecipeEntity> = emptyList(),
    val favoriteRecipes: List<RecipeEntity> = emptyList(),
    val isLoading: Boolean = false,
    val isGeneratingRecipe: Boolean = false,
    val planType: String = "Día",
    val recommendations: List<RecipeCostBreakdown> = emptyList(),
    val mealOptions: List<MealOptionGroup> = emptyList(),
    val weeklyPlans: List<DayPlan> = emptyList(),
    val selectedDay: String = "",
    val message: String? = null,
    val error: String? = null
)
