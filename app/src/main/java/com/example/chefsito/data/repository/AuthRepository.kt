package com.example.chefsito.data.repository

import com.example.chefsito.data.local.entity.UserEntity

/**
 * Contrato de autenticación de Chefsito.
 *
 * La implementación actual es 100% local (Room + SHA-256), por lo que la app
 * funciona sin servidor. Si más adelante se conecta un backend REST, basta con
 * crear otra implementación de esta interfaz (Local-First / Offline-Ready).
 */
interface AuthRepository {
    suspend fun register(name: String, email: String, password: String): Result<UserEntity>
    suspend fun login(email: String, password: String): Result<UserEntity>
    suspend fun getUserById(id: Long): UserEntity?
    suspend fun emailExists(email: String): Boolean
}
