package com.example.chefsito.ui.ingredient

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.chefsito.data.local.entity.IngredientEntity
import com.example.chefsito.data.repository.IngredientRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class IngredientViewModel(
    private val repository: IngredientRepository
) : ViewModel() {

    // Transformamos el Flow del repositorio en un StateFlow que la UI en Compose pueda escuchar
    val uiState: StateFlow<IngredientUiState> = repository.getAllIngredients()
        .map { list ->
            IngredientUiState(ingredients = list, isLoading = false)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = IngredientUiState(isLoading = true)
        )

    fun addIngredient(ingredient: IngredientEntity) {
        viewModelScope.launch {
            repository.insertIngredient(ingredient)
        }
    }

    fun deleteIngredient(ingredient: IngredientEntity) {
        viewModelScope.launch {
            repository.deleteIngredient(ingredient)
        }
    }
}

/**
 * Factory requerida por Android Framework para instanciar ViewModels
 * con parámetros en el constructor cuando no se utiliza Hilt ni Koin.
 */
class IngredientViewModelFactory(
    private val repository: IngredientRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(IngredientViewModel::class.java)) {
            return IngredientViewModel(repository) as T
        }
        throw IllegalArgumentException("Clase ViewModel desconocida")
    }
}