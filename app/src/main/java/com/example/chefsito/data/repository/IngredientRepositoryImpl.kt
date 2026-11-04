package com.example.chefsito.data.repository

import com.example.chefsito.data.local.dao.IngredientDao
import com.example.chefsito.data.local.entity.IngredientEntity
import kotlinx.coroutines.flow.Flow

class IngredientRepositoryImpl(
    private val ingredientDao: IngredientDao
) : IngredientRepository {

    override fun getAllIngredients(): Flow<List<IngredientEntity>> {
        return ingredientDao.getAllIngredients()
    }

    override suspend fun getIngredientById(id: Long): IngredientEntity? {
        return ingredientDao.getIngredientById(id)
    }

    override suspend fun insertIngredient(ingredient: IngredientEntity) {
        ingredientDao.insertIngredient(ingredient)
    }

    override suspend fun updateIngredient(ingredient: IngredientEntity) {
        ingredientDao.updateIngredient(ingredient)
    }

    override suspend fun deleteIngredient(ingredient: IngredientEntity) {
        ingredientDao.deleteIngredient(ingredient)
    }
}