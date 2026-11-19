package com.example.chefsito.data.repository

import com.example.chefsito.data.local.dao.UserDao
import com.example.chefsito.data.local.entity.UserEntity
import com.example.chefsito.utils.PasswordHasher

class AuthRepositoryImpl(
    private val userDao: UserDao
) : AuthRepository {

    override suspend fun register(name: String, email: String, password: String): Result<UserEntity> {
        val cleanName = name.trim()
        val cleanEmail = normalizeEmail(email)

        if (cleanName.isBlank()) {
            return Result.failure(Exception("El nombre es requerido"))
        }
        if (cleanEmail.isBlank()) {
            return Result.failure(Exception("El correo es requerido"))
        }
        if (password.length < 6) {
            return Result.failure(Exception("La contraseña debe tener al menos 6 caracteres"))
        }
        if (userDao.getUserByEmail(cleanEmail) != null) {
            return Result.failure(Exception("Ya existe una cuenta con ese correo"))
        }

        return try {
            val user = UserEntity(
                name = cleanName,
                email = cleanEmail,
                passwordHash = PasswordHasher.sha256(password)
            )
            val id = userDao.registerUser(user)
            Result.success(user.copy(id = id))
        } catch (e: Exception) {
            Result.failure(Exception("No se pudo crear la cuenta: ${e.message ?: "error desconocido"}"))
        }
    }

    override suspend fun login(email: String, password: String): Result<UserEntity> {
        val cleanEmail = normalizeEmail(email)
        if (cleanEmail.isBlank() || password.isBlank()) {
            return Result.failure(Exception("Ingresa tu correo y contraseña"))
        }

        return try {
            val user = userDao.authenticate(cleanEmail, PasswordHasher.sha256(password))
                ?: return Result.failure(Exception("Correo o contraseña incorrectos"))
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(Exception("No se pudo iniciar sesión: ${e.message ?: "error desconocido"}"))
        }
    }

    override suspend fun getUserById(id: Long): UserEntity? = userDao.getUserById(id)

    override suspend fun emailExists(email: String): Boolean =
        userDao.getUserByEmail(normalizeEmail(email)) != null

    private fun normalizeEmail(email: String): String = email.trim().lowercase()
}
