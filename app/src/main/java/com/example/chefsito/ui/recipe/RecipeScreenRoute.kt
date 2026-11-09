package com.example.chefsito.ui.recipe

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.chefsito.ChefsitoApplication

@Composable
fun RecipeScreenRoute() {
    val context = LocalContext.current
    val application = context.applicationContext as ChefsitoApplication

    // Instancia del ViewModel utilizando el Factory con los repositorios desde Application
    val viewModel: RecipeViewModel = viewModel(
        factory = RecipeViewModelFactory(
            recipeRepository = application.recipeRepository,
            aiRecipeRepository = application.aiRecipeRepository
        )
    )

    // Recolectar el estado de manera segura considerando el ciclo de vida
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Manejo de errores visuales mediante Toast
    uiState.error?.let { errorMessage ->
        LaunchedEffect(errorMessage) {
            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }

    /*
     * TODO: [PARA EL COMPAÑERO DESARROLLADOR]
     * Aquí debes insertar la función composable que maqueta la vista visual (ej: RecipeScreen).
     * 
     * Instrucciones:
     * 1. Pásale los datos de la UI: 
     *      recipes = uiState.recipes
     *      isGenerating = uiState.isGeneratingRecipe
     * 
     * 2. Pásale las lambdas conectadas a las funciones del ViewModel:
     *      onGenerateRecipeFromPantry = { viewModel.generateAiRecipe() }
     *      
     *      // Para alternar favoritos, puedes usar updateRecipe invirtiendo el valor actual:
     *      onToggleFavorite = { recipe -> 
     *          viewModel.updateRecipe(recipe.copy(isFavorite = !recipe.isFavorite))
     *      }
     *      
     *      onDeleteRecipe = { recipe -> viewModel.deleteRecipe(recipe) }
     *      
     * Ejemplo de uso:
     * 
     * RecipeScreen(
     *     recipes = uiState.recipes,
     *     isGenerating = uiState.isGeneratingRecipe,
     *     onGenerateRecipeFromPantry = { viewModel.generateAiRecipe() },
     *     onToggleFavorite = { recipe -> viewModel.updateRecipe(recipe.copy(isFavorite = !recipe.isFavorite)) },
     *     onDeleteRecipe = { recipe -> viewModel.deleteRecipe(recipe) }
     * )
     */
}
