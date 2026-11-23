package com.example.chefsito

import android.app.Application
import com.example.chefsito.data.local.ChefsitoDatabase
import com.example.chefsito.data.repository.AuthRepository
import com.example.chefsito.data.repository.AuthRepositoryImpl
import com.example.chefsito.data.repository.CommunityRepository
import com.example.chefsito.data.repository.CommunityRepositoryImpl
import com.example.chefsito.data.repository.IngredientRepository
import com.example.chefsito.data.repository.IngredientRepositoryImpl
import com.example.chefsito.data.repository.RecipeRepository
import com.example.chefsito.data.repository.RecipeRepositoryImpl
import com.example.chefsito.data.remote.CommunityApiService
import com.example.chefsito.data.remote.CommunityApiServiceImpl
import com.example.chefsito.domain.usecase.DeleteIngredientUseCase
import com.example.chefsito.domain.usecase.DeleteProfileImageUseCase
import com.example.chefsito.domain.usecase.GenerateRecipePlanUseCase
import com.example.chefsito.domain.usecase.GetAllIngredientsUseCase
import com.example.chefsito.domain.usecase.GetBudgetUseCase
import com.example.chefsito.domain.usecase.GetCommunityRecipesUseCase
import com.example.chefsito.domain.usecase.GetPantryIngredientsUseCase
import com.example.chefsito.domain.usecase.SaveIngredientUseCase
import com.example.chefsito.domain.usecase.TogglePantryStatusUseCase
import com.example.chefsito.domain.usecase.UpdateBudgetUseCase
import com.example.chefsito.domain.usecase.UpdateProfileDataUseCase
import com.example.chefsito.domain.usecase.UpdateProfileImageUseCase
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

    // --- Repositories (Data Layer) ---
    val ingredientRepository: IngredientRepository by lazy {
        IngredientRepositoryImpl(database.ingredientDao())
    }

    val recipeRepository: RecipeRepository by lazy {
        RecipeRepositoryImpl(database.recipeDao())
    }

    val authRepository: AuthRepository by lazy {
        AuthRepositoryImpl(database.userDao())
    }

    val communityApiService: CommunityApiService by lazy {
        CommunityApiServiceImpl()
    }

    val communityRepository: CommunityRepository by lazy {
        CommunityRepositoryImpl(communityApiService)
    }

    // --- Use Cases (Domain Layer) ---
    val getAllIngredientsUseCase: GetAllIngredientsUseCase by lazy {
        GetAllIngredientsUseCase(ingredientRepository)
    }

    val getPantryIngredientsUseCase: GetPantryIngredientsUseCase by lazy {
        GetPantryIngredientsUseCase(ingredientRepository)
    }

    val saveIngredientUseCase: SaveIngredientUseCase by lazy {
        SaveIngredientUseCase(ingredientRepository)
    }

    val deleteIngredientUseCase: DeleteIngredientUseCase by lazy {
        DeleteIngredientUseCase(ingredientRepository)
    }

    val togglePantryStatusUseCase: TogglePantryStatusUseCase by lazy {
        TogglePantryStatusUseCase(ingredientRepository)
    }

    val getCommunityRecipesUseCase: GetCommunityRecipesUseCase by lazy {
        GetCommunityRecipesUseCase(communityRepository)
    }

    val generateRecipePlanUseCase: GenerateRecipePlanUseCase by lazy {
        GenerateRecipePlanUseCase(recipeRepository, ingredientRepository)
    }

    val updateProfileImageUseCase: UpdateProfileImageUseCase by lazy {
        UpdateProfileImageUseCase(this)
    }

    val deleteProfileImageUseCase: DeleteProfileImageUseCase by lazy {
        DeleteProfileImageUseCase(this)
    }

    val updateProfileDataUseCase: UpdateProfileDataUseCase by lazy {
        UpdateProfileDataUseCase(this)
    }

    val updateBudgetUseCase: UpdateBudgetUseCase by lazy {
        UpdateBudgetUseCase(this)
    }

    val getBudgetUseCase: GetBudgetUseCase by lazy {
        GetBudgetUseCase(this)
    }

    override fun onCreate() {
        super.onCreate()
        // Pre-carga el catálogo de recetas la primera vez que arranca la app.
        applicationScope.launch {
            recipeRepository.seedIfEmpty()
        }
    }
}
