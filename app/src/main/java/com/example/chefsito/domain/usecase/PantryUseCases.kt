package com.example.chefsito.domain.usecase

import com.example.chefsito.data.local.entity.IngredientEntity
import com.example.chefsito.data.repository.IngredientRepository
import kotlinx.coroutines.flow.Flow

/** Caso de uso: obtener todos los ingredientes (Flow reactivo). */
class GetAllIngredientsUseCase constructor(
    private val repository: IngredientRepository
) {
    operator fun invoke(): Flow<List<IngredientEntity>> = repository.getAllIngredients()
}

/** Caso de uso: obtener solo ingredientes en despensa (Flow reactivo). */
class GetPantryIngredientsUseCase constructor(
    private val repository: IngredientRepository
) {
    operator fun invoke(): Flow<List<IngredientEntity>> = repository.getPantryIngredients()
}

/** Caso de uso: guardar/actualizar ingrediente. */
class SaveIngredientUseCase constructor(
    private val repository: IngredientRepository
) {
    suspend operator fun invoke(ingredient: IngredientEntity) {
        if (ingredient.id == 0L) {
            repository.insertIngredient(ingredient)
        } else {
            repository.updateIngredient(ingredient)
        }
    }
}

/** Caso de uso: eliminar ingrediente. */
class DeleteIngredientUseCase constructor(
    private val repository: IngredientRepository
) {
    suspend operator fun invoke(ingredient: IngredientEntity) {
        repository.deleteIngredient(ingredient)
    }
}

/** Caso de uso: alternar estado de despensa (isInPantry). */
class TogglePantryStatusUseCase constructor(
    private val repository: IngredientRepository
) {
    suspend operator fun invoke(ingredient: IngredientEntity) {
        repository.togglePantryStatus(ingredient)
    }
}