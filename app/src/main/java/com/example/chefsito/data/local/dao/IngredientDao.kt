package com.example.chefsito.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.chefsito.data.local.entity.IngredientEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface IngredientDao {

    // Obtener todos los ingredientes
    @Query("SELECT * FROM ingredients")
    fun getAllIngredients(): Flow<List<IngredientEntity>>

    // Obtener ingrediente por ID
    @Query("SELECT * FROM ingredients WHERE id = :id")
    suspend fun getIngredientById(id: Long): IngredientEntity?

    // Obtener todos los ingredientes de la despensa en tiempo real con Flow
    @Query("SELECT * FROM ingredients WHERE isInPantry = 1")
    fun getPantryIngredients(): Flow<List<IngredientEntity>>

    // Insertar un ingrediente
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIngredient(ingredient: IngredientEntity)

    // Actualizar un ingrediente
    @Update
    suspend fun updateIngredient(ingredient: IngredientEntity)

    // Borrar un ingrediente de la despensa
    @Delete
    suspend fun deleteIngredient(ingredient: IngredientEntity)
}