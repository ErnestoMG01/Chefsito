package com.example.chefsito.domain.usecase

import com.example.chefsito.data.repository.CommunityRepository
import com.example.chefsito.data.remote.CommunityRecipe
import com.example.chefsito.domain.model.CommunityRecipesUiState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Caso de uso para obtener recetas de la comunidad.
 * Encapsula la lógica: Repository → manejo Result → Flow para UI.
 */
class GetCommunityRecipesUseCase constructor(
    private val repository: CommunityRepository
) {

    /**
     * Ejecuta la obtención de recetas y emite estados para la UI:
     * - Loading inicial
     * - Success con lista de recetas
     * - Error con mensaje y fallback local
     */
    operator fun invoke(): Flow<CommunityRecipesUiState> = flow {
        emit(CommunityRecipesUiState.Loading)

        val result = repository.getCommunityRecipes()

        if (result.isSuccess) {
            emit(CommunityRecipesUiState.Success(result.getOrNull() ?: emptyList()))
        } else {
            emit(CommunityRecipesUiState.Error(
                message = result.exceptionOrNull()?.message ?: "Error desconocido",
                fallback = emptyList()
            ))
        }
    }

    /** Fuerza recarga desde la API (ignora cache si existiera). */
    suspend fun refresh(): Result<List<CommunityRecipe>> {
        return repository.refreshCommunityRecipes()
    }
}