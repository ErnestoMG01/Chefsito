package com.example.chefsito.data.repository

import com.example.chefsito.data.remote.CommunityRecipe
import kotlinx.coroutines.flow.Flow

/**
 * Interface del repositorio de recetas de la comunidad.
 * Abstrae la fuente de datos (API externa + fallback local).
 */
interface CommunityRepository {

    /**
     * Obtiene recetas de la comunidad.
     * Internamente maneja: API → éxito → cache, API → error → fallback local.
     */
    suspend fun getCommunityRecipes(): Result<List<CommunityRecipe>>

    /**
     * Fuerza recarga desde la API (ignora cache si la hubiera).
     */
    suspend fun refreshCommunityRecipes(): Result<List<CommunityRecipe>>
}