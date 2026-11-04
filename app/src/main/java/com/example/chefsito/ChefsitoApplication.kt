package com.example.chefsito

import android.app.Application
import com.example.chefsito.data.local.ChefsitoDatabase
import com.example.chefsito.data.repository.IngredientRepository
import com.example.chefsito.data.repository.IngredientRepositoryImpl

class ChefsitoApplication : Application() {

    // Se inicializa solo cuando se utiliza por primera vez
    val database: ChefsitoDatabase by lazy {
        ChefsitoDatabase.getDatabase(this)
    }

    val ingredientRepository: IngredientRepository by lazy {
        IngredientRepositoryImpl(database.ingredientDao())
    }
}