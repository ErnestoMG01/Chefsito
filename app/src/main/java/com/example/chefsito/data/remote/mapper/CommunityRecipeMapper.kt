package com.example.chefsito.data.remote.mapper

import com.example.chefsito.data.remote.dto.CommunityRecipeDto
import com.example.chefsito.data.remote.CommunityRecipe

/**
 * Mapper para convertir DTOs de la API a modelos de dominio.
 * Separa la capa de datos de la capa de presentación/dominio.
 */
object CommunityRecipeMapper {

    /**
     * Convierte un DTO de la API a modelo de dominio CommunityRecipe.
     * Asigna un ID único basado en el ID de la API + offset para evitar colisiones con BD local.
     */
    fun toDomain(dto: CommunityRecipeDto): CommunityRecipe {
        return CommunityRecipe(
            id = dto.id + 10000, // Offset para evitar colisiones con IDs locales (1-24)
            name = dto.name,
            cuisine = dto.cuisine,
            difficulty = dto.difficulty,
            prepTimeMinutes = dto.prepTimeMinutes,
            ingredients = dto.ingredients,
            instructions = dto.instructions,
            imageUrl = dto.image
        )
    }

    fun toDomainList(dtos: List<CommunityRecipeDto>): List<CommunityRecipe> {
        return dtos.map { toDomain(it) }
    }
}