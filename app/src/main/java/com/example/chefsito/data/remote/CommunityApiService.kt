package com.example.chefsito.data.remote

import com.example.chefsito.data.remote.dto.CommunityRecipeDto
import kotlinx.coroutines.flow.Flow

/**
 * Interface para el servicio de API externa de recetas de la comunidad.
 * Permite testear con mocks y cambiar implementación (Retrofit, Ktor, HttpURLConnection).
 */
interface CommunityApiService {

    /**
     * Obtiene la lista de recetas desde la API externa.
     * @return Flow con la respuesta parseada a DTOs
     * @throws IOException si hay error de red o HTTP != 2xx
     */
    suspend fun fetchRecipes(): List<CommunityRecipeDto>
}