package com.example.chefsito.utils

import android.content.Context
import java.util.UUID

object SessionManager {
    private const val PREF_NAME = "chefsito_session"
    private const val KEY_TOKEN = "session_token"
    private const val KEY_EMAIL = "session_email"
    private const val KEY_USER_NAME = "session_user_name"
    private const val KEY_TIMESTAMP = "session_timestamp_ms"
    private const val KEY_FIRST_RUN = "is_first_run"

    fun saveSession(context: Context, email: String, userName: String) {
        val token = UUID.randomUUID().toString()
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_TOKEN, token)
            .putString(KEY_EMAIL, email)
            .putString(KEY_USER_NAME, userName)
            .putLong(KEY_TIMESTAMP, System.currentTimeMillis())
            .apply()
    }

    fun getSessionToken(context: Context): String? {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_TOKEN, null)
    }

    fun getUserEmail(context: Context): String? {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_EMAIL, null)
    }

    fun getUserName(context: Context): String? {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_USER_NAME, null)
    }

    fun isAuthenticated(context: Context, maxAgeMillis: Long = 24 * 60 * 60 * 1000L): Boolean {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val timestamp = prefs.getLong(KEY_TIMESTAMP, 0L)
        return (System.currentTimeMillis() - timestamp <= maxAgeMillis) && prefs.contains(KEY_TOKEN)
    }

    fun clearSession(context: Context) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
    }

    fun isFirstRun(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_FIRST_RUN, true)
    }

    fun setFirstRunCompleted(context: Context) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_FIRST_RUN, false).apply()
    }
}
