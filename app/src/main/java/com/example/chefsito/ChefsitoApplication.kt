package com.example.chefsito

import android.app.Application
import com.example.chefsito.data.local.ChefsitoDatabase
import com.example.chefsito.data.remote.AiRecipeService
import com.example.chefsito.data.remote.GeminiRecipeServiceImpl
import com.example.chefsito.data.repository.AiRecipeRepository
import com.example.chefsito.data.repository.IngredientRepository
import com.example.chefsito.data.repository.IngredientRepositoryImpl
import com.example.chefsito.data.repository.RecipeRepository
import com.example.chefsito.data.repository.RecipeRepositoryImpl

class ChefsitoApplication : Application() {

    // Se inicializa solo cuando se utiliza por primera vez
    val database: ChefsitoDatabase by lazy {
        ChefsitoDatabase.getDatabase(this)
    }

    val ingredientRepository: IngredientRepository by lazy {
        IngredientRepositoryImpl(database.ingredientDao())
    }

    val recipeRepository: RecipeRepository by lazy {
        RecipeRepositoryImpl(database.recipeDao())
    }
    
    val aiRecipeService: AiRecipeService by lazy {
        GeminiRecipeServiceImpl()
    }

    val aiRecipeRepository: AiRecipeRepository by lazy {
        AiRecipeRepository(
            ingredientRepository = ingredientRepository,
            aiRecipeService = aiRecipeService,
            recipeRepository = recipeRepository
        )
    }
}