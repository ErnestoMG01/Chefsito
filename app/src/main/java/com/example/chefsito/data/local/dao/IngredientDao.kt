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

    // Obtener todos los ingredientes del usuario activo
    @Query("SELECT * FROM ingredients WHERE userId = :userId ORDER BY name ASC")
    fun getIngredientsByUser(userId: Long): Flow<List<IngredientEntity>>

    // Compatibilidad general
    @Query("SELECT * FROM ingredients ORDER BY name ASC")
    fun getAllIngredients(): Flow<List<IngredientEntity>>

    // Obtener ingrediente por ID
    @Query("SELECT * FROM ingredients WHERE id = :id")
    suspend fun getIngredientById(id: Long): IngredientEntity?

    @Query("SELECT * FROM ingredients WHERE userId = :userId AND LOWER(name) = LOWER(:name) LIMIT 1")
    suspend fun getIngredientByUserAndName(userId: Long, name: String): IngredientEntity?

    // Obtener todos los ingredientes de la despensa del usuario activo en tiempo real con Flow
    @Query("SELECT * FROM ingredients WHERE isInPantry = 1 AND userId = :userId ORDER BY name ASC")
    fun getPantryIngredientsByUser(userId: Long): Flow<List<IngredientEntity>>

    @Query("SELECT * FROM ingredients WHERE isInPantry = 1 ORDER BY name ASC")
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
