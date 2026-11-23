package com.example.chefsito.data.repository

import com.example.chefsito.data.local.entity.IngredientEntity
import kotlinx.coroutines.flow.Flow

/**
 * Interface del repositorio de ingredientes (despensa del usuario).
 * Define el contrato para operaciones CRUD y consulta de ingredientes.
 */
interface IngredientRepository {

    /** Flujo reactivo con todos los ingredientes (incluye los que no están en despensa). */
    fun getAllIngredients(): Flow<List<IngredientEntity>>

    /** Flujo reactivo solo con ingredientes marcados como en despensa (isInPantry = true). */
    fun getPantryIngredients(): Flow<List<IngredientEntity>>

    /** Obtiene un ingrediente por su ID. */
    suspend fun getIngredientById(id: Long): IngredientEntity?

    /** Inserta un nuevo ingrediente. */
    suspend fun insertIngredient(ingredient: IngredientEntity)

    /** Actualiza un ingrediente existente. */
    suspend fun updateIngredient(ingredient: IngredientEntity)

    /** Elimina un ingrediente. */
    suspend fun deleteIngredient(ingredient: IngredientEntity)

    /** Alterna el estado isInPantry de un ingrediente. */
    suspend fun togglePantryStatus(ingredient: IngredientEntity)
}