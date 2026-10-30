package com.example.chefsito.utils

import android.content.Context
import android.net.Uri
import java.io.File

object ImageUtils {

    /**
     * Copia la imagen elegida a almacenamiento interno con un nombre nuevo cada vez,
     * para que la ruta cambie y la UI se refresque al cambiar de foto.
     */
    fun saveImageToInternalStorage(context: Context, uri: Uri): String? {
        return try {
            val newFile = File(context.filesDir, "profile_${System.currentTimeMillis()}.jpg")

            context.contentResolver.openInputStream(uri)?.use { input ->
                newFile.outputStream().use { output -> input.copyTo(output) }
            } ?: return null

            // Borra fotos de perfil anteriores
            context.filesDir.listFiles { f -> f.name.startsWith("profile_") && f != newFile }
                ?.forEach { it.delete() }

            newFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
