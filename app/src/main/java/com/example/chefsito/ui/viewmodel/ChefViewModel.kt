package com.example.chefsito.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.chefsito.data.local.ChefsitoDatabase
import com.example.chefsito.data.local.entity.IngredientEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ChefViewModel(application: Application) : AndroidViewModel(application) {

    private val ingredientDao = ChefsitoDatabase.getDatabase(application).ingredientDao()

    // Ingredientes de la despensa en tiempo real (Room -> Flow -> StateFlow para Compose)
    val pantryIngredients: StateFlow<List<IngredientEntity>> =
        ingredientDao.getPantryIngredients()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

    fun saveIngredient(name: String, category: String, price: Double) {
        viewModelScope.launch {
            ingredientDao.insertIngredient(
                IngredientEntity(name = name.trim(), category = category, price = price)
            )
        }
    }

    fun removeIngredient(ingredient: IngredientEntity) {
        viewModelScope.launch {
            ingredientDao.deleteIngredient(ingredient)
        }
    }
}
