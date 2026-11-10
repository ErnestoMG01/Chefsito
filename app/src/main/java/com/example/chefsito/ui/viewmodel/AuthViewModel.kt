package com.example.chefsito.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.chefsito.data.local.entity.UserEntity
import com.example.chefsito.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoginMode: Boolean = true,
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isPasswordVisible: Boolean = false,
    val isConfirmPasswordVisible: Boolean = false,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val isLoading: Boolean = false,
    val authenticatedUser: UserEntity? = null,
)

class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun setLoginMode(isLogin: Boolean) {
        _uiState.update {
            it.copy(
                isLoginMode = isLogin,
                errorMessage = null,
                successMessage = null,
                emailError = null,
                passwordError = null,
                confirmPasswordError = null
            )
        }
    }

    fun onNameChange(name: String) {
        _uiState.update { it.copy(name = name, errorMessage = null) }
    }

    fun onEmailChange(email: String) {
        _uiState.update { it.copy(email = email, emailError = null, errorMessage = null) }
    }

    fun onPasswordChange(password: String) {
        _uiState.update { it.copy(password = password, passwordError = null, errorMessage = null) }
    }

    fun onConfirmPasswordChange(confirmPassword: String) {
        _uiState.update {
            it.copy(confirmPassword = confirmPassword, confirmPasswordError = null, errorMessage = null)
        }
    }

    fun togglePasswordVisibility() {
        _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun toggleConfirmPasswordVisibility() {
        _uiState.update { it.copy(isConfirmPasswordVisible = !it.isConfirmPasswordVisible) }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }

    fun onForgotPasswordClick() {
        val email = _uiState.value.email.trim()
        when {
            email.isBlank() -> _uiState.update {
                it.copy(errorMessage = "Ingresa tu correo para recuperar la contraseña", successMessage = null)
            }
            !isValidEmail(email) -> _uiState.update {
                it.copy(errorMessage = "Ingresa un correo electrónico válido", successMessage = null)
            }
            else -> _uiState.update {
                it.copy(errorMessage = null, successMessage = "Se ha enviado un correo para restablecer tu contraseña")
            }
        }
    }

    fun submitForm() {
        val state = _uiState.value
        if (state.isLoading) return

        val email = state.email.trim()
        var emailErr: String? = null
        var passwordErr: String? = null
        var confirmErr: String? = null
        var nameErr: String? = null

        if (email.isBlank()) {
            emailErr = "El correo es requerido"
        } else if (!isValidEmail(email)) {
            emailErr = "Ingresa un correo válido"
        }

        if (state.isLoginMode) {
            if (state.password.isBlank()) passwordErr = "La contraseña es requerida"
        } else {
            if (state.name.isBlank()) nameErr = "El nombre es requerido"
            if (state.password.length < 6) {
                passwordErr = "La contraseña debe tener al menos 6 caracteres"
            }
            if (state.confirmPassword.isBlank()) {
                confirmErr = "Confirma tu contraseña"
            } else if (state.confirmPassword != state.password) {
                confirmErr = "Las contraseñas no coinciden"
            }
        }

        val firstError = nameErr ?: emailErr ?: passwordErr ?: confirmErr
        if (firstError != null) {
            _uiState.update {
                it.copy(
                    emailError = emailErr,
                    passwordError = passwordErr,
                    confirmPasswordError = confirmErr,
                    errorMessage = firstError,
                    successMessage = null
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }

            val result = if (state.isLoginMode) {
                authRepository.login(email, state.password)
            } else {
                authRepository.register(state.name, email, state.password)
            }

            result.onSuccess { user ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        authenticatedUser = user,
                        successMessage = if (state.isLoginMode) {
                            "¡Bienvenido de nuevo, ${user.name}!"
                        } else {
                            "Cuenta creada. ¡Bienvenido, ${user.name}!"
                        },
                        errorMessage = null
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "No se pudo completar la operación"
                    )
                }
            }
        }
    }

    private fun isValidEmail(email: String): Boolean =
        android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
}

class AuthViewModelFactory(
    private val authRepository: AuthRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            return AuthViewModel(authRepository) as T
        }
        throw IllegalArgumentException("Clase ViewModel desconocida")
    }
}
