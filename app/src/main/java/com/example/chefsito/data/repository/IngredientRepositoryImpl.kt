package com.example.chefsito.data.repository

import com.example.chefsito.data.local.dao.IngredientDao
import com.example.chefsito.data.local.entity.IngredientEntity
import com.example.chefsito.utils.SessionManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest

@OptIn(ExperimentalCoroutinesApi::class)
class IngredientRepositoryImpl(
    private val ingredientDao: IngredientDao
) : IngredientRepository {

    /**
     * Filtra automáticamente los ingredientes de la despensa por el usuario activo
     * (`SessionManager.activeUserIdFlow`), garantizando soporte multiusuario.
     */
    override fun getAllIngredients(): Flow<List<IngredientEntity>> {
        return SessionManager.activeUserIdFlow.flatMapLatest { userId ->
            ingredientDao.getIngredientsByUser(userId)
        }
    }

    override suspend fun getIngredientById(id: Long): IngredientEntity? {
        return ingredientDao.getIngredientById(id)
    }

    override suspend fun insertIngredient(ingredient: IngredientEntity) {
        val currentUserId = SessionManager.activeUserIdFlow.value
        ingredientDao.insertIngredient(ingredient.copy(userId = currentUserId))
    }

    override suspend fun updateIngredient(ingredient: IngredientEntity) {
        val currentUserId = SessionManager.activeUserIdFlow.value
        ingredientDao.updateIngredient(ingredient.copy(userId = currentUserId))
    }

    override suspend fun deleteIngredient(ingredient: IngredientEntity) {
        ingredientDao.deleteIngredient(ingredient)
    }
}
