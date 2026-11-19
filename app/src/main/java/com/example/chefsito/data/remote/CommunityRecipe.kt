package com.example.chefsito.data.remote

/**
 * Receta obtenida de la API pública de la comunidad (DummyJSON).
 * Se usa solo para demostrar la conexión operativa a un servicio externo.
 */
data class CommunityRecipe(
    val id: Int,
    val name: String,
    val cuisine: String,
    val difficulty: String,
    val prepTimeMinutes: Int,
    val ingredients: List<String>,
    val instructions: List<String>,
    val imageUrl: String
)
