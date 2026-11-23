package com.example.chefsito.ui.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.chefsito.data.local.entity.IngredientEntity
import com.example.chefsito.domain.usecase.DeleteIngredientUseCase
import com.example.chefsito.domain.usecase.DeleteProfileImageUseCase
import com.example.chefsito.domain.usecase.GetAllIngredientsUseCase
import com.example.chefsito.domain.usecase.GetBudgetUseCase
import com.example.chefsito.domain.usecase.GetPantryIngredientsUseCase
import com.example.chefsito.domain.usecase.SaveIngredientUseCase
import com.example.chefsito.domain.usecase.TogglePantryStatusUseCase
import com.example.chefsito.domain.usecase.UpdateBudgetUseCase
import com.example.chefsito.domain.usecase.UpdateProfileDataUseCase
import com.example.chefsito.domain.usecase.UpdateProfileImageUseCase
import com.example.chefsito.utils.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ChefViewModel(
    application: Application,
    private val getAllIngredientsUseCase: GetAllIngredientsUseCase,
    private val getPantryIngredientsUseCase: GetPantryIngredientsUseCase,
    private val saveIngredientUseCase: SaveIngredientUseCase,
    private val deleteIngredientUseCase: DeleteIngredientUseCase,
    private val togglePantryStatusUseCase: TogglePantryStatusUseCase,
    private val getBudgetUseCase: GetBudgetUseCase,
    private val updateBudgetUseCase: UpdateBudgetUseCase,
    private val updateProfileImageUseCase: UpdateProfileImageUseCase,
    private val deleteProfileImageUseCase: DeleteProfileImageUseCase,
    private val updateProfileDataUseCase: UpdateProfileDataUseCase
) : AndroidViewModel(application) {

    val pantryIngredients: StateFlow<List<IngredientEntity>> =
        getPantryIngredientsUseCase()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

    private val _userBudget = MutableStateFlow(0.0)
    val userBudget: StateFlow<Double> = _userBudget.asStateFlow()

    init {
        loadBudget()
    }

    private fun loadBudget() {
        viewModelScope.launch {
            _userBudget.value = getBudgetUseCase()
        }
    }

    fun updateBudget(newBudget: Double) {
        _userBudget.value = newBudget
        viewModelScope.launch {
            updateBudgetUseCase(newBudget)
        }
    }

    private val _profileImagePath = MutableStateFlow<String?>(null)
    val profileImagePath: StateFlow<String?> = _profileImagePath.asStateFlow()

    fun updateProfileImage(context: Context, uri: Uri) {
        viewModelScope.launch {
            val path = updateProfileImageUseCase(uri)
            path?.let { _profileImagePath.value = it }
        }
    }

    fun deleteProfileImage(context: Context) {
        viewModelScope.launch {
            val success = deleteProfileImageUseCase()
            if (success) {
                _profileImagePath.value = null
            }
        }
    }

    // ---- Datos personales del perfil (se guardan en el teléfono) ----
    private val _profileName = MutableStateFlow("")
    val profileName: StateFlow<String> = _profileName.asStateFlow()

    private val _profileEmail = MutableStateFlow("")
    val profileEmail: StateFlow<String> = _profileEmail.asStateFlow()

    private val _profilePhone = MutableStateFlow("")
    val profilePhone: StateFlow<String> = _profilePhone.asStateFlow()

    init {
        loadProfileData()
    }

    private fun loadProfileData() {
        viewModelScope.launch {
            val prefs = getApplication<Application>().getSharedPreferences("chefsito_prefs_", Context.MODE_PRIVATE)
            _profileName.value = prefs.getString("profile_name", "") ?: ""
            _profileEmail.value = prefs.getString("profile_email", "") ?: ""
            _profilePhone.value = prefs.getString("profile_phone", "") ?: ""
        }
    }

    fun updateProfileData(name: String, email: String, phone: String) {
        _profileName.value = name.trim()
        _profileEmail.value = email.trim()
        _profilePhone.value = phone.trim()
        viewModelScope.launch {
            updateProfileDataUseCase(name, email, phone)
        }
    }

    fun saveIngredient(name: String, category: String, userCost: Double, id: Long = 0L) {
        viewModelScope.launch {
            saveIngredientUseCase(
                IngredientEntity(
                    id = id,
                    name = name.trim(),
                    category = category,
                    userCost = userCost
                )
            )
        }
    }

    fun removeIngredient(ingredient: IngredientEntity) {
        viewModelScope.launch {
            deleteIngredientUseCase(ingredient)
        }
    }

    fun togglePantryStatus(ingredient: IngredientEntity) {
        viewModelScope.launch {
            togglePantryStatusUseCase(ingredient)
        }
    }
}

class ChefViewModelFactory(
    private val application: Application,
    private val getAllIngredientsUseCase: GetAllIngredientsUseCase,
    private val getPantryIngredientsUseCase: GetPantryIngredientsUseCase,
    private val saveIngredientUseCase: SaveIngredientUseCase,
    private val deleteIngredientUseCase: DeleteIngredientUseCase,
    private val togglePantryStatusUseCase: TogglePantryStatusUseCase,
    private val getBudgetUseCase: GetBudgetUseCase,
    private val updateBudgetUseCase: UpdateBudgetUseCase,
    private val updateProfileImageUseCase: UpdateProfileImageUseCase,
    private val deleteProfileImageUseCase: DeleteProfileImageUseCase,
    private val updateProfileDataUseCase: UpdateProfileDataUseCase
) : androidx.lifecycle.ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChefViewModel::class.java)) {
            return ChefViewModel(
                application = application,
                getAllIngredientsUseCase = getAllIngredientsUseCase,
                getPantryIngredientsUseCase = getPantryIngredientsUseCase,
                saveIngredientUseCase = saveIngredientUseCase,
                deleteIngredientUseCase = deleteIngredientUseCase,
                togglePantryStatusUseCase = togglePantryStatusUseCase,
                getBudgetUseCase = getBudgetUseCase,
                updateBudgetUseCase = updateBudgetUseCase,
                updateProfileImageUseCase = updateProfileImageUseCase,
                deleteProfileImageUseCase = deleteProfileImageUseCase,
                updateProfileDataUseCase = updateProfileDataUseCase
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}