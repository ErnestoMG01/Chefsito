package com.example.chefsito.ui.recipe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.chefsito.data.domain.RecipeCostBreakdown
import com.example.chefsito.data.domain.RecipeCosting
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
import java.util.Calendar
import java.util.TimeZone

private val weekDays = listOf(
    "Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo"
)

private const val MAX_OPTIONS_PER_MEAL = 3

private fun todayName(): String {
    val calendar = Calendar.getInstance(TimeZone.getTimeZone("America/Mexico_City"))
    // Calendar: domingo = 1, lunes = 2 ... sábado = 7. Índice lunes = 0.
    val index = (calendar.get(Calendar.DAY_OF_WEEK) + 5) % 7
    return weekDays[index]
}

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
    private val ingredientRepository: IngredientRepository
) : ViewModel() {

    private val _isGeneratingRecipe = MutableStateFlow(false)
    private val _isSyncingCatalog = MutableStateFlow(false)
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
        _isSyncingCatalog,
        _message,
        _error
    ) { state, isSyncing, message, error ->
        state.copy(
            isSyncingCatalog = isSyncing,
            message = message,
            error = error
        )
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

    /** Alterna el corazón de una receta para el usuario activo y la sincroniza con AWS RDS. */
    fun toggleFavorite(recipeId: Long) {
        viewModelScope.launch {
            recipeRepository.toggleFavoriteForCurrentUser(recipeId)
        }
    }

    /**
     * Sincroniza el catálogo con el servidor AWS RDS e importa nuevas recetas actualizadas
     * para que la aplicación siempre tenga más variedad que las 24 precargadas.
     */
    fun syncDynamicRecipesFromCloud(fetchNewExternal: Boolean = true) {
        if (_isSyncingCatalog.value) return
        viewModelScope.launch {
            _isSyncingCatalog.value = true
            _error.value = null
            _message.value = null

            val result = recipeRepository.syncWithCloudCatalog(fetchNewExternal = fetchNewExternal)
            result.onSuccess { totalCount ->
                _message.value = "Catálogo sincronizado con AWS: $totalCount recetas disponibles"
            }.onFailure { err ->
                val count = recipeRepository.getRecipeCount()
                _message.value = "Modo local activo ($count recetas). Configura la IP de tu servidor AWS EC2 en Mi Perfil para sincronizar en vivo (${err.message ?: "sin conexión"})."
            }
            _isSyncingCatalog.value = false
        }
    }

    /**
     * Permite crear y publicar una nueva receta dinámica en la base de datos (Room + AWS RDS).
     */
    fun createNewDynamicRecipe(
        title: String,
        description: String,
        mealType: String,
        dayOfWeek: String,
        prepTimeMinutes: Int,
        difficulty: String,
        rawIngredientsText: String,
        rawStepsText: String
    ) {
        viewModelScope.launch {
            _isSyncingCatalog.value = true
            val res = recipeRepository.createDynamicRecipe(
                title = title,
                description = description,
                mealType = mealType,
                dayOfWeek = dayOfWeek,
                prepTimeMinutes = prepTimeMinutes,
                difficulty = difficulty,
                rawIngredientsText = rawIngredientsText,
                rawStepsText = rawStepsText
            )
            res.onSuccess { created ->
                _message.value = "Nueva receta '${created.title}' agregada al catálogo dinámico"
            }.onFailure { err ->
                _error.value = "No se pudo guardar la receta: ${err.message}"
            }
            _isSyncingCatalog.value = false
        }
    }

    /**
     * Genera el plan de "Día" o "Semana" usando el motor determinista.
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

                if (planType == "Semana") {
                    val perDayBudget = budget / 7.0
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
                        _planState.value = PlanSnapshot(planType = "Semana")
                        _error.value = "No encontramos recetas para armar la semana. Registra más ingredientes en tu despensa."
                        return@launch
                    }

                    _planState.value = PlanSnapshot(
                        planType = "Semana",
                        recommendations = weeklyPlans.flatMap { plan ->
                            plan.options.mapNotNull { it.options.firstOrNull() }
                        },
                        weeklyPlans = weeklyPlans
                    )
                    _message.value = "Plan semanal listo: $daysWithOptions día(s) con opciones · presupuesto por día: $${perDayBudget.toInt()}"
                } else {
                    val day = dayOfWeek ?: todayName()
                    val options = recipeRepository.recommendRecipeOptions(
                        pantry = pantry,
                        budget = budget,
                        dayOfWeek = day,
                        maxPerMeal = MAX_OPTIONS_PER_MEAL
                    )
                    val grouped = groupByMeal(options)

                    if (grouped.isEmpty()) {
                        _planState.value = PlanSnapshot(planType = "Día")
                        _error.value = "No encontramos recetas para hoy. Registra ingredientes en tu despensa."
                        return@launch
                    }

                    _planState.value = PlanSnapshot(
                        planType = "Día",
                        recommendations = grouped.mapNotNull { it.options.firstOrNull() },
                        mealOptions = grouped,
                        selectedDay = day
                    )

                    val totalOptions = grouped.sumOf { it.options.size }
                    val missingCount = grouped.sumOf { group ->
                        group.options.firstOrNull()?.missing?.size ?: 0
                    }
                    _message.value = buildString {
                        append("Plan de $day: ")
                        append("$totalOptions opciones para ${grouped.size} tipo(s) de comida")
                        if (missingCount == 0) {
                            append(" · tienes todo en casa")
                        } else {
                            append(" · faltan $missingCount ingrediente(s) en la opción principal")
                        }
                        append(" · presupuesto: $${budget.toInt()}")
                    }
                }
            } catch (e: Exception) {
                _error.value = "No se pudo generar el plan: ${e.message ?: "error desconocido"}"
            } finally {
                _isGeneratingRecipe.value = false
            }
        }
    }

    /** Agrupa las alternativas en el orden fijo Desayuno → Comida → Cena. */
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
