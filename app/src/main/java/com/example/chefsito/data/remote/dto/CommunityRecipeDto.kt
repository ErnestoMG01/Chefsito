package com.example.chefsito.data.remote.dto

import org.json.JSONArray
import org.json.JSONObject

/**
 * DTOs para parsing de la respuesta JSON de DummyJSON API.
 * Estructura esperada: { "recipes": [ {...}, {...} ] }
 */
data class CommunityRecipeDto(
    val id: Int,
    val name: String,
    val cuisine: String,
    val difficulty: String,
    val prepTimeMinutes: Int,
    val cookTimeMinutes: Int,
    val servings: Int,
    val caloriesPerServing: Int,
    val ingredients: List<String>,
    val instructions: List<String>,
    val image: String,
    val rating: Double,
    val reviewCount: Int,
    val tags: List<String>,
    val mealType: List<String>
) {
    companion object {
        fun fromJson(json: JSONObject): CommunityRecipeDto = CommunityRecipeDto(
            id = json.optInt("id", 0),
            name = json.optString("name", "Receta sin nombre"),
            cuisine = json.optString("cuisine", "Internacional"),
            difficulty = json.optString("difficulty", "Fácil"),
            prepTimeMinutes = json.optInt("prepTimeMinutes", 0),
            cookTimeMinutes = json.optInt("cookTimeMinutes", 0),
            servings = json.optInt("servings", 1),
            caloriesPerServing = json.optInt("caloriesPerServing", 0),
            ingredients = json.optJSONArray("ingredients")?.toStringList() ?: emptyList(),
            instructions = json.optJSONArray("instructions")?.toStringList() ?: emptyList(),
            image = json.optString("image", ""),
            rating = json.optDouble("rating", 0.0),
            reviewCount = json.optInt("reviewCount", 0),
            tags = json.optJSONArray("tags")?.toStringList() ?: emptyList(),
            mealType = json.optJSONArray("mealType")?.toStringList() ?: emptyList()
        )
    }
}

data class CommunityApiResponseDto(
    val recipes: List<CommunityRecipeDto>,
    val total: Int,
    val skip: Int,
    val limit: Int
) {
    companion object {
        fun fromJson(json: JSONObject): CommunityApiResponseDto {
            val array = json.optJSONArray("recipes")
            val recipes = if (array != null) {
                (0 until array.length()).mapNotNull { index ->
                    (array.optJSONObject(index))?.let { CommunityRecipeDto.fromJson(it) }
                }
            } else {
                emptyList()
            }
            return CommunityApiResponseDto(
                recipes = recipes,
                total = json.optInt("total", 0),
                skip = json.optInt("skip", 0),
                limit = json.optInt("limit", 0)
            )
        }
    }
}

private fun JSONArray?.toStringList(): List<String> {
    if (this == null) return emptyList()
    return (0 until length()).mapNotNull { optString(it) }.filter { it.isNotBlank() }
}