package com.example.chefsito.ui.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class AuthUiState(
    val isLoginMode: Boolean = true,
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val isAuthenticated: Boolean = false,
)

class AuthViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun setLoginMode(isLogin: Boolean) {
        _uiState.update {
            it.copy(
                isLoginMode = isLogin,
                errorMessage = null,
                successMessage = null
            )
        }
    }

    fun onNameChange(name: String) {
        _uiState.update { it.copy(name = name, errorMessage = null) }
    }

    fun onEmailChange(email: String) {
        _uiState.update { it.copy(email = email, errorMessage = null) }
    }

    fun onPasswordChange(password: String) {
        _uiState.update { it.copy(password = password, errorMessage = null) }
    }

    fun togglePasswordVisibility() {
        _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }

    fun onForgotPasswordClick() {
        if (_uiState.value.email.isBlank()) {
            _uiState.update {
                it.copy(
                    errorMessage = "Ingresa tu correo para recuperar la contraseña",
                    successMessage = null
                )
            }
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(_uiState.value.email.trim()).matches()) {
            _uiState.update {
                it.copy(
                    errorMessage = "Ingresa un correo electrónico válido",
                    successMessage = null
                )
            }
        } else {
            _uiState.update {
                it.copy(
                    errorMessage = null,
                    successMessage = "Se ha enviado un correo para restablecer tu contraseña"
                )
            }
        }
    }

    fun submitForm(onLoginSuccess: (String) -> Unit) {
        val state = _uiState.value
        val emailTrimmed = state.email.trim()

        if (state.isLoginMode) {
            // Validaciones Login
            if (emailTrimmed.isBlank() || state.password.isBlank()) {
                _uiState.update {
                    it.copy(
                        errorMessage = "Completa todos los campos",
                        successMessage = null
                    )
                }
                return
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(emailTrimmed).matches()) {
                _uiState.update {
                    it.copy(
                        errorMessage = "Ingresa un correo electrónico válido",
                        successMessage = null
                    )
                }
                return
            }

            if (state.password.length < 6) {
                _uiState.update {
                    it.copy(
                        errorMessage = "La contraseña debe tener al menos 6 caracteres",
                        successMessage = null
                    )
                }
                return
            }

            // Éxito Login
            val displayName = state.name.ifBlank { emailTrimmed.substringBefore("@") }
            _uiState.update {
                it.copy(
                    errorMessage = null,
                    successMessage = "Inicio de sesión exitoso",
                    isAuthenticated = true
                )
            }
            onLoginSuccess(displayName)

        } else {
            // Validaciones Registro
            if (state.name.isBlank() || emailTrimmed.isBlank() || state.password.isBlank()) {
                _uiState.update {
                    it.copy(
                        errorMessage = "Completa todos los campos",
                        successMessage = null
                    )
                }
                return
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(emailTrimmed).matches()) {
                _uiState.update {
                    it.copy(
                        errorMessage = "Ingresa un correo electrónico válido",
                        successMessage = null
                    )
                }
                return
            }

            if (state.password.length < 6) {
                _uiState.update {
                    it.copy(
                        errorMessage = "La contraseña debe tener al menos 6 caracteres",
                        successMessage = null
                    )
                }
                return
            }

            // Éxito Registro -> Cambia a modo Iniciar Sesión mostrando banner de éxito como en la imagen
            _uiState.update {
                it.copy(
                    isLoginMode = true,
                    errorMessage = null,
                    successMessage = "Registro completado. Ya puedes iniciar sesión."
                )
            }
        }
    }
}
