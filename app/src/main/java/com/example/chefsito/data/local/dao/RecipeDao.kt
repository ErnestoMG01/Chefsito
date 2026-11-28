package com.example.chefsito.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.chefsito.data.local.entity.RecipeEntity
import com.example.chefsito.data.local.entity.UserFavoriteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeDao {
    @Query("SELECT * FROM recipes ORDER BY title ASC")
    fun getAllRecipes(): Flow<List<RecipeEntity>>

    @Query("SELECT * FROM recipes WHERE isFavorite = 1 ORDER BY title ASC")
    fun getFavoriteRecipes(): Flow<List<RecipeEntity>>

    @Query(
        """
        SELECT r.* FROM recipes r
        INNER JOIN user_favorites uf ON uf.recipeId = r.id
        WHERE uf.userId = :userId
        ORDER BY r.title ASC
        """
    )
    fun getFavoriteRecipesForUser(userId: Long): Flow<List<RecipeEntity>>

    @Query("SELECT recipeId FROM user_favorites WHERE userId = :userId")
    fun getFavoriteIdsForUser(userId: Long): Flow<List<Long>>

    @Query("SELECT EXISTS(SELECT 1 FROM user_favorites WHERE userId = :userId AND recipeId = :recipeId)")
    suspend fun isFavoriteForUser(userId: Long, recipeId: Long): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addUserFavorite(favorite: UserFavoriteEntity)

    @Query("DELETE FROM user_favorites WHERE userId = :userId AND recipeId = :recipeId")
    suspend fun removeUserFavorite(userId: Long, recipeId: Long)

    @Query("SELECT * FROM recipes WHERE id = :id")
    suspend fun getRecipeById(id: Long): RecipeEntity?

    @Query("SELECT * FROM recipes WHERE LOWER(title) = LOWER(:title) LIMIT 1")
    suspend fun getRecipeByTitle(title: String): RecipeEntity?

    @Query("SELECT COUNT(*) FROM recipes")
    suspend fun getRecipeCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipe(recipe: RecipeEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipes(recipes: List<RecipeEntity>)

    @Update
    suspend fun updateRecipe(recipe: RecipeEntity)

    @Delete
    suspend fun deleteRecipe(recipe: RecipeEntity)
}
