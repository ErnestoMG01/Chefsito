package com.example.chefsito.domain.model

import com.example.chefsito.data.domain.RecipeCostBreakdown

/** Resultado del plan generado. */
data class RecipePlanResult(
    val planType: String,
    val recommendations: List<RecipeCostBreakdown>,
    val mealOptions: List<MealOptionGroup>,
    val weeklyPlans: List<DayPlan>,
    val selectedDay: String,
    val message: String
)

/** Plan para un día específico en la vista semanal. */
data class DayPlan(
    val dayOfWeek: String,
    val budget: Double,
    val options: List<MealOptionGroup>
)

/** Agrupa opciones por tipo de comida. */
data class MealOptionGroup(
    val mealType: String,
    val options: List<RecipeCostBreakdown>
)