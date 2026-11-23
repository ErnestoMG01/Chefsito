package com.example.chefsito.data.remote

import com.example.chefsito.data.remote.dto.CommunityApiResponseDto
import com.example.chefsito.data.remote.dto.CommunityRecipeDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

/**
 * Implementación nativa con HttpURLConnection de la API de recetas comunitarias.
 * No requiere Retrofit ni dependencias adicionales.
 * 
 * Endpoint: https://dummyjson.com/recipes?limit=8
 */
class CommunityApiServiceImpl : CommunityApiService {

    private companion object {
        private const val ENDPOINT = "https://dummyjson.com/recipes?limit=8"
        private const val TIMEOUT_MS = 10_000
    }

    override suspend fun fetchRecipes(): List<CommunityRecipeDto> = withContext(Dispatchers.IO) {
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
            val response = CommunityApiResponseDto.fromJson(org.json.JSONObject(body))
            response.recipes
        } finally {
            connection.disconnect()
        }
    }
}