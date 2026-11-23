package com.example.chefsito.domain.usecase

import android.content.Context
import android.net.Uri
import com.example.chefsito.utils.ImageUtils
import com.example.chefsito.utils.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Caso de uso: actualizar foto de perfil.
 * Maneja guardado en almacenamiento interno + SharedPreferences + SessionManager.
 */
class UpdateProfileImageUseCase constructor(
    private val context: Context
) {
    suspend operator fun invoke(uri: Uri): String? = withContext(Dispatchers.IO) {
        val path = ImageUtils.saveImageToInternalStorage(context, uri) ?: return@withContext null
        val prefs = context.getSharedPreferences("chefsito_prefs_", Context.MODE_PRIVATE)
        prefs.edit().putString("profile_image_path", path).apply()
        SessionManager.saveProfileImagePath(context, path)
        path
    }
}

/**
 * Caso de uso: eliminar foto de perfil.
 * Borra el archivo físico + limpia SharedPreferences + SessionManager.
 */
class DeleteProfileImageUseCase constructor(
    private val context: Context
) {
    suspend operator fun invoke(): Boolean = withContext(Dispatchers.IO) {
        val prefs = context.getSharedPreferences("chefsito_prefs_", Context.MODE_PRIVATE)
        val currentPath = prefs.getString("profile_image_path", null)
            ?: SessionManager.getProfileImagePath(context)
        
        currentPath?.let { path ->
            File(path).delete()
        }
        
        prefs.edit().remove("profile_image_path").apply()
        SessionManager.clearProfileImagePath(context)
        true
    }
}

/**
 * Caso de uso: actualizar datos de perfil (nombre, email, teléfono).
 */
class UpdateProfileDataUseCase constructor(
    private val context: Context
) {
    suspend operator fun invoke(name: String, email: String, phone: String) = withContext(Dispatchers.IO) {
        val prefs = context.getSharedPreferences("chefsito_prefs_", Context.MODE_PRIVATE)
        prefs.edit()
            .putString("profile_name", name.trim())
            .putString("profile_email", email.trim())
            .putString("profile_phone", phone.trim())
            .apply()
        Unit
    }
}

/**
 * Caso de uso: actualizar presupuesto del usuario.
 */
class UpdateBudgetUseCase constructor(
    private val context: Context
) {
    suspend operator fun invoke(budget: Double) = withContext(Dispatchers.IO) {
        val prefs = context.getSharedPreferences("chefsito_prefs_", Context.MODE_PRIVATE)
        prefs.edit().putFloat("user_budget", budget.toFloat()).apply()
        Unit
    }
}

/**
 * Caso de uso: obtener presupuesto guardado.
 */
class GetBudgetUseCase constructor(
    private val context: Context
) {
    suspend operator fun invoke(): Double = withContext(Dispatchers.IO) {
        val prefs = context.getSharedPreferences("chefsito_prefs_", Context.MODE_PRIVATE)
        prefs.getFloat("user_budget", 150f).toDouble()
    }
}