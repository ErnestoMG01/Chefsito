package com.example.chefsito.data.repository

import android.content.Context
import com.example.chefsito.data.domain.RecipeCostBreakdown
import com.example.chefsito.data.domain.RecipeCosting
import com.example.chefsito.data.local.RecipeSeed
import com.example.chefsito.data.local.dao.RecipeDao
import com.example.chefsito.data.local.entity.IngredientEntity
import com.example.chefsito.data.local.entity.RecipeEntity
import com.example.chefsito.data.local.entity.UserFavoriteEntity
import com.example.chefsito.data.remote.ChefsitoCloudApiService
import com.example.chefsito.utils.SessionManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest

@OptIn(ExperimentalCoroutinesApi::class)
class RecipeRepositoryImpl(
    private val recipeDao: RecipeDao,
    private val appContext: Context? = null
) : RecipeRepository {

    /**
     * Devuelve el catálogo completo proyectando `isFavorite` específicamente para el
     * usuario con sesión activa (`SessionManager.activeUserIdFlow`). Así varios usuarios
     * pueden usar la app sin mezclar sus favoritos.
     */
    override fun getAllRecipes(): Flow<List<RecipeEntity>> =
        SessionManager.activeUserIdFlow.flatMapLatest { userId ->
            combine(
                recipeDao.getAllRecipes(),
                recipeDao.getFavoriteIdsForUser(userId)
            ) { recipes, favIds ->
                val favSet = favIds.toSet()
                recipes.map { recipe ->
                    recipe.copy(isFavorite = recipe.id in favSet)
                }
            }
        }

    override fun getFavoriteRecipes(): Flow<List<RecipeEntity>> =
        SessionManager.activeUserIdFlow.flatMapLatest { userId ->
            recipeDao.getFavoriteRecipesForUser(userId).combine(
                recipeDao.getFavoriteIdsForUser(userId)
            ) { favRecipes, _ ->
                favRecipes.map { it.copy(isFavorite = true) }
            }
        }

    override suspend fun getRecipeById(id: Long): RecipeEntity? {
        val recipe = recipeDao.getRecipeById(id) ?: return null
        val userId = SessionManager.activeUserIdFlow.value
        val isFav = recipeDao.isFavoriteForUser(userId, id)
        return recipe.copy(isFavorite = isFav)
    }

    override suspend fun getRecipeCount(): Int = recipeDao.getRecipeCount()

    override suspend fun insertRecipe(recipe: RecipeEntity) = recipeDao.insertRecipe(recipe)

    override suspend fun updateRecipe(recipe: RecipeEntity) {
        val userId = SessionManager.activeUserIdFlow.value
        if (recipe.isFavorite) {
            recipeDao.addUserFavorite(UserFavoriteEntity(userId = userId, recipeId = recipe.id))
        } else {
            recipeDao.removeUserFavorite(userId = userId, recipeId = recipe.id)
        }
        recipeDao.updateRecipe(recipe)
    }

    override suspend fun deleteRecipe(recipe: RecipeEntity) = recipeDao.deleteRecipe(recipe)

    override suspend fun toggleFavoriteForCurrentUser(recipeId: Long) {
        val userId = SessionManager.activeUserIdFlow.value
        val currentlyFav = recipeDao.isFavoriteForUser(userId, recipeId)
        if (currentlyFav) {
            recipeDao.removeUserFavorite(userId, recipeId)
        } else {
            recipeDao.addUserFavorite(UserFavoriteEntity(userId = userId, recipeId = recipeId))
        }

        // Sincronizar en segundo plano con AWS RDS si el usuario tiene token JWT
        if (appContext != null && SessionManager.hasCloudJwt(appContext)) {
            val baseUrl = SessionManager.getApiBaseUrl(appContext)
            val token = SessionManager.getSessionToken(appContext) ?: return
            runCatching {
                ChefsitoCloudApiService.toggleFavoriteInCloud(baseUrl, token, recipeId)
            }
        }
    }

    override suspend fun syncWithCloudCatalog(fetchNewExternal: Boolean): Result<Int> {
        val context = appContext ?: return Result.failure(Exception("Contexto no disponible"))
        return runCatching {
            val baseUrl = SessionManager.getApiBaseUrl(context)
            val token = SessionManager.getSessionToken(context)

            if (fetchNewExternal) {
                runCatching {
                    ChefsitoCloudApiService.triggerDynamicExternalIngestion(baseUrl, maxToImport = 8)
                }
            }

            val cloudRecipes = ChefsitoCloudApiService.fetchCatalogRecipes(baseUrl, token)
            val userId = SessionManager.activeUserIdFlow.value

            for (remote in cloudRecipes) {
                val existing = recipeDao.getRecipeByTitle(remote.title)
                if (existing == null) {
                    recipeDao.insertRecipe(remote.copy(id = 0L))
                    val inserted = recipeDao.getRecipeByTitle(remote.title)
                    if (inserted != null && remote.isFavorite) {
                        recipeDao.addUserFavorite(
                            UserFavoriteEntity(userId = userId, recipeId = inserted.id)
                        )
                    }
                } else {
                    recipeDao.updateRecipe(
                        existing.copy(
                            estimatedPrice = remote.estimatedPrice,
                            prepTimeMinutes = remote.prepTimeMinutes,
                            difficulty = remote.difficulty,
                            description = remote.description,
                            mealType = remote.mealType,
                            dayOfWeek = remote.dayOfWeek,
                            ingredients = remote.ingredients,
                            steps = remote.steps,
                            source = remote.source,
                            externalId = remote.externalId
                        )
                    )
                    if (remote.isFavorite) {
                        recipeDao.addUserFavorite(
                            UserFavoriteEntity(userId = userId, recipeId = existing.id)
                        )
                    }
                }
            }

            recipeDao.getRecipeCount()
        }
    }

    override suspend fun createDynamicRecipe(
        title: String,
        description: String,
        mealType: String,
        dayOfWeek: String,
        prepTimeMinutes: Int,
        difficulty: String,
        rawIngredientsText: String,
        rawStepsText: String
    ): Result<RecipeEntity> = runCatching {
        val cleanTitle = title.trim()
        require(cleanTitle.isNotBlank()) { "El título de la receta es obligatorio" }

        // 1. Intentar publicar primero en el servidor AWS para que todos los usuarios la reciban
        if (appContext != null) {
            val baseUrl = SessionManager.getApiBaseUrl(appContext)
            val cloudRecipe = runCatching {
                ChefsitoCloudApiService.createCloudRecipe(
                    baseUrl = baseUrl,
                    title = cleanTitle,
                    description = description.trim(),
                    mealType = mealType,
                    dayOfWeek = dayOfWeek,
                    prepTimeMinutes = prepTimeMinutes,
                    difficulty = difficulty,
                    rawIngredientsText = rawIngredientsText,
                    rawStepsText = rawStepsText
                )
            }.getOrNull()

            if (cloudRecipe != null) {
                val localToSave = cloudRecipe.copy(id = 0L)
                recipeDao.insertRecipe(localToSave)
                return@runCatching recipeDao.getRecipeByTitle(cleanTitle) ?: localToSave
            }
        }

        // 2. Fallback local en Room con cálculo automático de costos
        val formattedIngredients = rawIngredientsText.trim().lines()
            .filter { it.isNotBlank() }
            .map { line ->
                if ("|" in line) line.trim() else "${line.trim()}|8.0"
            }
        val estimatedTotal = formattedIngredients.sumOf {
            it.substringAfter("|").toDoubleOrNull() ?: 8.0
        }
        val formattedSteps = rawStepsText.trim().lines()
            .filter { it.isNotBlank() }
            .mapIndexed { idx, s ->
                val clean = s.trim()
                if (clean.firstOrNull()?.isDigit() == true) clean else "${idx + 1}. $clean"
            }

        val entity = RecipeEntity(
            title = cleanTitle,
            estimatedPrice = estimatedTotal,
            prepTimeMinutes = prepTimeMinutes,
            difficulty = difficulty,
            description = description.trim().ifBlank { "Receta agregada dinámicamente al catálogo de Chefsito." },
            mealType = mealType,
            dayOfWeek = dayOfWeek,
            ingredients = formattedIngredients.joinToString("\n"),
            steps = formattedSteps.joinToString("\n"),
            source = "DYNAMIC_LOCAL"
        )
        recipeDao.insertRecipe(entity)
        recipeDao.getRecipeByTitle(cleanTitle) ?: entity
    }

    override suspend fun recommendRecipes(
        pantry: List<IngredientEntity>,
        budget: Double,
        dayOfWeek: String?
    ): List<RecipeCostBreakdown> =
        recommendRecipeOptions(
            pantry = pantry,
            budget = budget,
            dayOfWeek = dayOfWeek,
            maxPerMeal = 1
        )

    override suspend fun recommendRecipeOptions(
        pantry: List<IngredientEntity>,
        budget: Double,
        dayOfWeek: String?,
        maxPerMeal: Int
    ): List<RecipeCostBreakdown> {
        val catalog = getAllRecipes().first()
        if (catalog.isEmpty()) return emptyList()

        val normalizedDay = dayOfWeek?.let { RecipeCosting.normalize(it) }
        val breakdowns = catalog.map { recipe ->
            RecipeCosting.breakdown(recipe, pantry, budget)
        }

        val mealOrder = listOf("Desayuno", "Comida", "Cena")
        val limit = maxPerMeal.coerceAtLeast(1)

        return mealOrder.flatMap { meal ->
            val candidates = breakdowns.filter {
                RecipeCosting.normalize(it.recipe.mealType) == RecipeCosting.normalize(meal)
            }
            if (candidates.isEmpty()) return@flatMap emptyList()

            val assignedToDay = if (normalizedDay.isNullOrBlank()) {
                emptyList()
            } else {
                candidates.filter {
                    RecipeCosting.normalize(it.recipe.dayOfWeek) == normalizedDay
                }
            }
            val assignedIds = assignedToDay.map { it.recipe.id }.toSet()

            val comparator = compareByDescending<RecipeCostBreakdown> { it.matchCount }
                .thenByDescending { it.fitsBudget }
                .thenBy { it.missingCost }
                .thenBy { it.totalCost }
                .thenBy { it.recipe.title }

            val orderedAssigned = assignedToDay.sortedWith(comparator)
            val orderedRest = candidates
                .filter { it.recipe.id !in assignedIds }
                .sortedWith(comparator)

            (orderedAssigned + orderedRest)
                .distinctBy { it.recipe.id }
                .take(limit)
        }
    }

    override suspend fun seedIfEmpty() {
        if (recipeDao.getRecipeCount() == 0) {
            recipeDao.insertRecipes(RecipeSeed.recipes)
        }
        // Intentar sincronizar en segundo plano con el catálogo ampliado del servidor AWS
        syncWithCloudCatalog(fetchNewExternal = false)
    }
}
