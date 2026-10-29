package com.example.chefsito.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.chefsito.data.local.entity.IngredientEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface IngredientDao {

    // Obtener todos los ingredientes de la despensa en tiempo real con Flow
    @Query("SELECT * FROM ingredients WHERE isInPantry = 1")
    fun getPantryIngredients(): Flow<List<IngredientEntity>>

    // Insertar o actualizar un ingrediente (ej. cuando actualizas el precio del huevo o agregas pollo)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIngredient(ingredient: IngredientEntity)

    // Borrar un ingrediente de la despensa
    @Delete
    suspend fun deleteIngredient(ingredient: IngredientEntity)
}