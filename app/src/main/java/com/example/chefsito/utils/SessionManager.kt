package com.example.chefsito.utils

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Sesión del usuario activo y configuración del servidor en AWS.
 *
 * Guarda el identificador, el JWT del backend en AWS y la URL base del servidor
 * para que la app soporte múltiples usuarios simultáneos desde varios teléfonos
 * o sesiones sin mezclar despensas, presupuestos ni favoritos.
 */
object SessionManager {
    private const val PREF_NAME = "chefsito_session"
    private const val KEY_TOKEN = "session_token"
    private const val KEY_USER_ID = "session_user_id"
    private const val KEY_EMAIL = "session_email"
    private const val KEY_USER_NAME = "session_user_name"
    private const val KEY_LOGGED_IN = "session_logged_in"
    private const val KEY_TIMESTAMP = "session_timestamp_ms"
    private const val KEY_PROFILE_IMAGE = "session_profile_image_path"
    private const val KEY_FIRST_RUN = "is_first_run"
    private const val KEY_API_BASE_URL = "aws_api_base_url"

    /**
     * Por defecto apunta al host local desde el emulador Android (`http://10.0.2.2:8000`).
     * En el laboratorio de AWS se puede actualizar desde Perfil o BuildConfig a la IP/DNS
     * pública de la instancia EC2 (ej. `http://54.xxx.xxx.xxx:8000` o `https://api.tu-dominio.com`).
     */
    const val DEFAULT_AWS_BASE_URL = "http://3.91.46.131:8000"

    private val _activeUserIdFlow = MutableStateFlow(0L)
    val activeUserIdFlow: StateFlow<Long> = _activeUserIdFlow.asStateFlow()

    fun init(context: Context) {
        _activeUserIdFlow.value = getUserId(context) ?: 0L
    }

    fun saveSession(
        context: Context,
        userId: Long,
        userName: String,
        email: String,
        jwtToken: String? = null
    ) {
        val token = jwtToken?.takeIf { it.isNotBlank() }
            ?: "local-${userId}-${System.currentTimeMillis()}"
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_TOKEN, token)
            .putLong(KEY_USER_ID, userId)
            .putString(KEY_EMAIL, email)
            .putString(KEY_USER_NAME, userName)
            .putBoolean(KEY_LOGGED_IN, true)
            .putLong(KEY_TIMESTAMP, System.currentTimeMillis())
            .apply()
        _activeUserIdFlow.value = userId
    }

    fun getSessionToken(context: Context): String? = prefs(context).getString(KEY_TOKEN, null)

    fun hasCloudJwt(context: Context): Boolean {
        val token = getSessionToken(context) ?: return false
        return !token.startsWith("local-") && token.contains(".")
    }

    fun getUserId(context: Context): Long? {
        val id = prefs(context).getLong(KEY_USER_ID, -1L)
        return if (id >= 0L) id else null
    }

    fun getUserEmail(context: Context): String? = prefs(context).getString(KEY_EMAIL, null)

    fun getUserName(context: Context): String? = prefs(context).getString(KEY_USER_NAME, null)

    fun isLoggedIn(context: Context): Boolean = prefs(context).getBoolean(KEY_LOGGED_IN, false)

    /** Compatibilidad con el splash actual: sesión activa = usuario logueado. */
    fun isAuthenticated(context: Context): Boolean = isLoggedIn(context)

    /** Ruta absoluta de la foto de perfil elegida desde la galería. */
    fun getProfileImagePath(context: Context): String? =
        prefs(context).getString(KEY_PROFILE_IMAGE, null)

    fun saveProfileImagePath(context: Context, path: String) {
        prefs(context).edit().putString(KEY_PROFILE_IMAGE, path).apply()
    }

    fun clearProfileImagePath(context: Context) {
        prefs(context).edit().remove(KEY_PROFILE_IMAGE).apply()
    }

    /** URL base del backend desplegado en AWS EC2. */
    fun getApiBaseUrl(context: Context): String {
        val saved = prefs(context).getString(KEY_API_BASE_URL, null)
        return (saved?.takeIf { it.isNotBlank() } ?: DEFAULT_AWS_BASE_URL).trimEnd('/')
    }

    fun saveApiBaseUrl(context: Context, url: String) {
        val clean = url.trim().trimEnd('/')
        prefs(context).edit().putString(KEY_API_BASE_URL, clean.ifBlank { DEFAULT_AWS_BASE_URL }).apply()
    }

    fun logout(context: Context) {
        prefs(context).edit()
            .remove(KEY_TOKEN)
            .remove(KEY_USER_ID)
            .remove(KEY_EMAIL)
            .remove(KEY_USER_NAME)
            .remove(KEY_LOGGED_IN)
            .remove(KEY_TIMESTAMP)
            .remove(KEY_PROFILE_IMAGE)
            .apply()
        _activeUserIdFlow.value = 0L
    }

    /** Alias histórico usado en pantallas anteriores. */
    fun clearSession(context: Context) = logout(context)

    fun isFirstRun(context: Context): Boolean = prefs(context).getBoolean(KEY_FIRST_RUN, true)

    fun setFirstRunCompleted(context: Context) {
        prefs(context).edit().putBoolean(KEY_FIRST_RUN, false).apply()
    }

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
}
