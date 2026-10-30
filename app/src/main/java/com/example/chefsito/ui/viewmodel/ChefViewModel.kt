package com.example.chefsito.ui.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.chefsito.data.local.ChefsitoDatabase
import com.example.chefsito.data.local.entity.IngredientEntity
import com.example.chefsito.utils.ImageUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ChefViewModel(application: Application) : AndroidViewModel(application) {

    private val ingredientDao = ChefsitoDatabase.getDatabase(application).ingredientDao()

    val pantryIngredients: StateFlow<List<IngredientEntity>> =
        ingredientDao.getPantryIngredients()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

    private val sharedPreferences =
        application.getSharedPreferences("chefsito_prefs", Context.MODE_PRIVATE)

    private val savedBudget = sharedPreferences.getFloat("user_budget", 150f).toDouble()

    private val _userBudget = MutableStateFlow(savedBudget)
    val userBudget: StateFlow<Double> = _userBudget.asStateFlow()

    fun updateBudget(newBudget: Double) {
        _userBudget.value = newBudget
        sharedPreferences.edit().putFloat("user_budget", newBudget.toFloat()).apply()
    }

    private val _profileImagePath = MutableStateFlow(
        sharedPreferences.getString("profile_image_path", null)
    )
    val profileImagePath: StateFlow<String?> = _profileImagePath.asStateFlow()

    fun updateProfileImage(context: Context, uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            val path = ImageUtils.saveImageToInternalStorage(context, uri) ?: return@launch
            _profileImagePath.value = path
            sharedPreferences.edit().putString("profile_image_path", path).apply()
        }
    }

    // ---- Datos personales del perfil (se guardan en el teléfono) ----
    private val _profileName = MutableStateFlow(sharedPreferences.getString("profile_name", "") ?: "")
    val profileName: StateFlow<String> = _profileName.asStateFlow()

    private val _profileEmail = MutableStateFlow(sharedPreferences.getString("profile_email", "") ?: "")
    val profileEmail: StateFlow<String> = _profileEmail.asStateFlow()

    private val _profilePhone = MutableStateFlow(sharedPreferences.getString("profile_phone", "") ?: "")
    val profilePhone: StateFlow<String> = _profilePhone.asStateFlow()

    fun updateProfileData(name: String, email: String, phone: String) {
        _profileName.value = name.trim()
        _profileEmail.value = email.trim()
        _profilePhone.value = phone.trim()
        sharedPreferences.edit()
            .putString("profile_name", name.trim())
            .putString("profile_email", email.trim())
            .putString("profile_phone", phone.trim())
            .apply()
    }

    fun saveIngredient(name: String, category: String, price: Double, id: Long = 0L) {
        viewModelScope.launch {
            ingredientDao.insertIngredient(
                IngredientEntity(id = id, name = name.trim(), category = category, price = price)
            )
        }
    }

    fun removeIngredient(ingredient: IngredientEntity) {
        viewModelScope.launch {
            ingredientDao.deleteIngredient(ingredient)
        }
    }
}