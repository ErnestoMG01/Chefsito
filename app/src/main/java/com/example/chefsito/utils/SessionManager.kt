package com.example.chefsito.utils

import android.content.Context

/**
 * Sesión del usuario activo.
 *
 * Guarda el identificador y los datos básicos en SharedPreferences para que la
 * app recuerde al usuario entre aperturas. `logout()` borra SOLO las claves de
 * sesión, conservando la marca de "primer inicio" del splash.
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

    fun saveSession(context: Context, userId: Long, userName: String, email: String) {
        val token = "local-${userId}-${System.currentTimeMillis()}"
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_TOKEN, token)
            .putLong(KEY_USER_ID, userId)
            .putString(KEY_EMAIL, email)
            .putString(KEY_USER_NAME, userName)
            .putBoolean(KEY_LOGGED_IN, true)
            .putLong(KEY_TIMESTAMP, System.currentTimeMillis())
            .apply()
    }

    fun getSessionToken(context: Context): String? = prefs(context).getString(KEY_TOKEN, null)

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
