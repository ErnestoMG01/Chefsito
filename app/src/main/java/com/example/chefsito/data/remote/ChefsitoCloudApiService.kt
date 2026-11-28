package com.example.chefsito.data.remote

import com.example.chefsito.data.local.entity.IngredientEntity
import com.example.chefsito.data.local.entity.RecipeEntity
import com.example.chefsito.data.local.entity.UserEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

data class CloudAuthResult(
    val user: UserEntity,
    val accessToken: String,
    val budget: Double
)

data class CloudSyncReport(
    val importedCount: Int,
    val updatedCount: Int,
    val totalCatalogCount: Int
)

/**
 * Cliente HTTP nativo para comunicarse con el Backend de Chefsito desplegado en AWS (EC2 + RDS PostgreSQL).
 *
 * Maneja:
 * 1. Registro y Login con JWT + Bcrypt (`/api/v1/auth/register`, `/api/v1/auth/login`).
 * 2. Descarga y sincronización incremental de recetas dinámicas (`/api/v1/recipes`).
 * 3. Ingesta en vivo desde proveedores externos hacia RDS (`/api/v1/recipes/sync-external`).
 * 4. Creación de nuevas recetas desde la app hacia la nube (`/api/v1/recipes`).
 * 5. Sincronización de despensa y favoritos aislados por usuario (`/api/v1/pantry`, `/api/v1/favorites`).
 */
object ChefsitoCloudApiService {

    private const val TIMEOUT_MS = 8_000
    private const val DEFAULT_ADMIN_KEY = "chefsito-admin-243873"

    suspend fun register(
        baseUrl: String,
        name: String,
        email: String,
        password: String
    ): CloudAuthResult = withContext(Dispatchers.IO) {
        val payload = JSONObject().apply {
            put("name", name)
            put("email", email)
            put("password", password)
        }
        val response = requestJson(
            url = "$baseUrl/api/v1/auth/register",
            method = "POST",
            body = payload.toString()
        )
        parseAuthResult(response)
    }

    suspend fun login(
        baseUrl: String,
        email: String,
        password: String
    ): CloudAuthResult = withContext(Dispatchers.IO) {
        val payload = JSONObject().apply {
            put("email", email)
            put("password", password)
        }
        val response = requestJson(
            url = "$baseUrl/api/v1/auth/login",
            method = "POST",
            body = payload.toString()
        )
        parseAuthResult(response)
    }

    suspend fun fetchCatalogRecipes(
        baseUrl: String,
        jwtToken: String? = null
    ): List<RecipeEntity> = withContext(Dispatchers.IO) {
        val raw = requestRaw(
            url = "$baseUrl/api/v1/recipes",
            method = "GET",
            jwtToken = jwtToken
        )
        val array = JSONArray(raw)
        buildList {
            for (i in 0 until array.length()) {
                val obj = array.optJSONObject(i) ?: continue
                add(parseRecipeEntity(obj))
            }
        }
    }

    suspend fun triggerDynamicExternalIngestion(
        baseUrl: String,
        maxToImport: Int = 8
    ): CloudSyncReport = withContext(Dispatchers.IO) {
        val response = requestJson(
            url = "$baseUrl/api/v1/recipes/sync-external?max_to_import=$maxToImport",
            method = "POST",
            body = "{}"
        )
        CloudSyncReport(
            importedCount = response.optInt("imported_count", 0),
            updatedCount = response.optInt("updated_count", 0),
            totalCatalogCount = response.optInt("total_catalog_count", 0)
        )
    }

    suspend fun createCloudRecipe(
        baseUrl: String,
        title: String,
        description: String,
        mealType: String,
        dayOfWeek: String,
        prepTimeMinutes: Int,
        difficulty: String,
        rawIngredientsText: String,
        rawStepsText: String
    ): RecipeEntity = withContext(Dispatchers.IO) {
        val payload = JSONObject().apply {
            put("title", title)
            put("description", description)
            put("meal_type", mealType)
            put("day_of_week", dayOfWeek)
            put("prep_time_minutes", prepTimeMinutes)
            put("difficulty", difficulty)
            put("raw_ingredients_text", rawIngredientsText)
            put("raw_steps_text", rawStepsText)
            put("source", "USER_CLOUD_APP")
        }
        val response = requestJson(
            url = "$baseUrl/api/v1/recipes",
            method = "POST",
            body = payload.toString(),
            extraHeaders = mapOf("X-Admin-Key" to DEFAULT_ADMIN_KEY)
        )
        parseRecipeEntity(response)
    }

    suspend fun fetchUserPantry(
        baseUrl: String,
        jwtToken: String,
        userId: Long
    ): List<IngredientEntity> = withContext(Dispatchers.IO) {
        val raw = requestRaw(
            url = "$baseUrl/api/v1/pantry",
            method = "GET",
            jwtToken = jwtToken
        )
        val array = JSONArray(raw)
        buildList {
            for (i in 0 until array.length()) {
                val item = array.optJSONObject(i) ?: continue
                add(
                    IngredientEntity(
                        id = item.optLong("id", 0L),
                        userId = userId,
                        name = item.optString("name"),
                        category = item.optString("category", "Abarrotes / General"),
                        userCost = item.optDouble("user_cost", 0.0),
                        isInPantry = item.optBoolean("is_in_pantry", true)
                    )
                )
            }
        }
    }

    suspend fun upsertPantryItem(
        baseUrl: String,
        jwtToken: String,
        ingredient: IngredientEntity
    ) = withContext(Dispatchers.IO) {
        val payload = JSONObject().apply {
            put("name", ingredient.name)
            put("category", ingredient.category)
            put("user_cost", ingredient.userCost)
            put("is_in_pantry", ingredient.isInPantry)
        }
        requestRaw(
            url = "$baseUrl/api/v1/pantry",
            method = "POST",
            body = payload.toString(),
            jwtToken = jwtToken
        )
    }

    suspend fun deletePantryItem(
        baseUrl: String,
        jwtToken: String,
        ingredientName: String
    ) = withContext(Dispatchers.IO) {
        val encoded = URLEncoder.encode(ingredientName, "UTF-8")
        requestRaw(
            url = "$baseUrl/api/v1/pantry/$encoded",
            method = "DELETE",
            jwtToken = jwtToken
        )
    }

    suspend fun toggleFavoriteInCloud(
        baseUrl: String,
        jwtToken: String,
        recipeId: Long
    ): Boolean = withContext(Dispatchers.IO) {
        val obj = requestJson(
            url = "$baseUrl/api/v1/favorites/$recipeId/toggle",
            method = "POST",
            body = "{}",
            jwtToken = jwtToken
        )
        obj.optBoolean("is_favorite", false)
    }

    private fun parseAuthResult(root: JSONObject): CloudAuthResult {
        val token = root.optString("access_token")
        val userObj = root.getJSONObject("user")
        val user = UserEntity(
            id = userObj.optLong("id", 1L),
            name = userObj.optString("name", "Chef"),
            email = userObj.optString("email", ""),
            passwordHash = "cloud-bcrypt-managed"
        )
        val budget = userObj.optDouble("budget", 150.0)
        return CloudAuthResult(user = user, accessToken = token, budget = budget)
    }

    private fun parseRecipeEntity(obj: JSONObject): RecipeEntity {
        return RecipeEntity(
            id = obj.optLong("id", 0L),
            title = obj.optString("title", "Receta Mexicana"),
            estimatedPrice = obj.optDouble("estimated_price", 0.0),
            prepTimeMinutes = obj.optInt("prep_time_minutes", 25),
            difficulty = obj.optString("difficulty", "Fácil"),
            isFavorite = obj.optBoolean("is_favorite", false),
            description = obj.optString("description", ""),
            mealType = obj.optString("meal_type", "Comida"),
            dayOfWeek = obj.optString("day_of_week", ""),
            ingredients = obj.optString("ingredients", ""),
            steps = obj.optString("steps", ""),
            source = obj.optString("source", "AWS_CLOUD_CATALOG"),
            externalId = obj.optString("external_id", "")
        )
    }

    private fun requestJson(
        url: String,
        method: String,
        body: String? = null,
        jwtToken: String? = null,
        extraHeaders: Map<String, String> = emptyMap()
    ): JSONObject {
        val raw = requestRaw(url, method, body, jwtToken, extraHeaders)
        return if (raw.isBlank()) JSONObject() else JSONObject(raw)
    }

    private fun requestRaw(
        url: String,
        method: String,
        body: String? = null,
        jwtToken: String? = null,
        extraHeaders: Map<String, String> = emptyMap()
    ): String {
        val conn = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = method
            connectTimeout = TIMEOUT_MS
            readTimeout = TIMEOUT_MS
            setRequestProperty("Accept", "application/json")
            setRequestProperty("User-Agent", "Chefsito-Android-AWS/2.0")
            if (!jwtToken.isNullOrBlank() && !jwtToken.startsWith("local-")) {
                setRequestProperty("Authorization", "Bearer $jwtToken")
            }
            extraHeaders.forEach { (k, v) -> setRequestProperty(k, v) }
            if (body != null && method in listOf("POST", "PUT", "PATCH")) {
                doOutput = true
                setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            }
        }

        try {
            if (body != null && method in listOf("POST", "PUT", "PATCH")) {
                conn.outputStream.bufferedWriter(Charsets.UTF_8).use { it.write(body) }
            }
            val code = conn.responseCode
            if (code !in 200..299) {
                val errBody = runCatching {
                    conn.errorStream?.bufferedReader()?.use { it.readText() }.orEmpty()
                }.getOrDefault("")
                val detail = runCatching {
                    JSONObject(errBody).optString("detail").takeIf { it.isNotBlank() }
                }.getOrNull() ?: "HTTP $code"
                throw IOException(detail)
            }
            if (code == HttpURLConnection.HTTP_NO_CONTENT) return ""
            return conn.inputStream.bufferedReader().use { it.readText() }
        } finally {
            conn.disconnect()
        }
    }
}
