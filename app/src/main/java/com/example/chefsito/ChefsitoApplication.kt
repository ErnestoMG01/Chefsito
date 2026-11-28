package com.example.chefsito

import android.app.Application
import com.example.chefsito.data.local.ChefsitoDatabase
import com.example.chefsito.data.repository.AuthRepository
import com.example.chefsito.data.repository.AuthRepositoryImpl
import com.example.chefsito.data.repository.IngredientRepository
import com.example.chefsito.data.repository.IngredientRepositoryImpl
import com.example.chefsito.data.repository.RecipeRepository
import com.example.chefsito.data.repository.RecipeRepositoryImpl
import com.example.chefsito.utils.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class ChefsitoApplication : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // Se inicializa solo cuando se utiliza por primera vez
    val database: ChefsitoDatabase by lazy {
        ChefsitoDatabase.getDatabase(this)
    }

    val ingredientRepository: IngredientRepository by lazy {
        IngredientRepositoryImpl(database.ingredientDao())
    }

    val recipeRepository: RecipeRepository by lazy {
        RecipeRepositoryImpl(database.recipeDao(), this)
    }

    val authRepository: AuthRepository by lazy {
        AuthRepositoryImpl(database.userDao(), this)
    }

    override fun onCreate() {
        super.onCreate()
        SessionManager.init(this)
        // Pre-carga el catálogo de recetas local y sincroniza con el servidor AWS RDS si está disponible.
        applicationScope.launch {
            recipeRepository.seedIfEmpty()
        }
    }
}
