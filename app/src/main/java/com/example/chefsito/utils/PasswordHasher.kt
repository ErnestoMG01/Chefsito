package com.example.chefsito.utils

import java.security.MessageDigest

/** Utilidades de seguridad para no guardar contraseñas en texto plano. */
object PasswordHasher {

    fun sha256(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest(password.toByteArray(Charsets.UTF_8))
        return bytes.joinToString(separator = "") { "%02x".format(it) }
    }
}
