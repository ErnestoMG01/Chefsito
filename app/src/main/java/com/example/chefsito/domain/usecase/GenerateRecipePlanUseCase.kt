package com.example.chefsito.domain.usecase

import com.example.chefsito.data.domain.RecipeCostBreakdown
import com.example.chefsito.data.domain.RecipeCosting
import com.example.chefsito.data.local.entity.IngredientEntity
import com.example.chefsito.data.repository.IngredientRepository
import com.example.chefsito.data.repository.RecipeRepository
import com.example.chefsito.domain.model.DayPlan
import com.example.chefsito.domain.model.MealOptionGroup
import com.example.chefsito.domain.model.RecipePlanResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.first

/**
 * Caso de uso para generar planes de recetas (Día / Semana).
 * Extrae la lógica compleja de RecipeViewModel para hacerla testeable y reutilizable.
 */
class GenerateRecipePlanUseCase constructor(
    private val recipeRepository: RecipeRepository,
    private val ingredientRepository: IngredientRepository
) {

    private val weekDays = listOf(
        "Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo"
    )

    companion object {
        private const val MAX_OPTIONS_PER_MEAL = 3
    }

    /**
     * Genera el plan de recetas según el tipo (Día o Semana).
     */
    suspend fun execute(
        budget: Double,
        planType: String = "Día",
        dayOfWeek: String? = null
    ): Result<RecipePlanResult> = withContext(Dispatchers.IO) {
        try {
            if (budget <= 0.0) {
                return@withContext Result.failure(
                    IllegalArgumentException("El presupuesto debe ser mayor a \$0.00")
                )
            }

            val pantry = ingredientRepository.getAllIngredients()
                .first()
                .filter { it.isInPantry }

            return@withContext when (planType) {
                "Semana" -> generateWeeklyPlan(budget, pantry)
                else -> generateDailyPlan(budget, pantry, dayOfWeek ?: todayName())
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun generateDailyPlan(
        budget: Double,
        pantry: List<IngredientEntity>,
        day: String
    ): Result<RecipePlanResult> {
        val options = recipeRepository.recommendRecipeOptions(
            pantry = pantry,
            budget = budget,
            dayOfWeek = day,
            maxPerMeal = MAX_OPTIONS_PER_MEAL
        )

        val grouped = groupByMeal(options)

        if (grouped.isEmpty()) {
            return Result.failure(
                IllegalStateException("No encontramos recetas para hoy. Registra ingredientes en tu despensa.")
            )
        }

        val totalOptions = grouped.sumOf { it.options.size }
        val missingCount = grouped.sumOf { group ->
            group.options.firstOrNull()?.missing?.size ?: 0
        }

        val message = buildString {
            append("Plan de $day: ")
            append("\$totalOptions opciones para \${grouped.size} tipo(s) de comida")
            if (missingCount == 0) {
                append(" · tienes todo en casa")
            } else {
                append(" · faltan \$missingCount ingrediente(s) en la opción principal")
            }
            append(" · presupuesto: \$\${budget.toInt()}")
        }

        return Result.success(RecipePlanResult(
            planType = "Día",
            recommendations = grouped.mapNotNull { it.options.firstOrNull() },
            mealOptions = grouped,
            weeklyPlans = emptyList(),
            selectedDay = day,
            message = message
        ))
    }

    private suspend fun generateWeeklyPlan(
        weeklyBudget: Double,
        pantry: List<IngredientEntity>
    ): Result<RecipePlanResult> {
        val perDayBudget = weeklyBudget / 7.0
        val weeklyPlans = weekDays.map { day ->
            val options = recipeRepository.recommendRecipeOptions(
                pantry = pantry,
                budget = perDayBudget,
                dayOfWeek = day,
                maxPerMeal = MAX_OPTIONS_PER_MEAL
            )
            DayPlan(
                dayOfWeek = day,
                budget = perDayBudget,
                options = groupByMeal(options)
            )
        }

        val daysWithOptions = weeklyPlans.count { it.options.isNotEmpty() }

        if (daysWithOptions == 0) {
            return Result.failure(
                IllegalStateException("No encontramos recetas para armar la semana. Registra más ingredientes en tu despensa.")
            )
        }

        val recommendations = weeklyPlans.flatMap { plan ->
            plan.options.mapNotNull { it.options.firstOrNull() }
        }

        val message = "Plan semanal listo: \$daysWithOptions día(s) con opciones · presupuesto por día: \$\${perDayBudget.toInt()}"

        return Result.success(RecipePlanResult(
            planType = "Semana",
            recommendations = recommendations,
            mealOptions = emptyList(),
            weeklyPlans = weeklyPlans,
            selectedDay = "",
            message = message
        ))
    }

    private fun groupByMeal(
        breakdowns: List<RecipeCostBreakdown>
    ): List<MealOptionGroup> {
        val mealOrder = listOf("Desayuno", "Comida", "Cena")
        return mealOrder.mapNotNull { meal ->
            val options = breakdowns.filter {
                RecipeCosting.normalize(it.recipe.mealType) == RecipeCosting.normalize(meal)
            }
            if (options.isEmpty()) null else MealOptionGroup(mealType = meal, options = options)
        }
    }

    private fun todayName(): String {
        val calendar = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("America/Mexico_City"))
        val index = (calendar.get(java.util.Calendar.DAY_OF_WEEK) + 5) % 7
        return weekDays[index]
    }
}