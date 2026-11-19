package com.example.chefsito.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

/**
 * Cliente HTTP nativo (HttpURLConnection + org.json) para consumir una API
 * externa sin API Key. No requiere Retrofit ni dependencias adicionales.
 *
 * Endpoint: https://dummyjson.com/recipes?limit=8
 */
object CommunityRecipeService {

    private const val ENDPOINT = "https://dummyjson.com/recipes?limit=8"
    private const val TIMEOUT_MS = 10_000

    suspend fun fetchRecipes(): List<CommunityRecipe> = withContext(Dispatchers.IO) {
        val connection = (URL(ENDPOINT).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = TIMEOUT_MS
            readTimeout = TIMEOUT_MS
            setRequestProperty("Accept", "application/json")
            setRequestProperty("User-Agent", "Chefsito-Android")
        }

        try {
            val status = connection.responseCode
            if (status !in 200..299) {
                throw IOException("La API respondió con HTTP $status")
            }

            val body = connection.inputStream.bufferedReader().use { it.readText() }
            parseRecipes(body)
        } finally {
            connection.disconnect()
        }
    }

    private fun parseRecipes(body: String): List<CommunityRecipe> {
        val root = JSONObject(body)
        val array = root.optJSONArray("recipes") ?: JSONArray()

        return buildList {
            for (index in 0 until array.length()) {
                val item = array.optJSONObject(index) ?: continue
                add(
                    CommunityRecipe(
                        id = item.optInt("id", index),
                        name = item.optString("name").ifBlank { "Receta de la comunidad" },
                        cuisine = item.optString("cuisine").ifBlank { "Internacional" },
                        difficulty = item.optString("difficulty").ifBlank { "Fácil" },
                        prepTimeMinutes = item.optInt("prepTimeMinutes", 0),
                        ingredients = item.optJSONArray("ingredients").toStringList(),
                        instructions = item.optJSONArray("instructions").toStringList(),
                        imageUrl = item.optString("image")
                    )
                )
            }
        }
    }

    private fun JSONArray?.toStringList(): List<String> {
        if (this == null) return emptyList()
        return buildList {
            for (index in 0 until length()) {
                val value = optString(index)
                if (value.isNotBlank()) add(value)
            }
        }
    }
}
