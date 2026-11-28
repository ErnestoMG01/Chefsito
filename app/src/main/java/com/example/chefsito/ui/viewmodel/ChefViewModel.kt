package com.example.chefsito.ui.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.chefsito.data.local.ChefsitoDatabase
import com.example.chefsito.data.local.entity.IngredientEntity
import com.example.chefsito.data.remote.ChefsitoCloudApiService
import com.example.chefsito.utils.ImageUtils
import com.example.chefsito.utils.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class ChefViewModel(application: Application) : AndroidViewModel(application) {

    private val appContext = application.applicationContext
    private val ingredientDao = ChefsitoDatabase.getDatabase(application).ingredientDao()

    /**
     * Despensa reactiva aislada por el usuario que tiene sesión activa (`activeUserIdFlow`).
     * Si otro usuario inicia sesión en el dispositivo o sincroniza desde AWS, ve únicamente su despensa.
     */
    val pantryIngredients: StateFlow<List<IngredientEntity>> =
        SessionManager.activeUserIdFlow
            .flatMapLatest { userId ->
                ingredientDao.getPantryIngredientsByUser(userId)
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

    private val _userBudget = MutableStateFlow(150.0)
    val userBudget: StateFlow<Double> = _userBudget.asStateFlow()

    private val _profileImagePath = MutableStateFlow<String?>(null)
    val profileImagePath: StateFlow<String?> = _profileImagePath.asStateFlow()

    private val _profileName = MutableStateFlow("")
    val profileName: StateFlow<String> = _profileName.asStateFlow()

    private val _profileEmail = MutableStateFlow("")
    val profileEmail: StateFlow<String> = _profileEmail.asStateFlow()

    private val _profilePhone = MutableStateFlow("")
    val profilePhone: StateFlow<String> = _profilePhone.asStateFlow()

    private val _awsApiBaseUrl = MutableStateFlow(SessionManager.getApiBaseUrl(application))
    val awsApiBaseUrl: StateFlow<String> = _awsApiBaseUrl.asStateFlow()

    init {
        viewModelScope.launch {
            SessionManager.activeUserIdFlow.collect { userId ->
                reloadUserPreferences(userId)
                syncPantryFromCloudIfAuthenticated(userId)
            }
        }
    }

    private fun userPrefs(userId: Long = SessionManager.activeUserIdFlow.value) =
        appContext.getSharedPreferences("chefsito_prefs_$userId", Context.MODE_PRIVATE)

    private fun reloadUserPreferences(userId: Long) {
        val prefs = userPrefs(userId)
        _userBudget.value = prefs.getFloat("user_budget", 150f).toDouble()
        _profileImagePath.value = prefs.getString("profile_image_path", null)
            ?: SessionManager.getProfileImagePath(appContext)
        _profileName.value = prefs.getString("profile_name", "")
            ?.ifBlank { SessionManager.getUserName(appContext).orEmpty() }
            .orEmpty()
        _profileEmail.value = prefs.getString("profile_email", "")
            ?.ifBlank { SessionManager.getUserEmail(appContext).orEmpty() }
            .orEmpty()
        _profilePhone.value = prefs.getString("profile_phone", "").orEmpty()
        _awsApiBaseUrl.value = SessionManager.getApiBaseUrl(appContext)
    }

    fun syncPantryFromCloudIfAuthenticated(userId: Long = SessionManager.activeUserIdFlow.value) {
        if (!SessionManager.hasCloudJwt(appContext)) return
        val token = SessionManager.getSessionToken(appContext) ?: return
        val baseUrl = SessionManager.getApiBaseUrl(appContext)

        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                val remoteItems = ChefsitoCloudApiService.fetchUserPantry(baseUrl, token, userId)
                for (remote in remoteItems) {
                    val existing = ingredientDao.getIngredientByUserAndName(userId, remote.name)
                    if (existing == null) {
                        ingredientDao.insertIngredient(remote.copy(id = 0L, userId = userId))
                    } else {
                        ingredientDao.updateIngredient(
                            existing.copy(
                                category = remote.category,
                                userCost = remote.userCost,
                                isInPantry = remote.isInPantry
                            )
                        )
                    }
                }
            }
        }
    }

    fun updateBudget(newBudget: Double) {
        _userBudget.value = newBudget
        userPrefs().edit().putFloat("user_budget", newBudget.toFloat()).apply()
    }

    fun updateAwsApiBaseUrl(newUrl: String) {
        SessionManager.saveApiBaseUrl(appContext, newUrl)
        _awsApiBaseUrl.value = SessionManager.getApiBaseUrl(appContext)
        syncPantryFromCloudIfAuthenticated()
    }

    fun updateProfileImage(context: Context, uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            val path = ImageUtils.saveImageToInternalStorage(context, uri) ?: return@launch
            _profileImagePath.value = path
            userPrefs().edit().putString("profile_image_path", path).apply()
            SessionManager.saveProfileImagePath(context, path)
        }
    }

    fun updateProfileData(name: String, email: String, phone: String) {
        _profileName.value = name.trim()
        _profileEmail.value = email.trim()
        _profilePhone.value = phone.trim()
        userPrefs().edit()
            .putString("profile_name", name.trim())
            .putString("profile_email", email.trim())
            .putString("profile_phone", phone.trim())
            .apply()
    }

    fun saveIngredient(name: String, category: String, userCost: Double, id: Long = 0L) {
        val currentUserId = SessionManager.activeUserIdFlow.value
        val entity = IngredientEntity(
            id = id,
            userId = currentUserId,
            name = name.trim(),
            category = category,
            userCost = userCost
        )
        viewModelScope.launch(Dispatchers.IO) {
            ingredientDao.insertIngredient(entity)
            if (SessionManager.hasCloudJwt(appContext)) {
                val token = SessionManager.getSessionToken(appContext) ?: return@launch
                val baseUrl = SessionManager.getApiBaseUrl(appContext)
                runCatching {
                    ChefsitoCloudApiService.upsertPantryItem(baseUrl, token, entity)
                }
            }
        }
    }

    fun removeIngredient(ingredient: IngredientEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            ingredientDao.deleteIngredient(ingredient)
            if (SessionManager.hasCloudJwt(appContext)) {
                val token = SessionManager.getSessionToken(appContext) ?: return@launch
                val baseUrl = SessionManager.getApiBaseUrl(appContext)
                runCatching {
                    ChefsitoCloudApiService.deletePantryItem(baseUrl, token, ingredient.name)
                }
            }
        }
    }
}
